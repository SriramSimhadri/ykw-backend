package com.ykw.profile.mapper;

import com.ykw.profile.dto.UserProfile;
import com.ykw.profile.model.Profile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    UserProfile toResponse(Profile profile);

}