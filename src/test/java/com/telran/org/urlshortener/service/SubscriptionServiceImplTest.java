package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.dto.SubscriptionCreateDTO;
import com.telran.org.urlshortener.dto.SubscriptionDTO;
import com.telran.org.urlshortener.entity.Subscription;
import com.telran.org.urlshortener.entity.User;
import com.telran.org.urlshortener.exception.IdNotFoundException;
import com.telran.org.urlshortener.exception.PathPrefixAvailabilityException;
import com.telran.org.urlshortener.mapper.Converter;
import com.telran.org.urlshortener.model.RoleType;
import com.telran.org.urlshortener.model.StatusState;
import com.telran.org.urlshortener.repository.SubscriptionJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionJpaRepository repository;

    @Mock
    private UserService userService;

    @Mock
    private Converter<Subscription, SubscriptionCreateDTO, SubscriptionDTO> converter;

    @InjectMocks
    private SubscriptionServiceImpl service;

    private User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setRole(RoleType.USER);
    }

    // -------------------------------------------------------
    // CREATE
    // -------------------------------------------------------

    @Test
    void create_newSubscription_success() {
        SubscriptionCreateDTO dto = new SubscriptionCreateDTO("myprefix");
        Subscription entity = new Subscription("myprefix");
        Subscription saved = new Subscription("myprefix");
        saved.setId(10L);
        saved.setUser(user);
        when(userService.getCurrentUser()).thenReturn(user);
        when(repository.findByPathPrefix("myprefix")).thenReturn(Optional.empty());
        when(converter.dtoToEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(converter.entityToDto(saved)).thenReturn(new SubscriptionDTO(10L, "myprefix",
                null, null, StatusState.UNPAID));

        SubscriptionDTO result = service.create(dto);

        assertEquals(10L, result.getId());
    }

    @Test
    void create_reuseExpiredSubscription_success() {
        SubscriptionCreateDTO dto = new SubscriptionCreateDTO("myprefix");
        Subscription expired = new Subscription("myprefix");
        expired.setId(5L);
        expired.setUser(new User()); // другой пользователь
        expired.setExpirationDate(LocalDate.now().minusDays(1));
        when(userService.getCurrentUser()).thenReturn(user);
        when(repository.findByPathPrefix("myprefix")).thenReturn(Optional.of(expired));
        when(repository.save(expired)).thenReturn(expired);
        when(converter.entityToDto(expired)).thenReturn(new SubscriptionDTO(5L, "myprefix",
                null, null, StatusState.UNPAID));

        SubscriptionDTO result = service.create(dto);

        assertEquals(5L, result.getId());
        assertEquals(user, expired.getUser());
    }

    @Test
    void create_prefixOwnedByAnotherActiveUser_throws() {
        SubscriptionCreateDTO dto = new SubscriptionCreateDTO("myprefix");
        Subscription active = new Subscription("myprefix");
        active.setUser(new User());
        active.setExpirationDate(LocalDate.now().plusDays(5));
        when(userService.getCurrentUser()).thenReturn(user);
        when(repository.findByPathPrefix("myprefix")).thenReturn(Optional.of(active));

        assertThrows(PathPrefixAvailabilityException.class, () -> service.create(dto));
    }

    @Test
    void create_prefixOwnedByCurrentUserActive_throws() {
        SubscriptionCreateDTO dto = new SubscriptionCreateDTO("myprefix");
        Subscription active = new Subscription("myprefix");
        active.setUser(user);
        active.setExpirationDate(LocalDate.now().plusDays(5));
        when(userService.getCurrentUser()).thenReturn(user);
        when(repository.findByPathPrefix("myprefix")).thenReturn(Optional.of(active));

        assertThrows(PathPrefixAvailabilityException.class, () -> service.create(dto));
    }

    // -------------------------------------------------------
    // FIND ALL
    // -------------------------------------------------------

    @Test
    void findAllByUserId_userOwnsData_success() {
        Subscription sub = new Subscription("myprefix");
        sub.setId(1L);
        sub.setUser(user);
        when(userService.getCurrentUser()).thenReturn(user);
        when(repository.findByUserId(1L)).thenReturn(List.of(sub));
        when(converter.entityToDto(sub)).thenReturn(new SubscriptionDTO(1L, "myprefix",
                null, null, StatusState.UNPAID));

        List<SubscriptionDTO> result = service.findAllByUserId(1L);

        assertEquals(1, result.size());
    }

    @Test
    void findAllByUserId_userAccessingOthers_throws() {
        User other = new User();
        other.setId(2L);
        when(userService.getCurrentUser()).thenReturn(user);

        assertThrows(AccessDeniedException.class, () -> service.findAllByUserId(2L));
    }

    @Test
    void findAllByUserId_adminAccess_success() {
        user.setRole(RoleType.ADMIN);
        Subscription sub = new Subscription("myprefix");
        sub.setId(1L);
        sub.setUser(new User());
        when(userService.getCurrentUser()).thenReturn(user);
        when(repository.findByUserId(2L)).thenReturn(List.of(sub));
        when(converter.entityToDto(sub)).thenReturn(new SubscriptionDTO(1L, "myprefix",
                null, null, StatusState.UNPAID));

        List<SubscriptionDTO> result = service.findAllByUserId(2L);

        assertEquals(1, result.size());
    }

    // -------------------------------------------------------
    // FIND BY ID
    // -------------------------------------------------------

    @Test
    void findById_success() {
        Subscription sub = new Subscription("myprefix");
        sub.setId(1L);
        sub.setUser(user);
        when(userService.getCurrentUser()).thenReturn(user);
        when(repository.findById(1L)).thenReturn(Optional.of(sub));
        when(converter.entityToDto(sub)).thenReturn(new SubscriptionDTO(1L, "myprefix",
                null, null, StatusState.UNPAID));

        SubscriptionDTO dto = service.findById(1L);

        assertEquals(1L, dto.getId());
    }

    @Test
    void findById_notFound_throws() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IdNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void findById_userAccessingOthers_throws() {
        User other = new User();
        other.setId(2L);
        Subscription sub = new Subscription("myprefix");
        sub.setId(1L);
        sub.setUser(other);
        when(userService.getCurrentUser()).thenReturn(user);
        when(repository.findById(1L)).thenReturn(Optional.of(sub));

        assertThrows(AccessDeniedException.class, () -> service.findById(1L));
    }

    // -------------------------------------------------------
    // DELETE
    // -------------------------------------------------------

    @Test
    void delete_success() {
        Subscription sub = new Subscription("myprefix");
        sub.setId(1L);
        sub.setUser(user);
        when(userService.getCurrentUser()).thenReturn(user);
        when(repository.findById(1L)).thenReturn(Optional.of(sub));

        service.delete(1L);

        verify(repository).delete(sub);
    }

    @Test
    void delete_otherUser_throws() {
        User other = new User();
        other.setId(2L);
        Subscription sub = new Subscription("myprefix");
        sub.setId(1L);
        sub.setUser(other);
        when(userService.getCurrentUser()).thenReturn(user);
        when(repository.findById(1L)).thenReturn(Optional.of(sub));

        assertThrows(AccessDeniedException.class, () -> service.delete(1L));
    }

    @Test
    void delete_notFound_throws() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IdNotFoundException.class, () -> service.delete(1L));
    }

    // -------------------------------------------------------
    // MAKE PAYMENT (now synchronous, admin only)
    // -------------------------------------------------------

    @Test
    void makePayment_subscriptionNotFound_throws() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IdNotFoundException.class, () -> service.makePayment(1L));
    }

    @Test
    void makePayment_nullExpiration_setsOneMonthFromNow() {
        Subscription sub = new Subscription("myprefix");
        sub.setId(1L);
        sub.setExpirationDate(null);
        when(repository.findById(1L)).thenReturn(Optional.of(sub));

        service.makePayment(1L);

        assertEquals(StatusState.PAID, sub.getStatus());
        assertNotNull(sub.getExpirationDate());
        assertEquals(LocalDate.now().plusMonths(1), sub.getExpirationDate());
        verify(repository).save(sub);
    }

    @Test
    void makePayment_expiredSubscription_setsOneMonthFromNow() {
        Subscription sub = new Subscription("myprefix");
        sub.setId(1L);
        sub.setExpirationDate(LocalDate.now().minusDays(5));
        when(repository.findById(1L)).thenReturn(Optional.of(sub));

        service.makePayment(1L);

        assertEquals(StatusState.PAID, sub.getStatus());
        assertEquals(LocalDate.now().plusMonths(1), sub.getExpirationDate());
        verify(repository).save(sub);
    }

    @Test
    void makePayment_activeSubscription_extendsByOneMonth() {
        LocalDate currentExpiration = LocalDate.now().plusDays(10);
        Subscription sub = new Subscription("myprefix");
        sub.setId(1L);
        sub.setExpirationDate(currentExpiration);
        when(repository.findById(1L)).thenReturn(Optional.of(sub));

        service.makePayment(1L);

        assertEquals(StatusState.PAID, sub.getStatus());
        assertEquals(currentExpiration.plusMonths(1), sub.getExpirationDate());
        verify(repository).save(sub);
    }
}