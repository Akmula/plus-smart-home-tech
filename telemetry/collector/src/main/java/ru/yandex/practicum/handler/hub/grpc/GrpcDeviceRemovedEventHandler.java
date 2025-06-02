package ru.yandex.practicum.handler.hub.grpc;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.KafkaClient;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;

@Component
public class GrpcDeviceRemovedEventHandler extends AbstractGrpcHubEventHandler<DeviceRemovedEventAvro> {

    protected GrpcDeviceRemovedEventHandler(KafkaClient client) {
        super(client);
    }

    @Override
    public DeviceRemovedEventAvro mapToAvro(HubEventProto event) {
        return DeviceRemovedEventAvro.newBuilder()
                .setId(event.getDeviceRemoved().getId())
                .build();
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_REMOVED;
    }
}