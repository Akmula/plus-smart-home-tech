package ru.yandex.practicum.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.proxy.HibernateProxy;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.ProductState;
import ru.yandex.practicum.enums.QuantityState;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
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
    BigDecimal price; // Цена товара

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o)
                .getHibernateLazyInitializer()
                .getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this)
                .getHibernateLazyInitializer()
                .getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Product product = (Product) o;
        return getProductId() != null && Objects.equals(getProductId(), product.getProductId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this)
                .getHibernateLazyInitializer()
                .getPersistentClass().hashCode() : getClass().hashCode();
    }
}