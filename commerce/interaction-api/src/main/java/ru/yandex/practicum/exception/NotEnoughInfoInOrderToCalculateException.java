package ru.yandex.practicum.exception;

public class NotEnoughInfoInOrderToCalculateException extends RuntimeException {

    private static final String MSG_TEMPLATE = "Недостаточно информации в заказе для расчёта";

    public NotEnoughInfoInOrderToCalculateException() {
        super(MSG_TEMPLATE);
    }
}