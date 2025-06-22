package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.entity.Order;
import ru.yandex.practicum.enums.OrderState;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.feignclient.DeliveryFeignClient;
import ru.yandex.practicum.feignclient.PaymentFeignClient;
import ru.yandex.practicum.feignclient.ShoppingCartFeignClient;
import ru.yandex.practicum.feignclient.WarehouseFeignClient;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.repository.OrderRepository;
import ru.yandex.practicum.request.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.request.CreateNewOrderRequest;
import ru.yandex.practicum.request.ProductReturnRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final ShoppingCartFeignClient cartClient;
    private final WarehouseFeignClient warehouseClient;
    private final DeliveryFeignClient deliveryClient;
    private final PaymentFeignClient paymentClient;

    @Override
    public Page<OrderDto> getUserOrders(String username, Pageable pageable) {
        log.info("OrderService -> Получение заказов пользователя: {}", username);

        checkUser(username);

        ShoppingCartDto userCart = cartClient.getCartForUser(username);

        if (pageable.getSort().getFirst().equals("productName")) {
            pageable.setSort(List.of("state"));
        }

        Sort sort = Sort.by(Sort.DEFAULT_DIRECTION, String.join(",", pageable.getSort()));
        PageRequest pageRequest = PageRequest.of(pageable.getPage(), pageable.getSize(), sort);

        Page<OrderDto> orders = repository.getAllOrdersByCartId(userCart.getShoppingCartId(), pageRequest)
                .map(mapper::mapToDto);

        log.info("OrderService -> Получен список заказов: {}", orders);

        return orders;
    }

    @Override
    @Transactional
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        log.info("OrderService -> Создание заказа: {}", request);

        // Корзина пользователя
        ShoppingCartDto cart = request.getShoppingCart();

        Order order = Order.builder()
                .state(OrderState.NEW)
                .products(cart.getProducts())
                .cartId(cart.getShoppingCartId())
                .build();

        Order newOrder = repository.save(order);

        // Собираем товары к заказу для подготовки к отправке
        AssemblyProductsForOrderRequest assemblyProducts = new AssemblyProductsForOrderRequest();
        assemblyProducts.setOrderId(newOrder.getOrderId());
        assemblyProducts.setProducts(cart.getProducts());

        BookedProductsDto booking = warehouseClient.assemblyProductsForOrder(assemblyProducts);

        newOrder.setDeliveryVolume(booking.getDeliveryVolume());
        newOrder.setDeliveryWeight(booking.getDeliveryWeight());
        newOrder.setFragile(booking.getFragile());

        // Создание доставки
        DeliveryDto delivery = DeliveryDto.builder()
                .orderId(newOrder.getOrderId())
                .fromAddress(warehouseClient.getWarehouseAddress())
                .toAddress(request.getDeliveryAddress())
                .build();

        DeliveryDto savedDelivery = deliveryClient.planDelivery(delivery);
        newOrder.setDeliveryId(savedDelivery.getDeliveryId());

        // Формирование оплаты для заказа
        PaymentDto payment = paymentClient.payment(mapper.mapToDto(newOrder));
        newOrder.setPaymentId(payment.getPaymentId());

        // Расчёт стоимости товаров в заказе
        BigDecimal productPrice = paymentClient.productCost(mapper.mapToDto(newOrder));
        newOrder.setProductPrice(productPrice);

        Order savedOrder = repository.save(newOrder);
        OrderDto dto = mapper.mapToDto(savedOrder);

        log.info("OrderService -> Оформленный заказ: {}", dto);
        return dto;
    }

    @Override
    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {
        log.info("OrderService -> Запрос на возврат заказа: {}", request);

        Order order = getOrderById(request.getOrderId());
        warehouseClient.acceptReturn(request.getProducts());
        order.setState(OrderState.PRODUCT_RETURNED);
        OrderDto dto = mapper.mapToDto(repository.save(order));

        log.info("OrderService -> Заказ пользователя после сборки: {}", dto);
        return dto;
    }

    @Override
    @Transactional
    public OrderDto payment(UUID orderId) {
        log.info("OrderService -> Оплата заказа с id: {}", orderId);

        Order order = getOrderById(orderId);
        order.setState(OrderState.PAID);
        OrderDto dto = mapper.mapToDto(repository.save(order));

        log.info("OrderService -> Заказ пользователя после оплаты: {}", dto);
        return dto;
    }

    @Override
    @Transactional
    public OrderDto paymentFailed(UUID orderId) {
        log.info("OrderController -> Оплата заказа  с id: {} произошла с ошибкой!", orderId);

        Order order = getOrderById(orderId);
        order.setState(OrderState.PAYMENT_FAILED);
        OrderDto dto = mapper.mapToDto(repository.save(order));

        log.info("OrderController -> Заказ пользователя после ошибки оплаты: {}", dto);
        return dto;
    }

    @Override
    @Transactional
    public OrderDto delivery(UUID orderId) {
        log.info("OrderService -> Доставка заказа с id: {}!", orderId);

        Order order = getOrderById(orderId);
        order.setState(OrderState.DELIVERED);
        OrderDto dto = mapper.mapToDto(repository.save(order));

        log.info("OrderService -> Заказ пользователя после доставки: {}", dto);
        return dto;
    }

    @Override
    public OrderDto deliveryFailed(UUID orderId) {
        log.info("OrderController -> Доставка заказа с id: {} произошла с ошибкой!", orderId);

        Order order = getOrderById(orderId);
        order.setState(OrderState.DELIVERY_FAILED);
        OrderDto dto = mapper.mapToDto(repository.save(order));

        log.info("OrderController -> Заказ пользователя после ошибки доставки: {}", dto);
        return dto;
    }

    @Override
    @Transactional
    public OrderDto complete(UUID orderId) {
        log.info("OrderController -> Завершение заказа с id: {}!", orderId);

        Order order = getOrderById(orderId);
        order.setState(OrderState.COMPLETED);
        OrderDto dto = mapper.mapToDto(repository.save(order));

        log.info("OrderController -> Заказ пользователя после всех стадий и завершенный: {}", dto);
        return dto;
    }

    @Override
    @Transactional
    public OrderDto calculateTotal(UUID orderId) {
        log.info("OrderController -> Расчёт стоимости заказа с id: {}!", orderId);

        Order order = getOrderById(orderId);
        BigDecimal totalCost = paymentClient.getTotalCost(mapper.mapToDto(order));
        order.setTotalPrice(totalCost);
        OrderDto dto = mapper.mapToDto(repository.save(order));

        log.info("OrderController -> Заказ пользователя с расчётом общей стоимости: {}", dto);
        return dto;
    }

    @Override
    @Transactional
    public OrderDto calculateDelivery(UUID orderId) {
        log.info("OrderController -> Расчёт стоимости доставки заказа с id: {}!", orderId);

        Order order = getOrderById(orderId);
        BigDecimal deliveryPrice = deliveryClient.deliveryCost(mapper.mapToDto(order));
        order.setDeliveryPrice(deliveryPrice);
        OrderDto dto = mapper.mapToDto(repository.save(order));

        log.info("OrderController -> Заказ пользователя с расчётом доставки: {}", dto);
        return dto;
    }

    @Override
    @Transactional
    public OrderDto assembly(UUID orderId) {
        log.info("OrderController -> Сборка заказа с id: {}!", orderId);

        Order order = getOrderById(orderId);
        order.setState(OrderState.ASSEMBLED);
        OrderDto dto = mapper.mapToDto(repository.save(order));

        log.info("OrderController -> Заказ пользователя после сборки: {}", dto);
        return dto;
    }

    @Override
    @Transactional
    public OrderDto assemblyFailed(UUID orderId) {
        log.info("OrderController -> Сборка заказа с id - {}, произошла с ошибкой!", orderId);

        Order order = getOrderById(orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);
        OrderDto dto = mapper.mapToDto(repository.save(order));

        log.info("OrderController -> Заказ пользователя после ошибки сборки: {}", order);
        return dto;
    }

    private static void checkUser(String username) {
        if (username.isEmpty()) {
            throw new NotAuthorizedUserException();
        }
    }

    private Order getOrderById(UUID orderId) {
        return repository.findOrderByOrderId(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Не найден заказ: " + orderId));
    }
}