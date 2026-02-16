package com.telran.org.urlshortener.service;

//import com.telran.org.urlshortener.client.ExternalPaymentClient;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionJpaRepository repository;

    private final UserService service;

//    private final ExternalPaymentClient paymentClient;

    private final Converter<Subscription, SubscriptionCreateDTO, SubscriptionDTO> converter;

    @Override
    @PreAuthorize("hasAuthority('USER')")
    @Transactional
    public SubscriptionDTO create(SubscriptionCreateDTO dto) {
        Objects.requireNonNull(dto, "SubscriptionCreateDTO must not be null");
        String prefix = normalizePrefix(dto.getPathPrefix());
        User currentUser = service.getCurrentUser();
        log.debug("Creating subscription userId={}, prefix={}", currentUser.getId(), prefix);
        Optional<Subscription> existing = repository.findByPathPrefix(prefix);
        LocalDate now = LocalDate.now();
        if (existing.isPresent()) {
            Subscription sub = existing.get();
            LocalDate expirationDate  = sub.getExpirationDate();
            log.debug("Existing subscription found id={}, expiration={}", sub.getId(), expirationDate);
            if (!Objects.equals(sub.getUser().getId(), currentUser.getId())) {
                if (expirationDate != null && !expirationDate.isAfter(now)) {
                    log.info("Reusing expired subscription id={} for user={}", sub.getId(), currentUser.getId());
                    sub.setCreationDate(LocalDate.now());
                    sub.setStatus(StatusState.UNPAID);
                    sub.setUser(currentUser);
                    Subscription saved = repository.save(sub);
                    log.info("Subscription renewed id={}, prefix={}", saved.getId(), prefix);
                    return converter.entityToDto(saved);
                }
                log.warn("Prefix {} is not available until {}", prefix, expirationDate);
                throw new PathPrefixAvailabilityException("Subscription is not available until "
                        + sub.getExpirationDate());
            }
            log.warn("Subscription belongs to user and expiration date is {}", expirationDate);
            throw new PathPrefixAvailabilityException("Subscription is yours and expiration date is "
                    + expirationDate);
        }
        Subscription newSub = converter.dtoToEntity(dto);
        newSub.setCreationDate(LocalDate.now());
        newSub.setUser(currentUser);
        Subscription saved = repository.save(newSub);
        log.info("Subscription created id={}, prefix={}", saved.getId(), prefix);
        return converter.entityToDto(saved);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @Transactional(readOnly = true)
    public List<SubscriptionDTO> findAllByUserId(long userId) {
        validateId(userId);
        User currentUser = service.getCurrentUser();
        log.debug("findAllByUserId requestedUserId={} by user={}", userId, currentUser.getId());
        if (currentUser.getRole() == RoleType.USER &&
                !Objects.equals(currentUser.getId(), userId)) {
            log.warn("User {} attempted to access subscriptions of user {}", currentUser.getId(), userId);
            throw new AccessDeniedException("You do not have permission to view another user's subscriptions.");
        }
        List<SubscriptionDTO> list = repository.findByUserId(userId).stream()
                .map(converter::entityToDto)
                .collect(Collectors.toList());
        log.info("Returned {} subscriptions for userId={}", list.size(), userId);
        return list;
    }

    @Override
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @Transactional(readOnly = true)
    public SubscriptionDTO findById(long id) {
        validateId(id);
        User currentUser = service.getCurrentUser();
        log.debug("findById subscriptionId={} by user={}", id, currentUser.getId());
        Subscription sub = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Subscription not found id={}", id);
                    return new IdNotFoundException("Subscription with id " + id + " is not found.");
                });
        if (currentUser.getRole() == RoleType.USER &&
                !Objects.equals(sub.getUser().getId(), currentUser.getId())) {
            log.warn("User {} attempted to access subscription {} belonging to {}",
                    currentUser.getId(), id, sub.getUser().getId());
            throw new AccessDeniedException("You do not have permission to view this subscription.");
        }
        log.info("Subscription found id={}", id);
        return converter.entityToDto(sub);
    }

    @Override
    @PreAuthorize("hasAuthority('USER')")
    @Transactional
    public void delete(long id) {
        validateId(id);
        User currentUser = service.getCurrentUser();
        log.debug("delete subscription id={} by user={}", id, currentUser.getId());
        Subscription sub = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Subscription not found id={}", id);
                    return new IdNotFoundException("Subscription with id " + id + " is not found.");
                });
        if (!Objects.equals(sub.getUser().getId(), currentUser.getId())) {
            log.warn("User {} attempted to delete subscription {} belonging to {}",
                    currentUser.getId(), id, sub.getUser().getId());
            throw new AccessDeniedException("You do not have permission to delete this subscription.");
        }
        repository.delete(sub);
        log.info("Subscription deleted id={}", id);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional
    public void makePayment(long id) {
        validateId(id);
//        User currentUser = service.getCurrentUser();
//        log.debug("makePayment subscriptionId={} by user={}", id, currentUser.getId());
        Subscription sub = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Subscription not found id={}", id);
                    return new IdNotFoundException("Subscription with id " + id + " is not found.");
                });
        LocalDate now = LocalDate.now();
        LocalDate expiration = sub.getExpirationDate();
        if (expiration == null || !expiration.isAfter(now)) {
            sub.setExpirationDate(now.plusMonths(1));
        } else {
            sub.setExpirationDate(expiration.plusMonths(1));
        }
        sub.setStatus(StatusState.PAID);
        repository.save(sub);
        log.info("Payment successful for subscription {}, new expiration={}",
                id, sub.getExpirationDate());
//        if (!Objects.equals(sub.getUser().getId(), currentUser.getId())) {
//            log.warn("User {} attempted to pay for subscription {} belonging to {}",
//                    currentUser.getId(), id, sub.getUser().getId());
//            throw new AccessDeniedException(
//                    "Subscription with id " + id + " does not belong to this User."
//            );
//        }
//        log.info("Starting async payment for subscription {}", id);
//        startAsyncPayment(sub);
    }

//    @Override
//    @Async
//    @Transactional
//    public void startAsyncPayment(Subscription subscription) {
//        long id = subscription.getId();
//        log.debug("Async payment started for subscription {}", id);
//        boolean isPaid = paymentClient.waitForPayment(id);
//        if (!isPaid) {
//            log.info("Payment failed for subscription {}", id);
//            subscription.setStatus(StatusState.UNPAID);
//            repository.save(subscription);
//            return;
//        }
//        LocalDate now = LocalDate.now();
//        LocalDate expiration = subscription.getExpirationDate();
//        if (expiration == null || !expiration.isAfter(now)) {
//            subscription.setExpirationDate(now.plusMonths(1));
//        } else {
//            subscription.setExpirationDate(expiration.plusMonths(1));
//        }
//        subscription.setStatus(StatusState.PAID);
//        repository.save(subscription);
//        log.info("Payment successful for subscription {}, new expiration={}",
//                id, subscription.getExpirationDate());
//    }

    private void validateId(long id) {
        if (id <= 0) throw new IllegalArgumentException("Id must be positive");
    }

    private String normalizePrefix(String prefix) {
        Objects.requireNonNull(prefix, "pathPrefix must not be null");
        String p = prefix.trim();
        if (!p.matches("^[A-Za-z0-9_-]{3,50}$")) {
            throw new IllegalArgumentException("Invalid pathPrefix format");
        }
        return p;
    }
}