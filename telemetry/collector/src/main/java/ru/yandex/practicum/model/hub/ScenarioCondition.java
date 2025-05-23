package ru.yandex.practicum.model.hub;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.model.hub.enums.ScenarioOperation;
import ru.yandex.practicum.model.hub.enums.ScenarioType;

@Getter
@Setter
@ToString(callSuper = true)
public class ScenarioCondition {
    String sensorId;
    ScenarioType type;
    ScenarioOperation operation;
    Integer value;
}