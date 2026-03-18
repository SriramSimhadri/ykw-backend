package com.ykw.profile.mapper;

import com.ykw.profile.dto.UserResponse;
import com.ykw.profile.model.User;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    default OffsetDateTime map(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }

        return localDateTime.atOffset(ZoneOffset.UTC);
    }

    default LocalDateTime map(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }

        return offsetDateTime.toLocalDateTime();
    }

}