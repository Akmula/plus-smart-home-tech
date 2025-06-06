package ru.yandex.practicum.handler.sensor.grpc;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

public interface GrpcSensorEventHandler {

    SensorEventProto.PayloadCase getMessageType();

    void handle(SensorEventProto event);

}