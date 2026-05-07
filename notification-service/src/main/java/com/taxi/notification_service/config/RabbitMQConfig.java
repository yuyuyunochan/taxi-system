package com.taxi.notification_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "trip.events";

    public static final String TRIP_CREATED_QUEUE = "notification.trip.created.queue";
    public static final String TRIP_STATUS_QUEUE = "notification.trip.status.queue";

    @Bean
    public TopicExchange tripExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue tripCreatedQueue() {
        return QueueBuilder.durable(TRIP_CREATED_QUEUE).build();
    }

    @Bean
    public Queue tripStatusQueue() {
        return QueueBuilder.durable(TRIP_STATUS_QUEUE).build();
    }

    @Bean
    public Binding tripCreatedBinding(Queue tripCreatedQueue, TopicExchange tripExchange) {
        return BindingBuilder.bind(tripCreatedQueue)
                .to(tripExchange)
                .with("trip.created");
    }

    @Bean
    public Binding tripStatusBinding(Queue tripStatusQueue, TopicExchange tripExchange) {
        return BindingBuilder.bind(tripStatusQueue)
                .to(tripExchange)
                .with("trip.status.changed");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}