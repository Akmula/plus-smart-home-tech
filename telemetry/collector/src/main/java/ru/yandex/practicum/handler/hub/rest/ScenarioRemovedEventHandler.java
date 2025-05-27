package ru.yandex.practicum.handler.hub.rest;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.KafkaClient;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.model.hub.AbstractHubEvent;
import ru.yandex.practicum.model.hub.ScenarioRemovedEvent;
import ru.yandex.practicum.model.hub.enums.HubEventType;

@Component
public class ScenarioRemovedEventHandler extends AbstractHubEventHandler<ScenarioRemovedEventAvro> {

    protected ScenarioRemovedEventHandler(KafkaClient client) {
        super(client);
    }

    @Override
    public ScenarioRemovedEventAvro mapToHubEventAvro(AbstractHubEvent event) {
        ScenarioRemovedEvent newEvent = (ScenarioRemovedEvent) event;

        return ScenarioRemovedEventAvro.newBuilder()
                .setName(newEvent.getName())
                .build();
    }

    @Override
    public HubEventType getHubEventType() {
        return HubEventType.SCENARIO_REMOVED;
    }
}