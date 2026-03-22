package com.ykw.auth.mapper;

import com.ykw.auth.dto.UserResponse;
import com.ykw.auth.dto.UserRole;
import com.ykw.auth.dto.UserStatus;
import com.ykw.auth.model.User;
import org.mapstruct.Mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    default UserRole map(com.ykw.auth.model.UserRole role) {
        if (role == null) {
            return null;
        }
        return UserRole.fromValue(role.name());
    }

    default UserStatus map(com.ykw.auth.model.UserStatus status) {
        if (status == null) {
            return null;
        }
        return UserStatus.fromValue(status.name());
    }

    default OffsetDateTime map(Instant localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(localDateTime, ZoneOffset.UTC);
    }
}