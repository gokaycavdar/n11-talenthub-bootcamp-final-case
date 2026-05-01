package com.gokaycavdar.paymentservice.service;

import com.gokaycavdar.paymentservice.dto.payment.InitiatePaymentRequest;
import com.gokaycavdar.paymentservice.dto.payment.InitiatePaymentResponse;
import com.gokaycavdar.paymentservice.dto.payment.PaymentBasketItemRequest;
import com.gokaycavdar.paymentservice.dto.payment.PaymentCallbackResponse;
import com.gokaycavdar.paymentservice.dto.payment.ThreeDsCallbackRequest;
import com.gokaycavdar.paymentservice.entity.PaymentAttempt;
import com.gokaycavdar.paymentservice.entity.PaymentProviderType;
import com.gokaycavdar.paymentservice.entity.PaymentStatus;
import com.gokaycavdar.paymentservice.event.PaymentFailedEvent;
import com.gokaycavdar.paymentservice.event.PaymentSucceededEvent;
import com.gokaycavdar.paymentservice.exception.ResourceNotFoundException;
import com.gokaycavdar.paymentservice.repository.PaymentAttemptRepository;
import com.gokaycavdar.paymentservice.service.provider.PaymentProvider;
import com.gokaycavdar.paymentservice.service.provider.PaymentProviderCallbackResult;
import com.gokaycavdar.paymentservice.service.provider.PaymentProviderInitResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentAttemptRepository paymentAttemptRepository;

    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    @Mock
    private PaymentProvider paymentProvider;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "configuredProviderName", "MOCK");
        ReflectionTestUtils.setField(
                paymentService,
                "callbackUrl",
                "http://localhost:8080/api/v1/payments/3ds/callback"
        );
        ReflectionTestUtils.setField(paymentService, "paymentProviders", List.of(paymentProvider));
    }

    @Test
    void initiateThreeDsPayment_shouldCreatePaymentAttemptAndReturnHtml() {
        when(paymentProvider.getProviderType()).thenReturn(PaymentProviderType.MOCK);

        InitiatePaymentRequest request = new InitiatePaymentRequest(
                1L,
                new BigDecimal("1000.00"),
                new BigDecimal("1000.00"),
                "Gokay",
                "Cavdar",
                "gokay@example.com",
                "Gokay Cavdar",
                "Ataturk Caddesi No 10",
                "Istanbul",
                "Kadikoy",
                "34710",
                "Gokay Cavdar",
                "5555444433331111",
                "12",
                "30",
                "123",
                List.of(
                        new PaymentBasketItemRequest(
                                10L,
                                "iPhone 15",
                                new BigDecimal("1000.00"),
                                1,
                                new BigDecimal("1000.00")
                        )
                )
        );

        when(paymentProvider.initiate3ds(any()))
                .thenReturn(new PaymentProviderInitResult("<html>mock-3ds</html>"));
        when(paymentAttemptRepository.save(any(PaymentAttempt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InitiatePaymentResponse response = paymentService.initiateThreeDsPayment(1L, request);

        assertEquals(PaymentStatus.INITIATED.name(), response.status());
        assertEquals("<html>mock-3ds</html>", response.threeDsHtmlContent());
        assertNotNull(response.conversationId());

        ArgumentCaptor<PaymentAttempt> captor = ArgumentCaptor.forClass(PaymentAttempt.class);
        verify(paymentAttemptRepository).save(captor.capture());

        PaymentAttempt savedAttempt = captor.getValue();
        assertEquals(1L, savedAttempt.getOrderId());
        assertEquals(1L, savedAttempt.getUserId());
        assertEquals(PaymentProviderType.MOCK, savedAttempt.getProvider());
        assertEquals(PaymentStatus.INITIATED, savedAttempt.getStatus());
        assertEquals(0, savedAttempt.getPaidPrice().compareTo(new BigDecimal("1000.00")));
    }

    @Test
    void handleThreeDsCallback_shouldMarkSuccessAndPublishEvent() {
        when(paymentProvider.getProviderType()).thenReturn(PaymentProviderType.MOCK);

        PaymentAttempt paymentAttempt = PaymentAttempt.builder()
                .orderId(1L)
                .userId(1L)
                .conversationId("conv-123")
                .provider(PaymentProviderType.MOCK)
                .status(PaymentStatus.INITIATED)
                .paidPrice(new BigDecimal("1000.00"))
                .build();

        ThreeDsCallbackRequest request = new ThreeDsCallbackRequest(
                "conv-123",
                "SUCCESS",
                "MOCKPAY123",
                null,
                "1",
                null
        );

        when(paymentAttemptRepository.findByConversationId("conv-123")).thenReturn(Optional.of(paymentAttempt));
        when(paymentProvider.resolveCallback(request))
                .thenReturn(new PaymentProviderCallbackResult(true, "MOCKPAY123", null));
        when(paymentAttemptRepository.save(any(PaymentAttempt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentCallbackResponse response = paymentService.handleThreeDsCallback(request);

        assertEquals(PaymentStatus.SUCCESS.name(), response.status());
        assertEquals("Payment completed successfully", response.message());
        assertEquals(PaymentStatus.SUCCESS, paymentAttempt.getStatus());
        assertEquals("MOCKPAY123", paymentAttempt.getExternalPaymentId());

        verify(paymentEventPublisher).publishPaymentSucceeded(any(PaymentSucceededEvent.class));
        verify(paymentEventPublisher, never()).publishPaymentFailed(any());
    }

    @Test
    void handleThreeDsCallback_shouldMarkFailedAndPublishEvent() {
        when(paymentProvider.getProviderType()).thenReturn(PaymentProviderType.MOCK);

        PaymentAttempt paymentAttempt = PaymentAttempt.builder()
                .orderId(2L)
                .userId(1L)
                .conversationId("conv-999")
                .provider(PaymentProviderType.MOCK)
                .status(PaymentStatus.INITIATED)
                .paidPrice(new BigDecimal("750.00"))
                .build();

        ThreeDsCallbackRequest request = new ThreeDsCallbackRequest(
                "conv-999",
                "FAILURE",
                "MOCKPAY999",
                null,
                "0",
                "Mock payment declined"
        );

        when(paymentAttemptRepository.findByConversationId("conv-999")).thenReturn(Optional.of(paymentAttempt));
        when(paymentProvider.resolveCallback(request))
                .thenReturn(new PaymentProviderCallbackResult(false, "MOCKPAY999", "Mock payment declined"));
        when(paymentAttemptRepository.save(any(PaymentAttempt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentCallbackResponse response = paymentService.handleThreeDsCallback(request);

        assertEquals(PaymentStatus.FAILED.name(), response.status());
        assertEquals("Mock payment declined", response.message());
        assertEquals(PaymentStatus.FAILED, paymentAttempt.getStatus());
        assertEquals("Mock payment declined", paymentAttempt.getFailureReason());

        verify(paymentEventPublisher).publishPaymentFailed(any(PaymentFailedEvent.class));
        verify(paymentEventPublisher, never()).publishPaymentSucceeded(any());
    }

    @Test
    void handleThreeDsCallback_shouldReturnAlreadyProcessed_whenStatusIsNotInitiated() {
        PaymentAttempt paymentAttempt = PaymentAttempt.builder()
                .orderId(1L)
                .userId(1L)
                .conversationId("conv-123")
                .provider(PaymentProviderType.MOCK)
                .status(PaymentStatus.SUCCESS)
                .paidPrice(new BigDecimal("1000.00"))
                .build();

        ThreeDsCallbackRequest request = new ThreeDsCallbackRequest(
                "conv-123",
                "SUCCESS",
                "MOCKPAY123",
                null,
                "1",
                null
        );

        when(paymentAttemptRepository.findByConversationId("conv-123")).thenReturn(Optional.of(paymentAttempt));

        PaymentCallbackResponse response = paymentService.handleThreeDsCallback(request);

        assertEquals(PaymentStatus.SUCCESS.name(), response.status());
        assertEquals("Callback already processed", response.message());

        verify(paymentAttemptRepository, never()).save(any());
        verify(paymentEventPublisher, never()).publishPaymentSucceeded(any());
        verify(paymentEventPublisher, never()).publishPaymentFailed(any());
    }

    @Test
    void handleThreeDsCallback_shouldThrow_whenAttemptNotFound() {
        ThreeDsCallbackRequest request = new ThreeDsCallbackRequest(
                "missing-conv",
                "SUCCESS",
                "MOCKPAY123",
                null,
                "1",
                null
        );

        when(paymentAttemptRepository.findByConversationId("missing-conv")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.handleThreeDsCallback(request)
        );

        assertEquals("Payment attempt not found", exception.getMessage());
    }
}
