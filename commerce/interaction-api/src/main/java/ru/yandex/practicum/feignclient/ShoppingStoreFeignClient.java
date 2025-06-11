package ru.yandex.practicum.feignclient;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.Pageable;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.request.SetProductQuantityStateRequest;

import java.util.UUID;

@FeignClient(name = "shopping-store", path = "/api/v1/shopping-store")
public interface ShoppingStoreFeignClient {

    @PutMapping
    ProductDto createNewProduct(@RequestBody @Valid ProductDto productDto);

    @PostMapping
    ProductDto updateProduct(@RequestBody @Valid ProductDto productDto);

    @GetMapping("/{productId}")
    ProductDto getProduct(@PathVariable UUID productId);

    @GetMapping
    Page<ProductDto> getProducts(@RequestParam("category") ProductCategory category,
                                 @SpringQueryMap Pageable pageable);

    @PostMapping("/removeProductFromStore")
    boolean removeProductFromStore(@RequestBody UUID productId);

    @PostMapping("/quantityState")
    boolean setProductQuantityState(@ModelAttribute SetProductQuantityStateRequest request);
}