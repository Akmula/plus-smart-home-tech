package ru.yandex.practicum;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

@Slf4j
@GrpcService
public class HubRouterController extends HubRouterControllerGrpc.HubRouterControllerImplBase {
    @Override
    public void handleDeviceAction(DeviceActionRequest request, StreamObserver<Empty> responseObserver) {
        log.info("Получен запрос на действие с устройством: hubId={}, action={}",
                request.getHubId(),
                request.getAction());

        try {
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
            log.info("Действие выполнено успешно");
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
            log.debug("Детали ошибки:", e);
        }
    }
}