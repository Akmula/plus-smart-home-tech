package ru.yandex.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.configuration.AnalyzerConfig;
import ru.yandex.practicum.configuration.ConsumerConfig;
import ru.yandex.practicum.handler.SnapshotHandler;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor implements Runnable {

    // Хранит текущий обработанный оффсет для каждой партиции и топика
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    private final Consumer<String, SensorsSnapshotAvro> consumer;
    private final SnapshotHandler snapshotHandler;
    private final List<String> topics;
    private final Duration pollTimeout;

    @Autowired
    public SnapshotProcessor(AnalyzerConfig config, SnapshotHandler snapshotHandler) {
        this.snapshotHandler = snapshotHandler;

        final ConsumerConfig consumerConfig =
                config.getConsumers().get(this.getClass().getSimpleName());

        this.consumer = new KafkaConsumer(consumerConfig.getProperties());
        this.topics = consumerConfig.getTopics();
        this.pollTimeout = consumerConfig.getPollTimeout();

      /*  Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Сработал хук на завершение JVM. Прерываю работу консьюмера событий от хабов. ");
            consumer.wakeup();
        }));*/
    }

    @Override
      public void run() {
        log.info("Подписываюсь на топики {}", topics);
        consumer.subscribe(topics);
        try {
            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(pollTimeout);

                if (!records.isEmpty()) {
                    int count = 0;
                    for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                        SensorsSnapshotAvro snapshotAvro = record.value();
                        log.info("Получен снимок умного дома: {}", snapshotAvro);
                        snapshotHandler.buildSnapshot(snapshotAvro);

                        manageOffsets(record, count++);
                    }

                    consumer.commitSync();
                }
            }
        } catch (WakeupException e) {
            // завершаем работу консьюмера (в блоке final)
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от хабов", e);
        } finally {
            try {
            consumer.commitSync(currentOffsets);
        } finally {
            log.info("Закрываем консьюмер");
            consumer.close();
        }
        }
    }

    private void manageOffsets(ConsumerRecord<?, ?> record, int count) {
        // обновляем текущий оффсет для топика-партиции
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if(count % 100 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if(exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }
}