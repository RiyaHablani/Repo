package com.hashedin.huspark.repository;

import com.hashedin.huspark.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Notification> findByUserIdAndTypeAndDeliveryStatus(Long userId, Notification.NotificationType type, Notification.DeliveryStatus status);

    @Query("SELECT n FROM Notification n WHERE n.deliveryStatus = 'PENDING' AND n.type = 'OVERDUE_BOOK'")
    List<Notification> findPendingOverdueNotifications();

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.type = :type AND n.createdAt >= :since")
    List<Notification> findByUserIdAndTypeAndCreatedAtSince(
            @Param("userId") Long userId,
            @Param("type") Notification.NotificationType type,
            @Param("since") LocalDateTime since);

    @Query("SELECT n FROM Notification n WHERE n.deliveryStatus = 'PENDING' AND n.deliveryMethod = :method")
    List<Notification> findPendingNotificationsByMethod(@Param("method") Notification.DeliveryMethod method);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.type = 'OVERDUE_BOOK' AND n.createdAt >= :since")
    long countOverdueNotificationsForUserSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
