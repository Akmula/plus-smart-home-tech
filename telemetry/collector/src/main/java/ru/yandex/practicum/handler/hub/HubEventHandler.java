package ru.yandex.practicum.handler.hub;

import ru.yandex.practicum.model.hub.AbstractHubEvent;
import ru.yandex.practicum.model.hub.enums.HubEventType;

public interface HubEventHandler {

    HubEventType getHubEventType();

    void handle(AbstractHubEvent event);

}