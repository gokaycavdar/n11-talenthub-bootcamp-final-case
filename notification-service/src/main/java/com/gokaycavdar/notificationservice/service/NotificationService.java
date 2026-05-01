package com.gokaycavdar.notificationservice.service;

import com.gokaycavdar.notificationservice.entity.Notification;
import com.gokaycavdar.notificationservice.entity.NotificationStatus;
import com.gokaycavdar.notificationservice.entity.NotificationType;
import com.gokaycavdar.notificationservice.event.PaymentFailedEvent;
import com.gokaycavdar.notificationservice.event.PaymentSucceededEvent;
import com.gokaycavdar.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createPaymentSucceededNotification(PaymentSucceededEvent event) {
        if (notificationRepository.existsByConversationIdAndType(
                event.conversationId(),
                NotificationType.PAYMENT_SUCCESS
        )) {
            log.info("Duplicate payment success notification skipped. orderId={}, conversationId={}",
                    event.orderId(), event.conversationId());
            return;
        }

        Notification notification = Notification.builder()
                .userId(event.userId())
                .orderId(event.orderId())
                .conversationId(event.conversationId())
                .type(NotificationType.PAYMENT_SUCCESS)
                .status(NotificationStatus.CREATED)
                .message("Payment completed successfully for order " + event.orderId() + ".")
                .build();

        notificationRepository.save(notification);

        log.info("Payment success notification created. orderId={}, userId={}, conversationId={}",
                event.orderId(), event.userId(), event.conversationId());
    }

    @Transactional
    public void createPaymentFailedNotification(PaymentFailedEvent event) {
        if (notificationRepository.existsByConversationIdAndType(
                event.conversationId(),
                NotificationType.PAYMENT_FAILED
        )) {
            log.info("Duplicate payment failed notification skipped. orderId={}, conversationId={}",
                    event.orderId(), event.conversationId());
            return;
        }

        String reason = StringUtils.hasText(event.reason()) ? event.reason() : "Unknown reason";

        Notification notification = Notification.builder()
                .userId(event.userId())
                .orderId(event.orderId())
                .conversationId(event.conversationId())
                .type(NotificationType.PAYMENT_FAILED)
                .status(NotificationStatus.CREATED)
                .message("Payment failed for order " + event.orderId() + ". Reason: " + reason)
                .build();

        notificationRepository.save(notification);

        log.info("Payment failed notification created. orderId={}, userId={}, conversationId={}",
                event.orderId(), event.userId(), event.conversationId());
    }
}
