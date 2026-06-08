package com.introtech.authenticationservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
public class AuthUser {

    @Id
    @SequenceGenerator(name = "user_sequence", initialValue = 10000, allocationSize = 1)
    @GeneratedValue(generator = "user_sequence")
    private Long id;

    @Column(unique = true)
    private String email;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String password_hash;

    @OneToMany(fetch = FetchType.EAGER)
    private Set<UserRoles> roles;

    @LastModifiedDate
    private Instant updated_at;

    @CreatedDate
    private Instant created_at;

}
