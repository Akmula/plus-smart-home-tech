package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.entity.Payment;
import ru.yandex.practicum.enums.PaymentState;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.feignclient.OrderFeignClient;
import ru.yandex.practicum.feignclient.ShoppingStoreFeignClient;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;
    private final PaymentMapper mapper;
    private final OrderFeignClient orderClient;
    private final ShoppingStoreFeignClient storeClient;

    @Value("${payment.vat}")
    private BigDecimal vat;

    @Override
    @Transactional
    public PaymentDto payment(OrderDto order) {
        log.info("PaymentService: -> Формирование оплаты для заказа: {}", order);

        checkOrder(order);

        Payment payment = Payment.builder()
                .productsTotal(order.getProductPrice())
                .deliveryTotal(order.getDeliveryPrice())
                .totalPayment(order.getTotalPrice())
                .feeTotal(order.getTotalPrice().multiply(vat))
                .paymentState(PaymentState.PENDING)
                .orderId(order.getOrderId())
                .build();

        PaymentDto savedPayment = mapper.mapToDto(repository.save(payment));

        log.info("PaymentService: -> Сформированная оплата заказа: {}", savedPayment);
        return savedPayment;
    }

    @Override
    public BigDecimal getTotalCost(OrderDto order) {
        log.info("PaymentService: -> Расчёт полной стоимости заказа: {}", order);

        checkOrder(order);

        BigDecimal productTotalCost = productCost(order);
        BigDecimal deliveryPrice = order.getDeliveryPrice();
        BigDecimal tax = productTotalCost.multiply(vat);

        BigDecimal totalCost = productTotalCost.add(deliveryPrice).add(tax);

        log.info("PaymentService: -> Полная стоимость заказа: {}", totalCost);
        return totalCost;
    }

    @Override
    @Transactional
    public void paymentSuccess(UUID paymentId) {
        log.info("PaymentService: -> Метод для эмуляции успешной оплаты: {}", paymentId);

        Payment payment = repository.findPaymentByPaymentId(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Заказ не найден"));
        payment.setPaymentState(PaymentState.SUCCESS);
        orderClient.payment(payment.getOrderId());
        repository.save(payment);

        log.info("PaymentService: -> Успешная оплата в платежном шлюзе: {}", paymentId);
    }

    @Override
    public BigDecimal productCost(OrderDto order) {
        log.info("PaymentService: -> Расчёт стоимости товаров в заказе: {}", order);

        Map<UUID, Long> products = order.getProducts();

        if (products == null) {
            throw new NotEnoughInfoInOrderToCalculateException();
        }

        BigDecimal totalCost = BigDecimal.ZERO;

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            ProductDto product = storeClient.getProduct(entry.getKey());
            BigDecimal productPrice = product.getPrice();
            BigDecimal total = productPrice.multiply(BigDecimal.valueOf(entry.getValue()));
            totalCost = totalCost.add(total);
        }

        log.info("PaymentService: -> Расчёт стоимости товаров в заказе: {}", totalCost);
        return totalCost;
    }

    @Override
    @Transactional
    public void paymentFailed(UUID paymentId) {
        log.info("PaymentService: -> Метод для эмуляции отказа в оплате платежного шлюза: {}", paymentId);

        Payment payment = repository.findPaymentByPaymentId(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Заказ не найден"));
        payment.setPaymentState(PaymentState.FAILED);
        orderClient.paymentFailed(payment.getOrderId());
        repository.save(payment);

        log.info("PaymentService: -> Отказ при оплате заказа: {}", paymentId);
    }

    private static void checkOrder(OrderDto order) {
        if (order.getTotalPrice() == null || order.getDeliveryPrice() == null || order.getProductPrice() == null) {
            log.error("Недостаточно информации в заказе для расчёта");
            throw new NotEnoughInfoInOrderToCalculateException();
        }
    }
}