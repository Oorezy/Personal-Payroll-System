package com.introtech.authenticationservice;

import com.introtech.authenticationservice.entity.AuthUser;
import com.introtech.authenticationservice.entity.UserRoles;
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

    private String username;

    private String name;

    private String password;

    private String clientName;

    private boolean enabled;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String username, String password, String clientName, boolean enabled, List<String> roles) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.clientName = clientName;
        this.enabled = enabled;
        this.authorities = roles.stream().map(SimpleGrantedAuthority::new).toList();
    }

    public static UserDetailsImpl build(AuthUser user) {
        return new UserDetailsImpl(user.getId(), user.getEmail(), user.getPassword_hash(), user.getClient().getClientName(),
                user.isEnabled(), user.getRoles().stream().map(UserRoles::getRoleName).toList());
    }

}
