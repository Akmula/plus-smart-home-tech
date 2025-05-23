package ru.yandex.practicum.model.sensor;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.model.sensor.enums.SensorEventType;

@Getter
@Setter
@ToString(callSuper = true)
public class ClimateSensorEvent extends AbstractSensorEvent {

    @NotNull
    Integer temperatureC;

    @NotNull
    Integer humidity;

    @NotNull
    Integer co2Level;

    @Override
    public SensorEventType getSensorEventType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }
}