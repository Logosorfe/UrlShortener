package com.telran.org.urlshortener.security;

public interface AuthenticationService {

    JwtAuthenticationResponse authenticate(SignInRequest request);
}

