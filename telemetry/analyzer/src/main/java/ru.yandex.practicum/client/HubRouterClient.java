package ru.yandex.practicum.client;

import com.google.protobuf.Timestamp;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.model.Action;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubRouterClient {
/*
    @GrpcClient("hub-router")
   HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouter;

    public void sendRequest(Action action) {
        try {
            DeviceActionRequest request = toDeviceActionRequest(action);
            log.warn("Условия выполнены, отправляем команду: {}", action);
            hubRouter.handleDeviceAction(request);
            log.info("Команда успешно отправлена через gRPC");
        } catch (Exception e) {
            log.error("Ошибка при отправке команды в gRPC-сервис: {}", e.getMessage(), e);
        }
    }

    public DeviceActionRequest toDeviceActionRequest(Action action) {
        Instant instant = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();

        return DeviceActionRequest.newBuilder()
                .setHubId(action.getScenario().getHubId())
                .setScenarioName(action.getScenario().getName())
                .setAction(DeviceActionProto.newBuilder()
                        .setSensorId(action.getSensor().getId())
                        .setType(ActionTypeProto.valueOf(action.getType().name()))
                        .setValue(action.getValue())
                        .build())
                .setTimestamp(timestamp)
                .build();
    }*/

    @GrpcClient("hub-router")
    HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouter;

   /* private final HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;
    public HubRouterClient(
            @GrpcClient("hub-router")
                           HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient) {
        this.hubRouterClient = hubRouterClient;
        log.info("gRPC клиент для HubRouter успешно инициализирован");
    }*/

    public void sendRequest(Action action) {
        try {
            DeviceActionRequest deviceActionRequest = buildActionRequest(action);

            log.info("Отправка действия в HubRouter: [Хаб: {}, Сценарий: {}, Датчик: {}, Тип: {}, Значение: {}]",
                    action.getScenario().getHubId(),
                    action.getScenario().getName(),
                    action.getSensor().getId(),
                    action.getType(),
                    action.getValue());

            hubRouter.handleDeviceAction(deviceActionRequest);
            log.info("Действие успешно отправлено в HubRouter");
        } catch (StatusRuntimeException e) {
            log.error("Ошибка при отправке действия в HubRouter. Статус: {}, Описание: {}",
                    e.getStatus().getCode(),
                    e.getStatus().getDescription(), e);
            throw new RuntimeException("Не удалось отправить действие в HubRouter", e);
        }
    }

    private DeviceActionRequest buildActionRequest(Action action) {
        return DeviceActionRequest.newBuilder()
                .setHubId(action.getScenario().getHubId())
                .setScenarioName(action.getScenario().getName())
                .setAction(DeviceActionProto.newBuilder()
                        .setSensorId(action.getSensor().getId())
                        .setType(actionTypeProto(action.getType()))
                        .setValue(action.getValue())
                        .build())
                .setTimestamp(setTimestamp())
                .build();
    }

    private ActionTypeProto actionTypeProto(ActionTypeAvro actionType) {
        return switch (actionType) {
            case ACTIVATE -> ActionTypeProto.ACTIVATE;
            case DEACTIVATE -> ActionTypeProto.DEACTIVATE;
            case INVERSE -> ActionTypeProto.INVERSE;
            case SET_VALUE -> ActionTypeProto.SET_VALUE;
        };
    }

    private Timestamp setTimestamp() {
        Instant instant = Instant.now();
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}