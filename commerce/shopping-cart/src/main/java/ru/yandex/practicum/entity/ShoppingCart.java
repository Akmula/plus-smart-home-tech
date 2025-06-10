package ru.yandex.practicum.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cart")
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cart_id", nullable = false)
    UUID shoppingCartId;

    @Column(name = "user_name", nullable = false)
    String username;

    @Column(name = "active", nullable = false)
    Boolean active;

    @ElementCollection
    @Column(name = "quantity")
    @MapKeyColumn(name = "product_id")
    @CollectionTable(name = "cart_products", joinColumns = @JoinColumn(name = "cart_id"))
    Map<UUID, Long> products;
}