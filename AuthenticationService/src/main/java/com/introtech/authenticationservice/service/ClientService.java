package com.introtech.authenticationservice.service;

import com.introtech.authenticationservice.entity.Client;
import com.introtech.authenticationservice.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    public Client clientExists(String clientName){
        return clientRepository.findByClientName(clientName);
    }
}
