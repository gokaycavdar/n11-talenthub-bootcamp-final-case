package com.gokaycavdar.orderservice.client;

import com.gokaycavdar.orderservice.dto.payment.PaymentInitiateRequest;
import com.gokaycavdar.orderservice.dto.payment.PaymentInitiateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "PAYMENT-SERVICE", path = "/api/v1/payments")
public interface PaymentClient {

    @PostMapping("/internal/3ds/init")
    PaymentInitiateResponse initiateThreeDsPayment(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody PaymentInitiateRequest request
    );
}
