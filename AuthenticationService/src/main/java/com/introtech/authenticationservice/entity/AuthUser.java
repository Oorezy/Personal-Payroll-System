package com.introtech.authenticationservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = {"email", "client"}))
public class AuthUser {

    @Id
    @SequenceGenerator(name = "user_sequence", initialValue = 10000, allocationSize = 1)
    @GeneratedValue(generator = "user_sequence")
    private Long id;

    private String email;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String password_hash;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "auth_user_id"),
            inverseJoinColumns = @JoinColumn(name = "roles_id")
    )
    private Set<UserRoles> roles;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client", referencedColumnName = "client_name")
    private Client client;

    private boolean enabled = true;

    @LastModifiedDate
    private Instant updated_at;

    @CreatedDate
    private Instant created_at;

}
