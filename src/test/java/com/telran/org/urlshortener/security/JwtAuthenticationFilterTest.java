package com.telran.org.urlshortener.security;

import com.telran.org.urlshortener.entity.User;
import com.telran.org.urlshortener.model.RoleType;
import com.telran.org.urlshortener.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthenticationFilterTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private UserJpaRepository repository;

    private User testUser;

    private String token;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(10L);
        testUser.setEmail("test@mail.com");
        testUser.setPassword("$2a$10$abcdefghijklmnopqrstuv");
        testUser.setRole(RoleType.ROLE_ADMIN);

        when(repository.findByEmail("test@mail.com")).thenReturn(Optional.of(testUser));
        when(repository.findById(10L)).thenReturn(Optional.of(testUser));
        when(repository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("test@mail.com")
                .password(testUser.getPassword())
                .roles("ADMIN")
                .build();

        token = jwtService.generateToken(userDetails);
    }

    @Test
    void testAccessWithValidToken() throws Exception {
        mockMvc.perform(get("/users/10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void testAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/users/10"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAccessWithInvalidToken() throws Exception {
        String invalidToken = "invalid.token.that.is.definitely.not.valid";

        mockMvc.perform(get("/users/10")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isForbidden());
    }
}