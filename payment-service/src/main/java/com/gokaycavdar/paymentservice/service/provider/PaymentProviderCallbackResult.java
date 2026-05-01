package com.gokaycavdar.paymentservice.service.provider;

public record PaymentProviderCallbackResult(
        boolean successful,
        String externalPaymentId,
        String failureReason
) {
}
