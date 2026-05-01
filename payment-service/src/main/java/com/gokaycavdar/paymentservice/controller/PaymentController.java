package com.gokaycavdar.paymentservice.controller;

import com.gokaycavdar.paymentservice.dto.payment.InitiatePaymentRequest;
import com.gokaycavdar.paymentservice.dto.payment.InitiatePaymentResponse;
import com.gokaycavdar.paymentservice.dto.payment.PaymentCallbackResponse;
import com.gokaycavdar.paymentservice.dto.payment.ThreeDsCallbackRequest;
import com.gokaycavdar.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/internal/3ds/init")
    public ResponseEntity<InitiatePaymentResponse> initiateThreeDsPayment(
            Authentication authentication,
            @Valid @RequestBody InitiatePaymentRequest request
    ) {
        return ResponseEntity.ok(
                paymentService.initiateThreeDsPayment(getUserId(authentication), request)
        );
    }

    @PostMapping("/3ds/callback")
    public ResponseEntity<PaymentCallbackResponse> handleThreeDsCallback(
            @Valid @ModelAttribute ThreeDsCallbackRequest request
    ) {
        return ResponseEntity.ok(paymentService.handleThreeDsCallback(request));
    }

    private Long getUserId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
