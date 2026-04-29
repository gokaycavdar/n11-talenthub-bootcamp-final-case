package com.gokaycavdar.userservice.mapper;

import org.mapstruct.Mapper;

import com.gokaycavdar.userservice.dto.user.UserResponse;
import com.gokaycavdar.userservice.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toUserResponse(User user);
}
