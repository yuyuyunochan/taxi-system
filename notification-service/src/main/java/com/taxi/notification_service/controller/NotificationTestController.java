package com.taxi.notification_service.controller;

import com.taxi.notification_service.entity.NotificationStatus;
import com.taxi.notification_service.entity.NotificationTask;
import com.taxi.notification_service.repository.NotificationTaskRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/test")
public class NotificationTestController {

    private final NotificationTaskRepository repository;

    public NotificationTestController(NotificationTaskRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/create")
    public String createTestTask() {

        NotificationTask task = new NotificationTask();
        task.setTripId(1L);
        task.setPassengerId(1L);
        task.setDriverId(1L);
        task.setMessage("Test notification");
        task.markAsPendingForRetry();

        repository.save(task);

        return "Task created with id=" + task.getId();
    }
}