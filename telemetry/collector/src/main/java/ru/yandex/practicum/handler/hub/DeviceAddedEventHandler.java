package ru.yandex.practicum.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.KafkaClient;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.model.hub.AbstractHubEvent;
import ru.yandex.practicum.model.hub.DeviceAddedEvent;
import ru.yandex.practicum.model.hub.enums.HubEventType;

@Component
public class DeviceAddedEventHandler extends AbstractHubEventHandler<DeviceAddedEventAvro> {

    protected DeviceAddedEventHandler(KafkaClient client) {
        super(client);
    }

    @Override
    public DeviceAddedEventAvro mapToHubEventAvro(AbstractHubEvent event) {
        DeviceAddedEvent newEvent = (DeviceAddedEvent) event;
        DeviceTypeAvro deviceTypeAvro = DeviceTypeAvro.valueOf(newEvent.getDeviceType().name());

        return DeviceAddedEventAvro.newBuilder()
                .setId(newEvent.getId())
                .setType(deviceTypeAvro)
                .build();
    }

    @Override
    public HubEventType getHubEventType() {
        return HubEventType.DEVICE_ADDED;
    }
}