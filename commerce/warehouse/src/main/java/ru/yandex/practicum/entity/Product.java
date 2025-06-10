package ru.yandex.practicum.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    @Column(name = "product_id")
    UUID productId;

    @NotNull
    Boolean fragile;

    @Positive
    Double weight;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "width", column = @Column(name = "width")),
            @AttributeOverride(name = "height", column = @Column(name = "height")),
            @AttributeOverride(name = "depth", column = @Column(name = "depth"))
    })
    Dimension dimension;

    @PositiveOrZero
    Long quantity;
}