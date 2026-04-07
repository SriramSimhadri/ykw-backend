package com.ykw.profile.service;

import com.ykw.common.logging.LogEvent;
import com.ykw.common.logging.LogUtil;
import com.ykw.profile.dto.PagedUserResponse;
import com.ykw.profile.dto.UpdateUserProfileRequest;
import com.ykw.profile.dto.UserProfile;
import com.ykw.profile.error.ResourceNotFoundException;
import com.ykw.profile.error.UnauthorizedException;
import com.ykw.profile.mapper.UserProfileMapper;
import com.ykw.profile.model.Profile;
import com.ykw.profile.repository.UserProfileRepository;
import com.ykw.common.security.CurrentUserContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.ykw.common.constants.Constants.USER_EMAIL;
import static com.ykw.common.constants.Constants.USER_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository repository;

    private final UserProfileMapper mapper;

    private final CurrentUserContext currentUserContext;

    public UserProfile getCurrentUserProfile() {

        Long userId = currentUserContext.getCurrentUser().userId();

        LogUtil.info(LogEvent.create("CURRENT_PROFILE_INIT").add(USER_ID, userId));

        Profile entity = repository.findById(userId)
                .orElseThrow(() -> {
                    LogUtil.error(LogEvent.create("CURRENT_PROFILE_NOT_FOUND").add(USER_ID, userId));
                    return new ResourceNotFoundException("Profile not found");
                });

        LogUtil.info(LogEvent.create("CURRENT_PROFILE_FOUND").add(USER_ID, userId));
        return mapper.toResponse(entity);
    }

    @Transactional
    public UserProfile upsertCurrentUserProfile(UpdateUserProfileRequest request) {

        LogUtil.info(LogEvent.create("UPSERT_PROFILE"));

        Long userId = Optional.ofNullable(currentUserContext.getCurrentUser().userId())
                .orElseThrow(() -> {
                    LogUtil.error(LogEvent.create("USER_NOT_AUTHENTICATED").add(USER_EMAIL, request.getName()));
                    return new UnauthorizedException("User not authenticated");
                });

        Profile entity = repository.findById(userId).orElseGet(() -> {
            LogUtil.info(LogEvent.create("UPSERT_PROFILE_TYPE_CREATE").add(USER_ID, userId));
            Profile profile = new Profile();
            profile.setId(userId);
            return profile;
        });

        // update fields
        if (request.getName() != null) entity.setName(request.getName());
        if (request.getBio() != null) entity.setBio(request.getBio());
        if (request.getProfileImageUrl() != null) entity.setProfileImageUrl(request.getProfileImageUrl());

        Profile saved = repository.save(entity);

        LogUtil.info(LogEvent.create("UPSERT_PROFILE_SUCCESS").add(USER_ID, userId));

        return mapper.toResponse(saved);
    }

    public UserProfile getUserById(Long userId) {

        LogUtil.info(LogEvent.create("GET_PROFILE_INIT").add(USER_ID, userId));

        Profile entity = repository.findById(userId)
                .orElseThrow(() -> {
                    LogUtil.info(LogEvent.create("PROFILE_NOT_FOUND").add(USER_ID, userId));
                    return new ResourceNotFoundException("Profile not found for userId: " + userId);
                });

        LogUtil.info(LogEvent.create("GET_PROFILE_SUCCESS").add(USER_ID, userId));
        return mapper.toResponse(entity);
    }


    public PagedUserResponse searchUsers(String query, Pageable pageable) {
        //TODO: Implement search
        return null;
    }
}