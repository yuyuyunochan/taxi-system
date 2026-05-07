package com.taxi.notification_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_tasks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private int retryCount;

    private String lastError;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = NotificationStatus.PENDING;
        }
    }

    public void markAsProcessing() {
        this.status = NotificationStatus.PROCESSING;
    }

    public void markAsSent() {
        this.status = NotificationStatus.SENT;
    }

    public void incrementRetry() {
        this.retryCount++;
    }

    public boolean canRetry() {
        return this.retryCount < 3;
    }

    public void markAsFailed(String error) {
        this.status = NotificationStatus.FAILED;
        this.lastError = error;
    }

    public void markAsPendingForRetry() {
        this.status = NotificationStatus.PENDING;
    }
}