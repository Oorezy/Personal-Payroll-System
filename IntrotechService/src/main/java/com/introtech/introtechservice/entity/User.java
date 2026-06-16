package com.introtech.introtechservice.entity;

import com.introtech.introtechservice.common.AuditableEntity;
import com.introtech.introtechservice.common.enums.UserRole;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User extends AuditableEntity {

    private String email;
    private String firstName;
    private String lastName;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    private String phoneNumber;
    @Builder.Default
    private boolean enabled = true;
}
