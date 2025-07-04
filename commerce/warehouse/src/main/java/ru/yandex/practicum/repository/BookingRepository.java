package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.entity.OrderBooking;

import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<OrderBooking, UUID> {

    Optional<OrderBooking> findBookingByOrderId(UUID orderId);

}