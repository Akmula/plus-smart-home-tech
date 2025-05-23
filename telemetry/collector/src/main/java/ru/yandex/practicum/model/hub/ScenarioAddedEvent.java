package ru.yandex.practicum.model.hub;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.model.hub.enums.HubEventType;

import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
public class ScenarioAddedEvent extends AbstractHubEvent {

    @NotNull
    @Size(min = 3)
    String name;

    @NotEmpty
    List<ScenarioCondition> conditions;

    @NotEmpty
    List<DeviceAction> actions;

    @Override
    public HubEventType getHubEventType() {
        return HubEventType.SCENARIO_ADDED;
    }
}