package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.entity.Subscription;
import com.telran.org.urlshortener.entity.User;
import com.telran.org.urlshortener.exception.IdNotFoundException;
import com.telran.org.urlshortener.exception.PathPrefixNotAvailableException;
import com.telran.org.urlshortener.exception.UserNotFoundException;
import com.telran.org.urlshortener.model.StatusState;
import com.telran.org.urlshortener.repository.SubscriptionJpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {
    SubscriptionJpaRepository repository;

    UserService service;

    @Override
    public Subscription create(Subscription subscription) {
        User currentUser = service.findById(0);
        Optional<Subscription> found = service.findAll().stream()
                .flatMap(u -> u.getSubscriptions().stream())
                .filter(s -> s.getPathPrefix().equals(subscription.getPathPrefix())).findFirst();
        if (found.isPresent()) {
            if (found.get().getExpirationDate() == null
                    || found.get().getExpirationDate().isBefore(LocalDate.now().plusDays(1))) {
                found.get().setCreationDate(LocalDate.now());
                found.get().setStatus(StatusState.UNPAID);
                found.get().setUser(currentUser);
                return found.get();
            }
            throw new PathPrefixNotAvailableException("This pathPrefix \"" + subscription.getPathPrefix()
                    + "\" is not available till " + found.get().getExpirationDate());
        }
        subscription.setCreationDate(LocalDate.now());
        subscription.setUser(currentUser);
        return repository.save(subscription);
    }

    @Override
    public List<Subscription> findByUserId(long userId) {
        return service.findById(userId).getSubscriptions();
    }

    @Override
    public Subscription findById(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IdNotFoundException("Subscription with id " + id + " is not found."));
    }

    @Override
    public void remove(long id) {
        repository.deleteById(id);
    }

    @Override
    public void makePayment(long id, long userId) {
        Subscription blank = findById(id);
        if (blank.getUser().getId() != userId) {
            throw new UserNotFoundException("Subscription with id " + id + " does not belong to User with id "
                    + userId + ". User needs to try creating the same subscription.");
        }
        if (blank.getExpirationDate() == null
                || blank.getExpirationDate().isBefore(LocalDate.now().plusDays(1))) {
            blank.setExpirationDate(LocalDate.now().plusMonths(1));
        } else blank.setExpirationDate(blank.getExpirationDate().plusMonths(1));
        blank.setStatus(StatusState.PAID);
    }
}
