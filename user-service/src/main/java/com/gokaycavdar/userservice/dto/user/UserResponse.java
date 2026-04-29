package com.gokaycavdar.userservice.dto.user;

import com.gokaycavdar.userservice.entity.Role;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Role role
) {
}
