package ru.yandex.practicum.handler.sensor.rest;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.KafkaClient;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.model.sensor.AbstractSensorEvent;
import ru.yandex.practicum.model.sensor.SwitchSensorEvent;
import ru.yandex.practicum.model.sensor.enums.SensorEventType;

@Component
public class SwitchSensorEventHandler extends AbstractSensorEventHandler<SwitchSensorAvro> {

    protected SwitchSensorEventHandler(KafkaClient client) {
        super(client);
    }

    @Override
    public SwitchSensorAvro mapToSensorEventAvro(AbstractSensorEvent event) {
        SwitchSensorEvent newEvent = (SwitchSensorEvent) event;

        return SwitchSensorAvro.newBuilder()
                .setState(newEvent.getState())
                .build();
    }

    @Override
    public SensorEventType getSensorEventType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }
}