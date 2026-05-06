package com.taxi.notification_service.repository;

import com.taxi.notification_service.entity.NotificationStatus;
import com.taxi.notification_service.entity.NotificationTask;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select n from NotificationTask n
           where n.status = :status
           order by n.createdAt asc
           """)
    List<NotificationTask> findNextByStatusForUpdate(@Param("status") NotificationStatus status,
                                                     Pageable pageable);
}