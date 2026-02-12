package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.client.ExternalPaymentClient;
import com.telran.org.urlshortener.dto.SubscriptionCreateDTO;
import com.telran.org.urlshortener.dto.SubscriptionDTO;
import com.telran.org.urlshortener.entity.Subscription;
import com.telran.org.urlshortener.entity.User;
import com.telran.org.urlshortener.exception.IdNotFoundException;
import com.telran.org.urlshortener.exception.PathPrefixNotAvailableException;
import com.telran.org.urlshortener.mapper.Converter;
import com.telran.org.urlshortener.model.RoleType;
import com.telran.org.urlshortener.model.StatusState;
import com.telran.org.urlshortener.repository.SubscriptionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionJpaRepository repository;

    private final UserService service;

    private final ExternalPaymentClient paymentClient;

    private final Converter<Subscription, SubscriptionCreateDTO, SubscriptionDTO> converter;

    @Override
    @PreAuthorize("hasRole('USER')")
    public SubscriptionDTO create(SubscriptionCreateDTO dto) {
        User currentUser = service.getCurrentUser();
        Optional<Subscription> found = repository.findByPathPrefix(dto.getPathPrefix());
        if (found.isPresent()) {
            if (found.get().getExpirationDate() == null
                    || !found.get().getExpirationDate().isAfter(LocalDate.now())) {
                found.get().setCreationDate(LocalDate.now());
                found.get().setStatus(StatusState.UNPAID);
                found.get().setUser(currentUser);
                return converter.entityToDto(repository.save(found.get()));
            }
            throw new PathPrefixNotAvailableException("This pathPrefix \"" + dto.getPathPrefix()
                    + "\" is not available until " + found.get().getExpirationDate());
        }
        Subscription subscriptionBeforeDB = converter.dtoToEntity(dto);
        subscriptionBeforeDB.setCreationDate(LocalDate.now());
        subscriptionBeforeDB.setUser(currentUser);
        return converter.entityToDto(repository.save(subscriptionBeforeDB));
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<SubscriptionDTO> findByUserId(long userId) {
        User currentUser = service.getCurrentUser();
        if (currentUser.getRole() == RoleType.ROLE_USER && currentUser.getId() != userId) {
            throw new AccessDeniedException("You do not have permission to view another user's subscriptions.");
        }
        return repository.findByUserId(userId).stream()
                .map(converter::entityToDto).collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public SubscriptionDTO findById(long id) {
        User currentUser = service.getCurrentUser();
        Subscription subscriptionFromDB = repository.findById(id)
                .orElseThrow(() -> new IdNotFoundException("Subscription with id " + id + " is not found."));
        if (currentUser.getRole() == RoleType.ROLE_USER
                && !subscriptionFromDB.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to view this subscription.");
        }
        return converter.entityToDto(subscriptionFromDB);
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public void delete(long id) {
        Subscription subscriptionFromDB = repository.findById(id)
                .orElseThrow(() -> new IdNotFoundException("Subscription with id " + id + " is not found."));
        if (!subscriptionFromDB.getUser().getId().equals(service.getCurrentUser().getId())) {
            throw new AccessDeniedException("You do not have permission to delete this subscription.");
        }
        repository.delete(subscriptionFromDB);
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public void makePayment(long id) {
        Subscription subscriptionFromDB = repository.findById(id)
                .orElseThrow(() -> new IdNotFoundException("Subscription with id " + id + " is not found."));
        if (!subscriptionFromDB.getUser().getId().equals(service.getCurrentUser().getId())) {
            throw new AccessDeniedException("Subscription with id " + id
                    + " does not belong to this User. User needs to try creating the same subscription.");
        }
        startAsyncPayment(subscriptionFromDB);
    }

    @Async
    public void startAsyncPayment(Subscription subscription) {
        boolean isPaid = paymentClient.waitForPayment(subscription.getId());
        if (!isPaid) {
            subscription.setStatus(StatusState.UNPAID);
            repository.save(subscription);
            return;
        }
        LocalDate now = LocalDate.now();
        LocalDate expiration = subscription.getExpirationDate();
        if (expiration == null || !expiration.isAfter(now)) {
            subscription.setExpirationDate(now.plusMonths(1));
        } else {
            subscription.setExpirationDate(expiration.plusMonths(1));
        }
        subscription.setStatus(StatusState.PAID);
        repository.save(subscription);
    }
}