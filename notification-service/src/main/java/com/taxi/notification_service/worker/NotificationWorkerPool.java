package com.taxi.notification_service.worker;

import com.taxi.notification_service.entity.NotificationTask;
import com.taxi.notification_service.service.NotificationTaskService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class NotificationWorkerPool {

    private static final Logger log = LoggerFactory.getLogger(NotificationWorkerPool.class);

    private final NotificationTaskService taskService;

    private ExecutorService executorService;
    private volatile boolean running = true;

    private static final int THREAD_COUNT = 3;

    public NotificationWorkerPool(NotificationTaskService taskService) {
        this.taskService = taskService;
    }

    @PostConstruct
    public void start() {
        log.info("Starting Notification Worker Pool with {} threads", THREAD_COUNT);

        executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(this::workerLoop);
        }
    }

    private void workerLoop() {
        while (running) {
            try {
                Optional<NotificationTask> optionalTask = taskService.claimNextTask();

                if (optionalTask.isEmpty()) {
                    Thread.sleep(1000);
                    continue;
                }

                NotificationTask task = optionalTask.get();

                log.info("Processing notification task id={}", task.getId());

                try {
                    simulateSend(task);
                    taskService.markSent(task.getId());
                    log.info("Task id={} successfully sent", task.getId());

                } catch (Exception sendException) {

                    log.error("Failed to send notification id={}", task.getId());

                    taskService.handleFailureWithRetry(
                            task.getId(),
                            sendException.getMessage()
                    );
                }

            } catch (Exception e) {
                log.error("Worker loop error", e);
            }
        }
    }

    private void simulateSend(NotificationTask task) throws InterruptedException {
        Thread.sleep(2000);

        if (Math.random() < 0.2) {
            throw new RuntimeException("Simulated notification failure");
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down Notification Worker Pool...");
        running = false;

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("Forcing shutdown...");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        log.info("Worker Pool stopped.");
    }
}