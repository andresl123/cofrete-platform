package com.cofrete.financeworker.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
@EnableConfigurationProperties(FinanceWorkerRabbitProperties.class)
public class MessagingConfiguration {

    @Bean
    DirectExchange financeEventsExchange(FinanceWorkerRabbitProperties properties) {
        return new DirectExchange(properties.getExchange(), true, false);
    }

    @Bean
    Queue tripRecalculationQueue(FinanceWorkerRabbitProperties properties) {
        return new Queue(properties.getTripRecalculationQueue(), true);
    }

    @Bean
    Queue reserveAllocationQueue(FinanceWorkerRabbitProperties properties) {
        return new Queue(properties.getReserveAllocationQueue(), true);
    }

    @Bean
    Binding tripRecalculationBinding(
        Queue tripRecalculationQueue,
        DirectExchange financeEventsExchange,
        FinanceWorkerRabbitProperties properties
    ) {
        return BindingBuilder.bind(tripRecalculationQueue)
            .to(financeEventsExchange)
            .with(properties.getTripRecalculationRequestedRoutingKey());
    }

    @Bean
    Binding reserveAllocationBinding(
        Queue reserveAllocationQueue,
        DirectExchange financeEventsExchange,
        FinanceWorkerRabbitProperties properties
    ) {
        return BindingBuilder.bind(reserveAllocationQueue)
            .to(financeEventsExchange)
            .with(properties.getReserveAllocationRequestedRoutingKey());
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
