package com.telran.org.urlshortener.security;

import com.telran.org.urlshortener.entity.User;
import com.telran.org.urlshortener.model.RoleType;
import com.telran.org.urlshortener.repository.UserJpaRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceTest {
    private JwtService jwtService;

    private SecretKey secretKey;

    private UserJpaRepository repository;

    @BeforeEach
    void setUp() {
        secretKey = Keys.hmacShaKeyFor("12345678901234567890123456789012".getBytes());
        repository = mock(UserJpaRepository.class);
        jwtService = new JwtService(secretKey, repository);
        setExpiration(jwtService, 3600000L);
    }

    private void setExpiration(JwtService service, long value) {
        try {
            var field = JwtService.class.getDeclaredField("expirationMs");
            field.setAccessible(true);
            field.set(service, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private UserDetails createUserDetails() {
        return org.springframework.security.core.userdetails.User
                .withUsername("test@mail.com")
                .password("pass")
                .roles("USER")
                .build();
    }

    private User createEntityUser() {
        User user = new User();
        user.setId(10L);
        user.setEmail("test@mail.com");
        user.setPassword("pass");
        user.setRole(RoleType.USER);
        return user;
    }

    @Test
    void testGenerateToken_withCustomClaims() {
        UserDetails userDetails = createUserDetails();
        User entityUser = createEntityUser();
        when(repository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(entityUser));
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        assertEquals("test@mail.com", claims.getSubject());
        assertEquals(10, claims.get("userId"));
        assertEquals("test@mail.com", claims.get("login"));
        assertEquals("USER", claims.get("role"));
    }

    @Test
    void testExtractUserName() {
        UserDetails userDetails = createUserDetails();
        when(repository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(createEntityUser()));
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUserName(token);
        assertEquals("test@mail.com", username);
    }

    @Test
    void testIsTokenValid_valid() {
        UserDetails userDetails = createUserDetails();
        when(repository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(createEntityUser()));
        String token = jwtService.generateToken(userDetails);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void testIsTokenValid_invalidUsername() {
        UserDetails userDetails = createUserDetails();
        when(repository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(createEntityUser()));
        String token = jwtService.generateToken(userDetails);
        UserDetails wrongUser = org.springframework.security.core.userdetails.User
                .withUsername("wrong@mail.com")
                .password("pass")
                .roles("USER")
                .build();
        assertFalse(jwtService.isTokenValid(token, wrongUser));
    }

    @Test
    void testTokenExpired() {
        UserDetails userDetails = createUserDetails();
        when(repository.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(createEntityUser()));
        setExpiration(jwtService, -1);
        String token = jwtService.generateToken(userDetails);
        assertFalse(jwtService.isTokenValid(token, userDetails));
    }
}