package com.taxi.trip_service.service;

import com.taxi.trip_service.dto.event.TripCreatedEvent;
import com.taxi.trip_service.dto.event.TripStatusChangedEvent;
import com.taxi.trip_service.entity.Trip;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TripEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "trip.events";

    public void publishTripCreated(Trip trip) {

        TripCreatedEvent event = TripCreatedEvent.builder()
                .tripId(trip.getId())
                .passengerId(trip.getPassengerId())
                .driverId(trip.getDriverId())
                .price(trip.getPrice())
                .createdAt(trip.getCreatedAt())
                .build();

        send("trip.created", event);
    }

    public void publishTripStatusChanged(Trip trip, String oldStatus) {

        TripStatusChangedEvent event = TripStatusChangedEvent.builder()
                .tripId(trip.getId())
                .passengerId(trip.getPassengerId())
                .driverId(trip.getDriverId())
                .oldStatus(oldStatus)
                .newStatus(trip.getStatus().name())
                .changedAt(LocalDateTime.now())
                .build();

        send("trip.status.changed", event);
    }

    private void send(String routingKey, Object event) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, routingKey, event);
            log.info("Event sent: {}", routingKey);
        } catch (AmqpException e) {
            log.error("Failed to publish event: {}", e.getMessage());
        }
    }
}