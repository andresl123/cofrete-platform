package com.cofrete.coreapi.trip;

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
@EnableConfigurationProperties(CoreApiRabbitProperties.class)
class TripMessagingConfiguration {

    @Bean
    DirectExchange financeEventsExchange(CoreApiRabbitProperties properties) {
        return new DirectExchange(properties.getExchange(), true, false);
    }

    @Bean
    Queue reserveAllocationCompletedQueue(CoreApiRabbitProperties properties) {
        return new Queue(properties.getReserveAllocationCompletedQueue(), true);
    }

    @Bean
    Binding reserveAllocationCompletedBinding(
        Queue reserveAllocationCompletedQueue,
        DirectExchange financeEventsExchange,
        CoreApiRabbitProperties properties
    ) {
        return BindingBuilder.bind(reserveAllocationCompletedQueue)
            .to(financeEventsExchange)
            .with(properties.getReserveAllocationCompletedRoutingKey());
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
