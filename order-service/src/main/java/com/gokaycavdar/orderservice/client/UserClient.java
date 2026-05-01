package com.gokaycavdar.orderservice.client;

import com.gokaycavdar.orderservice.dto.user.UserClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "USER-SERVICE", path = "/api/v1", configuration = com.gokaycavdar.orderservice.config.FeignConfig.class)
public interface UserClient {

    @GetMapping("/users/me")
    UserClientResponse getCurrentUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader);
}
