package com.introtech.authenticationservice.repository;

import com.introtech.authenticationservice.entity.Client;
import com.introtech.authenticationservice.entity.UserRoles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface UserRolesRepository extends JpaRepository<UserRoles, Long> {
    Set<UserRoles> findAllByClient(Client client);
}