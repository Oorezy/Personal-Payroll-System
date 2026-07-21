package com.introtech.authenticationservice.service;


import com.introtech.authenticationservice.UserDetailsImpl;
import com.introtech.authenticationservice.entity.AuthUser;
import com.introtech.authenticationservice.repository.AuthUserRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@NullMarked
public class AuthUserService implements UserDetailsService {

    private final AuthUserRepository repository;

    public AuthUserService(AuthUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        AuthUser user = repository.findByEmailIgnoreCase(username).orElse(null);
        if (user != null) {
            return UserDetailsImpl.build(user);
        } else
            throw new UsernameNotFoundException("User not found with username: " + username);
    }

    public UserDetailsImpl findUserById(Long id) {
        return repository.findById(id).map(UserDetailsImpl::build).orElseThrow(
                () -> new UsernameNotFoundException("User not found with id: " + id));
    }

    public UserDetailsImpl findUserByEmail(String email) {
        return repository.findByEmailIgnoreCase(email).map(UserDetailsImpl::build).orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public boolean existsByEmailAndClient_ClientName(String email, String clientClientName){
        return repository.existsByEmailAndClient_ClientName(email, clientClientName);
    }

    public AuthUser save(AuthUser authUser) {
        return repository.save(authUser);
    }

    public boolean isUserVerified(String email) {
        return repository.existsByEmailIgnoreCaseAndVerified(email, true);
    }
}
