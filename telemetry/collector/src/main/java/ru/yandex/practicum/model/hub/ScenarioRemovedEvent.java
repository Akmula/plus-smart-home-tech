package ru.yandex.practicum.model.hub;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.model.hub.enums.HubEventType;

@Getter
@Setter
@ToString(callSuper = true)
public class ScenarioRemovedEvent extends AbstractHubEvent {

    @NotNull
    @Size(min = 3)
    String name;

    @Override
    public HubEventType getHubEventType() {
        return HubEventType.SCENARIO_REMOVED;
    }
}