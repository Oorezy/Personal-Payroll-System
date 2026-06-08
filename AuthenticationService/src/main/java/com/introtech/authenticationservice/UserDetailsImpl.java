package com.introtech.authenticationservice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.introtech.authenticationservice.entity.AuthUser;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

@Data
public class UserDetailsImpl implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

//    private String email;

    private String username;

    private String name;

    @JsonIgnore
    private String password;

    private String phoneNumber;

    private boolean accountNonLocked;

    private boolean enabled;

    private boolean accountNonExpired;

    private String imageUrl;


    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String username, String password, List<String> roles) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.enabled = true;
        this.accountNonLocked = true;
        this.accountNonExpired = true;
        this.authorities = roles.stream().map(SimpleGrantedAuthority::new).toList();
    }

    public static UserDetailsImpl build(AuthUser user) {
        return new UserDetailsImpl(user.getId(), user.getEmail(), user.getPassword_hash(),
                user.getRoles().stream().map(role -> role.getRoleName().name()).toList());
    }

}
