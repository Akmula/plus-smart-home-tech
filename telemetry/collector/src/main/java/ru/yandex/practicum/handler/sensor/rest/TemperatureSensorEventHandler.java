package ru.yandex.practicum.handler.sensor.rest;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.KafkaClient;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.model.sensor.AbstractSensorEvent;
import ru.yandex.practicum.model.sensor.TemperatureSensorEvent;
import ru.yandex.practicum.model.sensor.enums.SensorEventType;

@Component
public class TemperatureSensorEventHandler extends AbstractSensorEventHandler<TemperatureSensorAvro> {

    protected TemperatureSensorEventHandler(KafkaClient client) {
        super(client);
    }

    @Override
    public TemperatureSensorAvro mapToSensorEventAvro(AbstractSensorEvent event) {
        TemperatureSensorEvent newEvent = (TemperatureSensorEvent) event;

        return TemperatureSensorAvro.newBuilder()
                .setTemperatureC(newEvent.getTemperatureC())
                .setTemperatureF(newEvent.getTemperatureF())
                .build();
    }

    @Override
    public SensorEventType getSensorEventType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }
}