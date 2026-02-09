package com.telran.org.urlshortener.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authenticationManager;

    private final UserDetailsService userDetailsService;

    private final JwtService jwtService;

    @Override
    public JwtAuthenticationResponse authenticate(SignInRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getLogin(),
                request.getPassword()));
        UserDetails user = userDetailsService.loadUserByUsername(request.getLogin());
        String token = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(token);
    }
}
