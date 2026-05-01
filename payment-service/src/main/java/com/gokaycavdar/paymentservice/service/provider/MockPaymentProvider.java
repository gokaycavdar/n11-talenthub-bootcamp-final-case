package com.gokaycavdar.paymentservice.service.provider;

import com.gokaycavdar.paymentservice.dto.payment.ThreeDsCallbackRequest;
import com.gokaycavdar.paymentservice.entity.PaymentProviderType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class MockPaymentProvider implements PaymentProvider {

    @Override
    public PaymentProviderType getProviderType() {
        return PaymentProviderType.MOCK;
    }

    @Override
    public PaymentProviderInitResult initiate3ds(PaymentProviderInitRequest request) {
        boolean successful = !request.cardNumber().endsWith("0000");
        String status = successful ? "SUCCESS" : "FAILURE";
        String mdStatus = successful ? "1" : "0";
        String paymentId = "MOCK-" + request.conversationId().replace("-", "").substring(0, 12).toUpperCase();
        String reason = successful ? "" : "Mock payment declined";

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Mock 3DS</title>
                </head>
                <body onload="document.forms[0].submit()">
                    <form method="post" action="%s">
                        <input type="hidden" name="conversationId" value="%s" />
                        <input type="hidden" name="status" value="%s" />
                        <input type="hidden" name="paymentId" value="%s" />
                        <input type="hidden" name="conversationData" value="" />
                        <input type="hidden" name="mdStatus" value="%s" />
                        <input type="hidden" name="reason" value="%s" />
                        <noscript>
                            <button type="submit">Complete Payment</button>
                        </noscript>
                    </form>
                </body>
                </html>
                """.formatted(
                request.callbackUrl(),
                request.conversationId(),
                status,
                paymentId,
                mdStatus,
                reason
        );

        return new PaymentProviderInitResult(html);
    }

    @Override
    public PaymentProviderCallbackResult resolveCallback(ThreeDsCallbackRequest request) {
        boolean successful = "SUCCESS".equalsIgnoreCase(request.getStatus());

        String failureReason = successful
                ? null
                : (StringUtils.hasText(request.getReason()) ? request.getReason() : "Mock payment failed");

        return new PaymentProviderCallbackResult(
                successful,
                request.getPaymentId(),
                failureReason
        );
    }
}
