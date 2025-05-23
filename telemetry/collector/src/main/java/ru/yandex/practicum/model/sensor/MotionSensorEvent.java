package ru.yandex.practicum.model.sensor;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.model.sensor.enums.SensorEventType;

@Getter
@Setter
@ToString(callSuper = true)
public class MotionSensorEvent extends AbstractSensorEvent {

    @NotNull
    Integer linkQuality;

    @NotNull
    Boolean motion;

    @NotNull
    Integer voltage;

    @Override
    public SensorEventType getSensorEventType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }
}