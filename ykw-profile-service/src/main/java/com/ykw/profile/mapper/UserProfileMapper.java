package com.ykw.profile.mapper;

import com.ykw.profile.dto.UserProfile;
import com.ykw.profile.model.Profile;
import org.mapstruct.Mapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    UserProfile toResponse(Profile profile);

    default OffsetDateTime map(Instant instant) {
        if (instant == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

}