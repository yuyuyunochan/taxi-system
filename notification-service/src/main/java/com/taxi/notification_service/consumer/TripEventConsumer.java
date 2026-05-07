package com.taxi.notification_service.consumer;

import com.taxi.notification_service.config.RabbitMQConfig;
import com.taxi.notification_service.dto.TripCreatedEvent;
import com.taxi.notification_service.dto.TripStatusChangedEvent;
import com.taxi.notification_service.service.NotificationTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TripEventConsumer {

    private final NotificationTaskService taskService;

    @RabbitListener(queues = RabbitMQConfig.TRIP_CREATED_QUEUE)
    public void handleTripCreated(TripCreatedEvent event) {
        log.info("RabbitMQ: Got event trip created #{}", event.getTripId());

        String message = String.format(
                "Your trip #%d created. Driver #%d in the way yet. Price: %s",
                event.getTripId(),
                event.getDriverId(),
                event.getPrice()
        );

        taskService.createTask(event.getPassengerId(), message);
    }

    @RabbitListener(queues = RabbitMQConfig.TRIP_STATUS_QUEUE)
    public void handleStatusChange(TripStatusChangedEvent event) {
        log.info("RabbitMQ: Trip status #{} changed to {}",
                event.getTripId(),
                event.getNewStatus());

        String message = String.format(
                "Status of your trip #%d changed to: %s",
                event.getTripId(),
                event.getNewStatus()
        );

        taskService.createTask(event.getPassengerId(), message);
        if (event.getDriverId() != null) {
            taskService.createTask(event.getDriverId(), message);
        }
    }
}