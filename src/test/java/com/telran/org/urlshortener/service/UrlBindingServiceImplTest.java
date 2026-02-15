package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.dto.UrlBindingCreateDTO;
import com.telran.org.urlshortener.dto.UrlBindingDTO;
import com.telran.org.urlshortener.entity.Subscription;
import com.telran.org.urlshortener.entity.UrlBinding;
import com.telran.org.urlshortener.entity.User;
import com.telran.org.urlshortener.exception.IdNotFoundException;
import com.telran.org.urlshortener.exception.PathPrefixAvailabilityException;
import com.telran.org.urlshortener.mapper.Converter;
import com.telran.org.urlshortener.model.RoleType;
import com.telran.org.urlshortener.repository.UrlBindingJpaRepository;
import com.telran.org.urlshortener.utility.UrlValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.access.AccessDeniedException;

import java.net.URI;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UrlBindingServiceImplTest {
    @Mock
    private UrlBindingJpaRepository repository;

    @Mock
    private UserService userService;

    @Mock
    private UrlValidationService urlValidation;

    @Mock
    private Converter<UrlBinding, UrlBindingCreateDTO, UrlBindingDTO> converter;

    @InjectMocks
    private UrlBindingServiceImpl service;

    private User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setRole(RoleType.ROLE_USER);
        user.setSubscriptions(new ArrayList<>());
    }

    // -------------------------------------------------------
    // CREATE
    // -------------------------------------------------------

    @Test
    void create_withoutPrefix_createsNewBinding() {
        UrlBindingCreateDTO dto = new UrlBindingCreateDTO("http://example.com");
        UrlBinding entity = new UrlBinding("http://example.com");
        UrlBinding saved = new UrlBinding("http://example.com");
        saved.setId(10L);
        saved.setUid("/abcdef12");
        when(userService.getCurrentUser()).thenReturn(user);
        when(urlValidation.normalizeUrl(any())).thenReturn("http://example.com");
        when(urlValidation.isValidRedirectUrl(any())).thenReturn(true);
        when(converter.dtoToEntity(dto)).thenReturn(entity);
        when(repository.findByUid("/abcdef12")).thenReturn(Optional.empty());
        when(repository.save(entity)).thenReturn(saved);
        when(converter.entityToDto(saved)).thenReturn(new UrlBindingDTO(10L,
                "http://example.com", "short", 0L));
        UrlBindingDTO result = service.create(dto, null);
        assertEquals(10L, result.getId());
        verify(repository).save(entity);
    }

    @Test
    void create_withoutPrefix_reusesExisting() {
        UrlBindingCreateDTO dto = new UrlBindingCreateDTO("http://example.com");
        UrlBinding existing = new UrlBinding("http://example.com");
        existing.setId(5L);
        existing.setUid("/abcdef12");
        when(userService.getCurrentUser()).thenReturn(user);
        when(urlValidation.normalizeUrl(any())).thenReturn("http://example.com");
        when(converter.dtoToEntity(dto)).thenReturn(new UrlBinding("http://example.com"));
        when(repository.findByUid("/abcdef12")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(existing);
        when(converter.entityToDto(existing))
                .thenReturn(new UrlBindingDTO(5L, "http://example.com", "short", 0L));
        UrlBindingDTO result = service.create(dto, null);
        assertEquals(5L, result.getId());
    }

    @Test
    void create_withPrefix_subscriptionNotActive_throws() {
        UrlBindingCreateDTO dto = new UrlBindingCreateDTO("http://example.com");
        when(userService.getCurrentUser()).thenReturn(user);
        when(urlValidation.normalizeUrl(any())).thenReturn("http://example.com");
        assertThrows(PathPrefixAvailabilityException.class,
                () -> service.create(dto, "myprefix"));
    }

    @Test
    void create_withPrefix_success() {
        UrlBindingCreateDTO dto = new UrlBindingCreateDTO("http://example.com");
        Subscription sub = new Subscription("myprefix");
        sub.setUser(user);
        sub.setExpirationDate(LocalDate.now().plusDays(5));
        user.getSubscriptions().add(sub);
        UrlBinding entity = new UrlBinding("http://example.com");
        UrlBinding saved = new UrlBinding("http://example.com");
        saved.setId(99L);
        saved.setUid("/myprefix/abcdef12");
        when(userService.getCurrentUser()).thenReturn(user);
        when(urlValidation.normalizeUrl(any())).thenReturn("http://example.com");
        when(repository.findByUid("/myprefix/abcdef12")).thenReturn(Optional.empty());
        when(converter.dtoToEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(converter.entityToDto(saved)).thenReturn(new UrlBindingDTO(99L, "http://example.com",
                "short", 0L));
        UrlBindingDTO result = service.create(dto, "myprefix");
        assertEquals(99L, result.getId());
    }

    // -------------------------------------------------------
    // FIND
    // -------------------------------------------------------

    @Test
    void find_success() {
        UrlBinding binding = new UrlBinding("http://example.com");
        binding.setId(1L);
        binding.setUser(user);
        binding.setUid("/abc");
        when(userService.getCurrentUser()).thenReturn(user);
        when(urlValidation.normalizeUId("/abc")).thenReturn("/abc");
        when(repository.findByUid("/abc")).thenReturn(Optional.of(binding));
        when(converter.entityToDto(binding)).thenReturn(new UrlBindingDTO(1L, "http://example.com",
                "short", 0L));
        UrlBindingDTO dto = service.find("/abc");
        assertEquals(1L, dto.getId());
    }

    @Test
    void find_notFound_throws() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(urlValidation.normalizeUId("/abc")).thenReturn("/abc");
        when(repository.findByUid("/abc")).thenReturn(Optional.empty());
        assertThrows(IdNotFoundException.class, () -> service.find("/abc"));
    }

    @Test
    void find_userAccessingOthers_throws() {
        User other = new User();
        other.setId(2L);
        UrlBinding binding = new UrlBinding("http://example.com");
        binding.setUser(other);
        when(userService.getCurrentUser()).thenReturn(user);
        when(urlValidation.normalizeUId("/abc")).thenReturn("/abc");
        when(repository.findByUid("/abc")).thenReturn(Optional.of(binding));
        assertThrows(AccessDeniedException.class, () -> service.find("/abc"));
    }

    // -------------------------------------------------------
    // RESET
    // -------------------------------------------------------

    @Test
    void reset_success() {
        UrlBinding binding = new UrlBinding("http://example.com");
        binding.setId(1L);
        binding.setUser(user);
        when(userService.getCurrentUser()).thenReturn(user);
        when(repository.findById(1L)).thenReturn(Optional.of(binding));
        when(repository.save(binding)).thenReturn(binding);
        when(converter.entityToDto(binding)).thenReturn(new UrlBindingDTO(1L, "http://example.com",
                "short", 0L));
        UrlBindingDTO dto = service.reset(1L);
        assertEquals(0L, binding.getCount());
    }

    @Test
    void reset_otherUser_throws() {
        User other = new User();
        other.setId(2L);
        UrlBinding binding = new UrlBinding("http://example.com");
        binding.setUser(other);
        when(userService.getCurrentUser()).thenReturn(user);
        when(repository.findById(1L)).thenReturn(Optional.of(binding));
        assertThrows(AccessDeniedException.class, () -> service.reset(1L));
    }

    // -------------------------------------------------------
    // DELETE
    // -------------------------------------------------------

    @Test
    void delete_success() {
        UrlBinding binding = new UrlBinding("http://example.com");
        binding.setUser(user);
        when(userService.getCurrentUser()).thenReturn(user);
        when(repository.findById(1L)).thenReturn(Optional.of(binding));
        service.delete(1L);
        verify(repository).delete(binding);
    }

    @Test
    void delete_otherUser_throws() {
        User other = new User();
        other.setId(2L);
        UrlBinding binding = new UrlBinding("http://example.com");
        binding.setUser(other);
        when(userService.getCurrentUser()).thenReturn(user);
        when(repository.findById(1L)).thenReturn(Optional.of(binding));
        assertThrows(AccessDeniedException.class, () -> service.delete(1L));
    }

    // -------------------------------------------------------
    // REDIRECT
    // -------------------------------------------------------

    @Test
    void resolveRedirect_success() {
        UrlBindingDTO dto = new UrlBindingDTO(1L, "http://example.com", "short", 0L);
        when(userService.getCurrentUser()).thenReturn(user);
        when(urlValidation.isValidUId("/abc")).thenReturn(true);
        UrlBinding binding = new UrlBinding("http://example.com");
        binding.setId(1L);
        binding.setUser(user);
        when(urlValidation.normalizeUId("/abc")).thenReturn("/abc");
        when(repository.findByUid("/abc")).thenReturn(Optional.of(binding));
        when(converter.entityToDto(binding)).thenReturn(dto);
        when(urlValidation.isValidRedirectUrl("http://example.com")).thenReturn(true);
        when(repository.incrementCountById(1L)).thenReturn(1);
        URI uri = service.resolveRedirectUri("/abc");
        assertEquals("http://example.com", uri.toString());
    }

    @Test
    void resolveRedirect_invalidUId_throws() {
        when(urlValidation.isValidUId("/abc")).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> service.resolveRedirectUri("/abc"));
    }

    @Test
    void resolveRedirect_unsafeUrl_throws() {
        UrlBindingDTO dto = new UrlBindingDTO(1L, "http://example.com", "short", 0L);
        when(urlValidation.isValidUId("/abc")).thenReturn(true);
        UrlBinding binding = new UrlBinding("http://example.com");
        binding.setId(1L);
        binding.setUser(user);
        when(userService.getCurrentUser()).thenReturn(user);
        when(urlValidation.normalizeUId("/abc")).thenReturn("/abc");
        when(repository.findByUid("/abc")).thenReturn(Optional.of(binding));
        when(converter.entityToDto(binding)).thenReturn(dto);
        when(urlValidation.isValidRedirectUrl("http://example.com")).thenReturn(false);
        when(userService.getCurrentUser()).thenReturn(user);
        assertThrows(AccessDeniedException.class, () -> service.resolveRedirectUri("/abc"));
    }

    @Test
    void resolveRedirect_incrementFailed_throws() {
        UrlBindingDTO dto = new UrlBindingDTO(1L, "http://example.com", "short", 0L);
        when(urlValidation.isValidUId("/abc")).thenReturn(true);
        UrlBinding binding = new UrlBinding("http://example.com");
        binding.setId(1L);
        binding.setUser(user);
        when(userService.getCurrentUser()).thenReturn(user);
        when(urlValidation.normalizeUId("/abc")).thenReturn("/abc");
        when(repository.findByUid("/abc")).thenReturn(Optional.of(binding));
        when(converter.entityToDto(binding)).thenReturn(dto);
        when(urlValidation.isValidRedirectUrl("http://example.com")).thenReturn(true);
        when(repository.incrementCountById(1L)).thenReturn(0);
        when(userService.getCurrentUser()).thenReturn(user);
        assertThrows(IdNotFoundException.class, () -> service.resolveRedirectUri("/abc"));
    }
}
