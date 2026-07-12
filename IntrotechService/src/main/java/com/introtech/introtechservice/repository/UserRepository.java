package com.introtech.introtechservice.repository;

import com.introtech.introtechservice.common.BaseRepository;
import com.introtech.introtechservice.entity.User;

import java.util.Optional;

public interface UserRepository extends BaseRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);
}
