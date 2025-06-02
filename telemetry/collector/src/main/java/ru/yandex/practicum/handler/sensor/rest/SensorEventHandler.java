package ru.yandex.practicum.handler.sensor.rest;

import ru.yandex.practicum.model.sensor.AbstractSensorEvent;
import ru.yandex.practicum.model.sensor.enums.SensorEventType;

public interface SensorEventHandler {

    SensorEventType getSensorEventType();

    void handle(AbstractSensorEvent event);

}