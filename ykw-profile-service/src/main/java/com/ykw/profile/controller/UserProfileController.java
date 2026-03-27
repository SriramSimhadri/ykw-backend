package com.ykw.profile.controller;

import com.ykw.profile.api.UserProfileApi;
import com.ykw.profile.dto.PagedUserResponse;
import com.ykw.profile.dto.UpdateUserProfileRequest;
import com.ykw.profile.dto.UserProfile;
import com.ykw.profile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

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
        UserProfile profile = userProfileService.getCurrentUserProfile();
        return ResponseEntity.ok(profile);
    }

    @Override
    public ResponseEntity<UserProfile> updateCurrentProfile(UpdateUserProfileRequest request) {
        UserProfile profile = userProfileService.upsertCurrentUserProfile(request);
        return ResponseEntity.ok(profile);
    }

    @Override
    public ResponseEntity<UserProfile> getProfile(Long userId) {
        UserProfile profile = userProfileService.getUserById(userId);
        return ResponseEntity.ok(profile);
    }
}