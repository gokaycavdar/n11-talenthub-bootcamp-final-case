package com.gokaycavdar.paymentservice.service;

import com.gokaycavdar.paymentservice.dto.payment.InitiatePaymentRequest;
import com.gokaycavdar.paymentservice.dto.payment.InitiatePaymentResponse;
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
import com.gokaycavdar.paymentservice.service.provider.PaymentProviderInitRequest;
import com.gokaycavdar.paymentservice.service.provider.PaymentProviderInitResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentAttemptRepository paymentAttemptRepository;
    private final PaymentEventPublisher paymentEventPublisher;
    private final List<PaymentProvider> paymentProviders;

    @Value("${payment.provider}")
    private String configuredProviderName;

    @Value("${payment.callback-url}")
    private String callbackUrl;

    @Transactional
    public InitiatePaymentResponse initiateThreeDsPayment(Long userId, InitiatePaymentRequest request) {
        PaymentProviderType providerType = PaymentProviderType.valueOf(configuredProviderName.toUpperCase());
        PaymentProvider provider = getProvider(providerType);

        log.info("3DS payment initiation started. orderId={}, userId={}, provider={}",
                request.orderId(), userId, providerType);

        String conversationId = UUID.randomUUID().toString();

        PaymentProviderInitRequest providerRequest = new PaymentProviderInitRequest(
                request.orderId(),
                userId,
                request.paidPrice(),
                conversationId,
                callbackUrl,
                request.cardHolder(),
                request.cardNumber(),
                request.expireMonth(),
                request.expireYear(),
                request.cvc()
        );

        PaymentProviderInitResult providerResult = provider.initiate3ds(providerRequest);

        PaymentAttempt paymentAttempt = PaymentAttempt.builder()
                .orderId(request.orderId())
                .userId(userId)
                .conversationId(conversationId)
                .provider(providerType)
                .status(PaymentStatus.INITIATED)
                .paidPrice(request.paidPrice())
                .build();

        paymentAttemptRepository.save(paymentAttempt);

        log.info("Payment attempt created. orderId={}, userId={}, conversationId={}, status={}",
                paymentAttempt.getOrderId(),
                paymentAttempt.getUserId(),
                paymentAttempt.getConversationId(),
                paymentAttempt.getStatus());

        return new InitiatePaymentResponse(
                conversationId,
                PaymentStatus.INITIATED.name(),
                providerResult.threeDsHtmlContent()
        );
    }

    @Transactional
    public PaymentCallbackResponse handleThreeDsCallback(ThreeDsCallbackRequest request) {
        log.info("3DS callback received. conversationId={}, status={}",
                request.getConversationId(), request.getStatus());

        PaymentAttempt paymentAttempt = paymentAttemptRepository.findByConversationId(request.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment attempt not found"));

        if (paymentAttempt.getStatus() != PaymentStatus.INITIATED) {
            log.info("Callback skipped because payment already processed. orderId={}, conversationId={}, currentStatus={}",
                    paymentAttempt.getOrderId(),
                    paymentAttempt.getConversationId(),
                    paymentAttempt.getStatus());

            return new PaymentCallbackResponse(
                    paymentAttempt.getConversationId(),
                    paymentAttempt.getStatus().name(),
                    "Callback already processed"
            );
        }

        PaymentProvider provider = getProvider(paymentAttempt.getProvider());
        PaymentProviderCallbackResult callbackResult = provider.resolveCallback(request);
        String correlationId = MDC.get("correlationId");

        if (callbackResult.successful()) {
            paymentAttempt.setStatus(PaymentStatus.SUCCESS);
            paymentAttempt.setExternalPaymentId(callbackResult.externalPaymentId());
            paymentAttempt.setFailureReason(null);
            paymentAttemptRepository.save(paymentAttempt);

            log.info("Payment marked as success. orderId={}, userId={}, conversationId={}, externalPaymentId={}",
                    paymentAttempt.getOrderId(),
                    paymentAttempt.getUserId(),
                    paymentAttempt.getConversationId(),
                    paymentAttempt.getExternalPaymentId());

            paymentEventPublisher.publishPaymentSucceeded(
                    new PaymentSucceededEvent(
                            paymentAttempt.getOrderId(),
                            paymentAttempt.getUserId(),
                            paymentAttempt.getConversationId(),
                            correlationId,
                            paymentAttempt.getPaidPrice(),
                            paymentAttempt.getExternalPaymentId(),
                            LocalDateTime.now()
                    )
            );

            return new PaymentCallbackResponse(
                    paymentAttempt.getConversationId(),
                    paymentAttempt.getStatus().name(),
                    "Payment completed successfully"
            );
        }

        paymentAttempt.setStatus(PaymentStatus.FAILED);
        paymentAttempt.setExternalPaymentId(callbackResult.externalPaymentId());
        paymentAttempt.setFailureReason(callbackResult.failureReason());
        paymentAttemptRepository.save(paymentAttempt);

        log.info("Payment marked as failed. orderId={}, userId={}, conversationId={}, reason={}",
                paymentAttempt.getOrderId(),
                paymentAttempt.getUserId(),
                paymentAttempt.getConversationId(),
                paymentAttempt.getFailureReason());

        paymentEventPublisher.publishPaymentFailed(
                new PaymentFailedEvent(
                        paymentAttempt.getOrderId(),
                        paymentAttempt.getUserId(),
                        paymentAttempt.getConversationId(),
                        correlationId,
                        paymentAttempt.getFailureReason(),
                        LocalDateTime.now()
                )
        );

        return new PaymentCallbackResponse(
                paymentAttempt.getConversationId(),
                paymentAttempt.getStatus().name(),
                paymentAttempt.getFailureReason()
        );
    }

    private PaymentProvider getProvider(PaymentProviderType providerType) {
        return paymentProviders.stream()
                .filter(provider -> provider.getProviderType() == providerType)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Payment provider not found: " + providerType));
    }
}
