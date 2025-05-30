package ru.yandex.practicum.configuration;


import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Getter
@Setter
public class ConsumerConfig {
    private String type;
    private List<String> topics;
    private Duration pollTimeout;
    private Properties properties;

    public ConsumerConfig(String type, List<String> topics, Duration pollTimeout, Map<String, String> properties) {
        this.type = type;
        this.topics = topics;
        this.pollTimeout = pollTimeout;

        this.properties = new Properties(properties.size());
        this.properties.putAll(properties);
    }
}