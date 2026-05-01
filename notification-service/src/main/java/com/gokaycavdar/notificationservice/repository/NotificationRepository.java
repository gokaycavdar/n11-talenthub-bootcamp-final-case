package com.gokaycavdar.notificationservice.repository;

import com.gokaycavdar.notificationservice.entity.Notification;
import com.gokaycavdar.notificationservice.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByConversationIdAndType(String conversationId, NotificationType type);
}
