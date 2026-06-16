package com.introtech.authenticationservice.repository;

import com.introtech.authenticationservice.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
    boolean existsByClientName(String clientName);

    Client findByClientName(String clientName);
}