package com.taxi.notification_service.service;

import com.taxi.notification_service.entity.NotificationStatus;
import com.taxi.notification_service.entity.NotificationTask;
import com.taxi.notification_service.repository.NotificationTaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NotificationTaskService {

    private final NotificationTaskRepository repository;

    public NotificationTaskService(NotificationTaskRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Optional<NotificationTask> claimNextTask() {
        var list = repository.findNextByStatusForUpdate(
                NotificationStatus.PENDING,
                PageRequest.of(0, 1)
        );

        if (list.isEmpty()) return Optional.empty();

        NotificationTask task = list.get(0);
        task.markAsProcessing();
        return Optional.of(task);
    }

    @Transactional
    public void markSent(Long taskId) {
        NotificationTask task = repository.findById(taskId)
                .orElseThrow(() -> new IllegalStateException("Task not found: " + taskId));
        task.markAsSent();
        repository.save(task);
    }

    @Transactional
    public void handleFailureWithRetry(Long taskId, String error) {
        NotificationTask task = repository.findById(taskId)
                .orElseThrow(() -> new IllegalStateException("Task not found: " + taskId));

        task.incrementRetry();

        if (!task.canRetry()) {
            task.markAsFailed(error);
        } else {
            task.markAsPendingForRetry();
        }

        repository.save(task);
    }
}