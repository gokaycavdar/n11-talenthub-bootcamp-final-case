package com.gokaycavdar.notificationservice.service;

import com.gokaycavdar.notificationservice.event.PaymentFailedEvent;
import com.gokaycavdar.notificationservice.event.PaymentSucceededEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentEventNotificationListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentEventNotificationListener listener;

    @Test
    void handlePaymentSucceeded_shouldDelegateToNotificationServiceAndClearMdc() {
        PaymentSucceededEvent event = new PaymentSucceededEvent(
                1L,
                10L,
                "conv-123",
                "corr-123",
                new BigDecimal("2000.00"),
                "MOCKPAY123",
                LocalDateTime.now()
        );

        listener.handlePaymentSucceeded(event);

        verify(notificationService).createPaymentSucceededNotification(event);
        assertNull(MDC.get("correlationId"));
    }

    @Test
    void handlePaymentFailed_shouldDelegateToNotificationServiceAndClearMdc() {
        PaymentFailedEvent event = new PaymentFailedEvent(
                2L,
                20L,
                "conv-999",
                "corr-999",
                "Mock payment declined",
                LocalDateTime.now()
        );

        listener.handlePaymentFailed(event);

        verify(notificationService).createPaymentFailedNotification(event);
        assertNull(MDC.get("correlationId"));
    }
}
