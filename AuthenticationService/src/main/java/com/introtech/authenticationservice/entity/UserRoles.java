package com.introtech.authenticationservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "roles")
public class UserRoles {

    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    private RoleName roleName;
}
