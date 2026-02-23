package com.github.saydov.documents.configuration;

import com.github.saydov.documents.enums.DocumentAction;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, DocumentAction.class,
                source -> DocumentAction.valueOf(source.toUpperCase()));
    }
}
