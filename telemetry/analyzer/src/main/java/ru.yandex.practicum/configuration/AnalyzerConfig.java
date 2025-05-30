package ru.yandex.practicum.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
@ConfigurationPropertiesScan
@ConfigurationProperties(prefix = "analyzer.kafka")
public class AnalyzerConfig {
    private final Map<String, ConsumerConfig> consumers;

    public AnalyzerConfig(Map<String, String> commonProperties, List<ConsumerConfig> consumers) {
        this.consumers = consumers
                .stream()
                .peek(config -> {
                    Properties mergedProps = new Properties();
                    mergedProps.putAll(commonProperties);
                    mergedProps.putAll(config.getProperties());
                    config.setProperties(mergedProps);
                })
                .collect(Collectors.toMap(ConsumerConfig::getType, Function.identity()));
    }
}