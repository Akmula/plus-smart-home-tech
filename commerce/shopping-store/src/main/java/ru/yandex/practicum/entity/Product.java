package ru.yandex.practicum.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.ProductState;
import ru.yandex.practicum.enums.QuantityState;

import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_id", nullable = false)
    UUID productId;

    @Column(name = "product_name", nullable = false)
    String productName;

    @Column(name = "description", nullable = false)
    String description;

    @Column(name = "image_src", nullable = false)
    String imageSrc; // Ссылка на картинку во внешнем хранилище или SVG

    @Enumerated(EnumType.STRING)
    @Column(name = "quantity_state")
    QuantityState quantityState; // Статус, перечисляющий состояние остатка как свойства товара

    @Enumerated(EnumType.STRING)
    @Column(name = "product_state")
    ProductState productState; // Статус товара

    @Enumerated(EnumType.STRING)
    @Column(name = "product_category")
    ProductCategory productCategory; // Категория товара

    @Min(1)
    @Column(name = "price", nullable = false)
    double price; // Цена товара

}