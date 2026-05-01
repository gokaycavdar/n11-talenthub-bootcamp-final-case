package com.gokaycavdar.notificationservice.service;

import com.gokaycavdar.notificationservice.entity.Notification;
import com.gokaycavdar.notificationservice.entity.NotificationStatus;
import com.gokaycavdar.notificationservice.entity.NotificationType;
import com.gokaycavdar.notificationservice.event.PaymentFailedEvent;
import com.gokaycavdar.notificationservice.event.PaymentSucceededEvent;
import com.gokaycavdar.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void createPaymentSucceededNotification_shouldPersistNotification() {
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                1L,
                10L,
                "conv-123",
                "corr-123",
                new BigDecimal("2000.00"),
                "MOCKPAY123",
                LocalDateTime.now()
        );

        when(notificationRepository.existsByConversationIdAndType(
                "conv-123",
                NotificationType.PAYMENT_SUCCESS
        )).thenReturn(false);

        notificationService.createPaymentSucceededNotification(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertEquals(10L, saved.getUserId());
        assertEquals(1L, saved.getOrderId());
        assertEquals("conv-123", saved.getConversationId());
        assertEquals(NotificationType.PAYMENT_SUCCESS, saved.getType());
        assertEquals(NotificationStatus.CREATED, saved.getStatus());
        assertEquals("Payment completed successfully for order 1.", saved.getMessage());
    }

    @Test
    void createPaymentSucceededNotification_shouldSkipDuplicateNotification() {
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                1L,
                10L,
                "conv-123",
                "corr-123",
                new BigDecimal("2000.00"),
                "MOCKPAY123",
                LocalDateTime.now()
        );

        when(notificationRepository.existsByConversationIdAndType(
                "conv-123",
                NotificationType.PAYMENT_SUCCESS
        )).thenReturn(true);

        notificationService.createPaymentSucceededNotification(event);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void createPaymentFailedNotification_shouldPersistNotificationWithReason() {
        PaymentFailedEvent event = new PaymentFailedEvent(
                2L,
                20L,
                "conv-999",
                "corr-999",
                "Mock payment declined",
                LocalDateTime.now()
        );

        when(notificationRepository.existsByConversationIdAndType(
                "conv-999",
                NotificationType.PAYMENT_FAILED
        )).thenReturn(false);

        notificationService.createPaymentFailedNotification(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertEquals(20L, saved.getUserId());
        assertEquals(2L, saved.getOrderId());
        assertEquals("conv-999", saved.getConversationId());
        assertEquals(NotificationType.PAYMENT_FAILED, saved.getType());
        assertEquals(NotificationStatus.CREATED, saved.getStatus());
        assertEquals("Payment failed for order 2. Reason: Mock payment declined", saved.getMessage());
    }

    @Test
    void createPaymentFailedNotification_shouldUseUnknownReason_whenReasonBlank() {
        PaymentFailedEvent event = new PaymentFailedEvent(
                3L,
                30L,
                "conv-555",
                "corr-555",
                "",
                LocalDateTime.now()
        );

        when(notificationRepository.existsByConversationIdAndType(
                "conv-555",
                NotificationType.PAYMENT_FAILED
        )).thenReturn(false);

        notificationService.createPaymentFailedNotification(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertEquals("Payment failed for order 3. Reason: Unknown reason", saved.getMessage());
    }
}
