package com.telran.org.urlshortener.security;

import com.telran.org.urlshortener.entity.User;
import com.telran.org.urlshortener.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserJpaRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User userFromDB = repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with login " + username + " is not found"));
        return new org.springframework.security.core.userdetails.User(userFromDB.getEmail(),
                userFromDB.getPassword(),
                List.of(new SimpleGrantedAuthority(userFromDB.getRole().name())));
    }
}