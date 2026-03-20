package com.ykw.profile.repository;

import com.ykw.profile.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<Profile, Long> {

}