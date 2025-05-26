package ru.yandex.practicum.model.hub;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.model.hub.enums.DeviceActionType;

@Getter
@Setter
@ToString(callSuper = true)
public class DeviceAction {

    @NotNull
    String sensorId;

    @NotNull
    DeviceActionType type;

    Integer value;
}