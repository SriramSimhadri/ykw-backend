package com.ykw.profile.service;

import com.ykw.profile.dto.PagedUserResponse;
import com.ykw.profile.dto.UpdateUserProfileRequest;
import com.ykw.profile.dto.UserProfile;
import com.ykw.profile.error.ResourceNotFoundException;
import com.ykw.profile.error.UnauthorizedException;
import com.ykw.profile.mapper.UserProfileMapper;
import com.ykw.profile.model.Profile;
import com.ykw.profile.repository.UserProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository repository;

    private final UserProfileMapper mapper;

    private final CurrentUserProvider currentUserProvider;

    public UserProfile getCurrentUserProfile() {
        Long userId = currentUserProvider.getUserId();

        Profile entity = repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        return mapper.toResponse(entity);
    }

    @Transactional
    public UserProfile upsertCurrentUserProfile(UpdateUserProfileRequest request) {
        Long userId = Optional.ofNullable(currentUserProvider.getUserId())
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        Profile entity = repository.findById(userId).orElseGet(() -> {
            Profile profile = new Profile();
            profile.setId(userId);
            return profile;
        });

        // update fields

        if (request.getName() != null) entity.setName(request.getName());
        if (request.getBio() != null) entity.setBio(request.getBio());
        if (request.getProfileImageUrl() != null) entity.setProfileImageUrl(request.getProfileImageUrl());

        Profile saved = repository.save(entity);

        return mapper.toResponse(saved);
    }

    public UserProfile getUserById(Long userId) {
        Profile entity = repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for userId: " + userId));

        return mapper.toResponse(entity);
    }


    public PagedUserResponse searchUsers(String query, Pageable pageable) {
       /* Page<UserProfileEntity> page = repository.searchByName(query, pageable);

        return new PagedUserResponse(
                page.getContent().stream().map(mapper::toDto).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );*/
        return null;
    }
}