package com.gokaycavdar.orderservice.dto.user;

public record UserClientResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String role
) {
}
