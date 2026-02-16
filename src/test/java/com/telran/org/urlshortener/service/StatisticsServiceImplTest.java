package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.dto.UrlBindingDTO;
import com.telran.org.urlshortener.entity.UrlBinding;
import com.telran.org.urlshortener.entity.User;
import com.telran.org.urlshortener.exception.IdNotFoundException;
import com.telran.org.urlshortener.mapper.Converter;
import com.telran.org.urlshortener.model.RoleType;
import com.telran.org.urlshortener.repository.UrlBindingJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatisticsServiceImplTest {
    @Mock
    private UrlBindingJpaRepository repository;

    @Mock
    private UserService userService;

    @Mock
    private Converter<UrlBinding, ?, UrlBindingDTO> converter;

    @InjectMocks
    private StatisticsServiceImpl service;

    private User user;

    private User admin;

    private UrlBinding userBinding;

    private UrlBinding adminBinding;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setRole(RoleType.USER);
        admin = new User();
        admin.setId(2L);
        admin.setEmail("admin@test.com");
        admin.setRole(RoleType.ADMIN);
        userBinding = new UrlBinding();
        userBinding.setId(10L);
        userBinding.setUser(user);
        userBinding.setCount(5L);
        adminBinding = new UrlBinding();
        adminBinding.setId(20L);
        adminBinding.setUser(admin);
        adminBinding.setCount(10L);
    }

    // -------------------------------------------------------
    // requestsNumberByUser
    // -------------------------------------------------------

    @Test
    void requestsNumberByUser_adminSuccess() {
        when(userService.getCurrentUser()).thenReturn(admin);
        when(repository.sumCountByUserId(1L)).thenReturn(5L);
        Long result = service.requestsNumberByUser(1L);
        assertEquals(5L, result);
    }

    @Test
    void requestsNumberByUser_userOwnSuccess() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(repository.sumCountByUserId(1L)).thenReturn(5L);
        Long result = service.requestsNumberByUser(1L);
        assertEquals(5L, result);
    }

    @Test
    void requestsNumberByUser_userForbidden() {
        when(userService.getCurrentUser()).thenReturn(user);

        assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> service.requestsNumberByUser(2L)
        );
    }

    // -------------------------------------------------------
    // requestsNumberByUrlBinding
    // -------------------------------------------------------

    @Test
    void requestsNumberByUrlBinding_adminSuccess() {
        when(userService.getCurrentUser()).thenReturn(admin);
        when(repository.findById(20L)).thenReturn(Optional.of(adminBinding));
        long result = service.requestsNumberByUrlBinding(20L);
        assertEquals(10L, result);
    }

    @Test
    void requestsNumberByUrlBinding_userOwnSuccess() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(repository.findById(10L)).thenReturn(Optional.of(userBinding));
        long result = service.requestsNumberByUrlBinding(10L);
        assertEquals(5L, result);
    }

    @Test
    void requestsNumberByUrlBinding_userForbidden() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(repository.findById(20L)).thenReturn(Optional.of(adminBinding));
        assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> service.requestsNumberByUrlBinding(20L)
        );
    }

    @Test
    void requestsNumberByUrlBinding_notFound() {
        when(userService.getCurrentUser()).thenReturn(admin);
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(IdNotFoundException.class,
                () -> service.requestsNumberByUrlBinding(999L));
    }

    // -------------------------------------------------------
    // topTenRequests
    // -------------------------------------------------------

    @Test
    void topTenRequests_adminSuccess() {
        when(userService.getCurrentUser()).thenReturn(admin);
        when(repository.findTop10ByOrderByCountDesc())
                .thenReturn(List.of(adminBinding, userBinding));
        UrlBindingDTO dto1 = new UrlBindingDTO(20L, "https://example.com/long1", "short1", 10L);
        UrlBindingDTO dto2 = new UrlBindingDTO(10L, "https://example.com/long2", "short2", 5L);
        when(converter.entityToDto(adminBinding)).thenReturn(dto1);
        when(converter.entityToDto(userBinding)).thenReturn(dto2);
        List<UrlBindingDTO> result = service.topTenRequests();
        assertEquals(2, result.size());
        assertEquals(20L, result.get(0).getId());
    }
}