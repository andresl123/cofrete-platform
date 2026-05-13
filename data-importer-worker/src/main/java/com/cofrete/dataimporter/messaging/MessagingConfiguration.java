package com.cofrete.dataimporter.messaging;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
@EnableConfigurationProperties(DataImporterRabbitProperties.class)
public class MessagingConfiguration {

    @Bean
    DirectExchange importEventsExchange(DataImporterRabbitProperties properties) {
        return new DirectExchange(properties.getExchange(), true, false);
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
