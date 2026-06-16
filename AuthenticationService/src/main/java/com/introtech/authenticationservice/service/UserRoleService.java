package com.introtech.authenticationservice.service;

import com.introtech.authenticationservice.entity.Client;
import com.introtech.authenticationservice.entity.UserRoles;
import com.introtech.authenticationservice.repository.UserRolesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRolesRepository userRoleRepository;

    @Transactional
    public Set<UserRoles> getUserRoles(Set<String> roles, Client client) {

        Set<String> roleNames = roles.stream().map(String::toUpperCase).collect(Collectors.toSet());
        Set<UserRoles> existingRoles = userRoleRepository.findAllByClient(client);
        Set<String> existingRoleNames = existingRoles.stream().map(UserRoles::getRoleName).collect(Collectors.toSet());

        Set<UserRoles> userRoles = existingRoles.stream().filter(role -> roleNames.contains(role.getRoleName())).collect(Collectors.toSet());

        List<UserRoles> newRoles = roleNames.stream()
                .filter(roleName -> !existingRoleNames.contains(roleName))
                .map(roleName -> {
                    UserRoles role = new UserRoles();
                    role.setRoleName(roleName);
                    role.setClient(client);
                    return role;
                })
                .toList();

        newRoles = userRoleRepository.saveAll(newRoles);
        userRoles.addAll(newRoles);

        return userRoles;
    }
}
