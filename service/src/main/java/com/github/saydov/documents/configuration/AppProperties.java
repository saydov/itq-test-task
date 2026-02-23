package com.github.saydov.documents.configuration;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.context.annotation.Bean;

@Getter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final int batchSize;
    private final Document document;
    private final Transition transition;

    public AppProperties(
            @DefaultValue("50") int batchSize,
            @DefaultValue Document document,
            @DefaultValue Transition transition
    ) {
        this.batchSize = batchSize;
        this.document = document;
        this.transition = transition;
    }

    @Bean
    public Document document() {
        return document;
    }

    @Bean
    public Transition transition() {
        return transition;
    }

    public record Document(String numberPrefix) {

        public Document(@DefaultValue("DOC") String numberPrefix) {
            this.numberPrefix = numberPrefix;
        }
    }

    public record Transition(int batchSize, int maxConcurrency) {

        public Transition(
                @DefaultValue("50") int batchSize,
                @DefaultValue("4") int maxConcurrency
        ) {
            this.batchSize = batchSize;
            this.maxConcurrency = maxConcurrency;
        }
    }
}
