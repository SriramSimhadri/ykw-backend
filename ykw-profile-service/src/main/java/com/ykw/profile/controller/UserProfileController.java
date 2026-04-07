package com.ykw.profile.controller;

import com.ykw.common.logging.LogEvent;
import com.ykw.common.logging.LogUtil;
import com.ykw.profile.api.UserProfileApi;
import com.ykw.profile.dto.PagedUserResponse;
import com.ykw.profile.dto.UpdateUserProfileRequest;
import com.ykw.profile.dto.UserProfile;
import com.ykw.profile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static com.ykw.common.constants.Constants.*;

@RestController
@RequiredArgsConstructor
public class UserProfileController implements UserProfileApi {

    private final UserProfileService userProfileService;

    @Override
    public ResponseEntity<PagedUserResponse> searchProfiles(String q, Integer page, Integer size) {
     /*   PagedUserResponse response =
                userProfileService.searchUsers(q, PageRequest.of(page, size));*/

        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<UserProfile> getCurrentProfile() {

        LogUtil.info(LogEvent.create("CURRENT_PROFILE_REQUEST"));

        UserProfile profile = userProfileService.getCurrentUserProfile();

        LogUtil.info(LogEvent.create("CURRENT_PROFILE_REQUEST_SUCCESS").add(USER_ID, profile.getId()));
        return ResponseEntity.ok(profile);
    }

    @Override
    public ResponseEntity<UserProfile> updateCurrentProfile(UpdateUserProfileRequest request) {

        LogUtil.info(LogEvent.create("UPDATE_PROFILE_REQUEST").add(USER_NAME, request.getName()));

        UserProfile profile = userProfileService.upsertCurrentUserProfile(request);

        LogUtil.info(LogEvent.create("UPDATE_PROFILE_SUCCESS").add(USER_NAME, request.getName()));
        return ResponseEntity.ok(profile);
    }

    @Override
    public ResponseEntity<UserProfile> getProfile(Long userId) {

        LogUtil.info(LogEvent.create("PROFILE_REQUEST").add(USER_ID, userId));

        UserProfile profile = userProfileService.getUserById(userId);

        LogUtil.info(LogEvent.create("PROFILE_REQUEST_SUCCESS").add(USER_ID, profile.getId()));
        return ResponseEntity.ok(profile);
    }
}