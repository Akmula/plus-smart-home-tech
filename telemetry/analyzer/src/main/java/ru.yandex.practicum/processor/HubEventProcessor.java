package ru.yandex.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.configuration.AnalyzerConfig;
import ru.yandex.practicum.configuration.ConsumerConfig;
import ru.yandex.practicum.handler.HubEventHandler;
import ru.yandex.practicum.handler.HubHandler;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class HubEventProcessor implements Runnable {

    private final KafkaConsumer<String, HubEventAvro> consumer;
    private final HubHandler hubHandler;
    private final List<String> topics;
    private final Duration pollTimeout;

    public HubEventProcessor(AnalyzerConfig config, HubHandler hubHandler) {
        this.hubHandler = hubHandler;
        final ConsumerConfig consumerConfig =
                config.getConsumers().get(this.getClass().getSimpleName());
        this.consumer = new KafkaConsumer<>(consumerConfig.getProperties());
        this.topics = consumerConfig.getTopics();
        this.pollTimeout = consumerConfig.getPollTimeout();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Сработал хук на завершение JVM. Прерываю работу консьюмера событий от хабов. ");
            consumer.wakeup();
        }));
    }

    @Override
    public void run() {
        log.info("Подписываюсь на топики {}", topics);
        consumer.subscribe(topics);
        try {
            Map<String, HubEventHandler> eventHandlers = hubHandler.getHandlers();
            while (true) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(pollTimeout);
                if (!records.isEmpty()) {
                    for (ConsumerRecord<String, HubEventAvro> record : records) {
                        HubEventAvro event = record.value();
                        String eventType = event.getPayload().getClass().getSimpleName();
                        String hubId = event.getHubId();

                        log.info("Обработка события {} для хаба {}, смещение: {}",
                                eventType, hubId, record.offset());

                        HubEventHandler handler = eventHandlers.get(eventType);
                        if (handler != null) {
                            log.debug("Найден обработчик для типа события {}", eventType);
                            handler.handle(event);
                            log.info("Событие {} для хаба {} успешно обработано",
                                    eventType, hubId);
                        } else {
                            throw new IllegalArgumentException("Не найден обработчик для типа события " + eventType);
                        }
                    }
                    consumer.commitSync();
                }
            }
        } catch (WakeupException e) {
            // завершаем работу консьюмера (в блоке final)
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от хабов", e);
        } finally {
            consumer.close();
        }
    }

}

