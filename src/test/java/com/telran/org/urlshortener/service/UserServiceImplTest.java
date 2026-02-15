package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.dto.UserCreateDTO;
import com.telran.org.urlshortener.dto.UserDTO;
import com.telran.org.urlshortener.entity.User;
import com.telran.org.urlshortener.exception.EmailNotUniqueException;
import com.telran.org.urlshortener.exception.UserNotFoundException;
import com.telran.org.urlshortener.mapper.Converter;
import com.telran.org.urlshortener.model.RoleType;
import com.telran.org.urlshortener.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {
    @Mock
    private UserJpaRepository repository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private Converter<User, UserCreateDTO, UserDTO> converter;

    @InjectMocks
    private UserServiceImpl service;

    private User admin;
    private User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        admin = new User();
        admin.setId(100L);
        admin.setEmail("admin@test.com");
        admin.setRole(RoleType.ROLE_ADMIN);
        user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setRole(RoleType.ROLE_USER);
    }

    // -------------------------------------------------------
    // CREATE
    // -------------------------------------------------------

    @Test
    void create_success() {
        UserCreateDTO dto = new UserCreateDTO("user@test.com", "pass");
        User entity = new User();
        User saved = new User();
        saved.setId(1L);
        when(converter.dtoToEntity(dto)).thenReturn(entity);
        when(repository.findByEmail("user@test.com")).thenReturn(Optional.empty());
        when(encoder.encode("pass")).thenReturn("ENCODED");
        when(repository.save(entity)).thenReturn(saved);
        when(converter.entityToDto(saved))
                .thenReturn(new UserDTO(1L, "user@test.com", RoleType.ROLE_USER));
        UserDTO result = service.create(dto);
        assertEquals(1L, result.getId());
    }

    @Test
    void create_emailExists_throws() {
        UserCreateDTO dto = new UserCreateDTO("user@test.com", "pass");
        when(repository.findByEmail("user@test.com")).thenReturn(Optional.of(new User()));
        assertThrows(EmailNotUniqueException.class, () -> service.create(dto));
    }

    @Test
    void create_invalidEmail_throws() {
        UserCreateDTO dto = new UserCreateDTO("bademail", "pass");
        assertThrows(IllegalArgumentException.class, () -> service.create(dto));
    }

    // -------------------------------------------------------
    // FIND ALL
    // -------------------------------------------------------

    @Test
    void findAll_adminSuccess() {
        when(repository.findAll()).thenReturn(List.of(user));
        when(converter.entityToDto(user))
                .thenReturn(new UserDTO(1L, "user@test.com", RoleType.ROLE_USER));
        mockAdminAuth();
        List<UserDTO> result = service.findAll();
        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
    }

    // -------------------------------------------------------
    // FIND BY ID
    // -------------------------------------------------------

    @Test
    void findById_adminSuccess() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(converter.entityToDto(user))
                .thenReturn(new UserDTO(1L, "user@test.com", RoleType.ROLE_USER));
        mockAdminAuth();
        UserDTO dto = service.findById(1L);
        assertEquals(1L, dto.getId());
    }

    @Test
    void findById_notFound_throws() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        mockAdminAuth();
        assertThrows(UserNotFoundException.class, () -> service.findById(1L));
    }

    // -------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------

    @Test
    void update_adminSuccess() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);
        when(converter.entityToDto(user))
                .thenReturn(new UserDTO(1L, "user@test.com", RoleType.ROLE_ADMIN));
        mockAdminAuth();
        UserDTO dto = service.update(1L, RoleType.ROLE_ADMIN);
        assertEquals(RoleType.ROLE_ADMIN, dto.getRole());
    }

    @Test
    void update_notFound_throws() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        mockAdminAuth();
        assertThrows(UserNotFoundException.class, () -> service.update(1L, RoleType.ROLE_ADMIN));
    }

    // -------------------------------------------------------
    // DELETE
    // -------------------------------------------------------

    @Test
    void delete_userDeletesSelf_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        mockUserAuth();
        service.delete(1L);
        verify(repository).delete(user);
    }

    @Test
    void delete_userDeletesOther_throws() {
        User other = new User();
        other.setId(2L);
        other.setEmail("other@test.com");
        other.setRole(RoleType.ROLE_USER);
        when(repository.findById(2L)).thenReturn(Optional.of(other));
        mockUserAuth();
        assertThrows(AccessDeniedException.class, () -> service.delete(2L));
    }

    @Test
    void delete_adminDeletesAny_success() {
        User other = new User();
        other.setId(2L);
        when(repository.findById(2L)).thenReturn(Optional.of(other));
        mockAdminAuth();
        service.delete(2L);
        verify(repository).delete(other);
    }

    @Test
    void delete_notFound_throws() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        mockAdminAuth();
        assertThrows(UserNotFoundException.class, () -> service.delete(1L));
    }

    // -------------------------------------------------------
    // FIND BY EMAIL
    // -------------------------------------------------------

    @Test
    void findByEmail_userOwnEmail_success() {
        when(repository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(converter.entityToDto(user))
                .thenReturn(new UserDTO(1L, "user@test.com", RoleType.ROLE_USER));
        mockUserAuth();
        UserDTO dto = service.findByEmail("user@test.com");
        assertEquals(1L, dto.getId());
    }

    @Test
    void findByEmail_userAccessingOther_throws() {
        User other = new User();
        other.setId(2L);
        other.setEmail("other@test.com");
        other.setRole(RoleType.ROLE_USER);
        when(repository.findByEmail("other@test.com")).thenReturn(Optional.of(other));
        mockUserAuth();
        assertThrows(AccessDeniedException.class, () -> service.findByEmail("other@test.com"));
    }

    @Test
    void findByEmail_adminSuccess() {
        User other = new User();
        other.setId(2L);
        other.setEmail("other@test.com");
        other.setRole(RoleType.ROLE_USER);
        when(repository.findByEmail("other@test.com")).thenReturn(Optional.of(other));
        when(converter.entityToDto(other))
                .thenReturn(new UserDTO(2L, "other@test.com", RoleType.ROLE_USER));
        mockAdminAuth();
        UserDTO dto = service.findByEmail("other@test.com");
        assertEquals(2L, dto.getId());
    }

    @Test
    void findByEmail_notFound_throws() {
        when(repository.findByEmail("missing@test.com")).thenReturn(Optional.empty());
        mockAdminAuth();
        assertThrows(UserNotFoundException.class, () -> service.findByEmail("missing@test.com"));
    }

    // -------------------------------------------------------
    // GET CURRENT USER
    // -------------------------------------------------------

    @Test
    void getCurrentUser_success() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("user@test.com");
        when(auth.getPrincipal()).thenReturn(userDetails);
        mockSecurityContext(auth);
        when(repository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        User result = service.getCurrentUser();
        assertEquals(1L, result.getId());
    }

    @Test
    void getCurrentUser_anonymous_throws() {
        Authentication auth = mock(AnonymousAuthenticationToken.class);
        when(auth.isAuthenticated()).thenReturn(false);
        mockSecurityContext(auth);
        assertThrows(SecurityException.class, () -> service.getCurrentUser());
    }

    @Test
    void getCurrentUser_invalidPrincipal_throws() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(new Object());
        mockSecurityContext(auth);
        assertThrows(IllegalArgumentException.class, () -> service.getCurrentUser());
    }

    // -------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------

    private void mockAdminAuth() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        UserDetails adminDetails = mock(UserDetails.class);
        when(adminDetails.getUsername()).thenReturn("admin@test.com");
        when(auth.getPrincipal()).thenReturn(adminDetails);
        mockSecurityContext(auth);
        when(repository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
    }

    private void mockUserAuth() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("user@test.com");
        when(auth.getPrincipal()).thenReturn(userDetails);
        mockSecurityContext(auth);
        when(repository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
    }

    private void mockSecurityContext(Authentication auth) {
        var context = mock(org.springframework.security.core.context.SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);
    }
}