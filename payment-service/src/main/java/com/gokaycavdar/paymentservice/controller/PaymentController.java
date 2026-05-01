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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment initialization and 3DS callback endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/internal/3ds/init")
    @Operation(
            summary = "Initiate 3DS payment",
            description = "Starts mock/provider 3DS payment flow for authenticated user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<InitiatePaymentResponse> initiateThreeDsPayment(
            Authentication authentication,
            @Valid @RequestBody InitiatePaymentRequest request
    ) {
        return ResponseEntity.ok(
                paymentService.initiateThreeDsPayment(getUserId(authentication), request)
        );
    }

    @PostMapping("/3ds/callback")
    @Operation(
            summary = "3DS callback",
            description = "Public callback endpoint used by payment provider after 3DS flow"
    )
    public ResponseEntity<PaymentCallbackResponse> handleThreeDsCallback(
            @Valid @ModelAttribute ThreeDsCallbackRequest request
    ) {
        return ResponseEntity.ok(paymentService.handleThreeDsCallback(request));
    }

    private Long getUserId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
