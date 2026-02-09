package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.dto.SubscriptionCreateDTO;
import com.telran.org.urlshortener.dto.SubscriptionDTO;
import com.telran.org.urlshortener.entity.Subscription;
import com.telran.org.urlshortener.entity.User;
import com.telran.org.urlshortener.exception.IdNotFoundException;
import com.telran.org.urlshortener.exception.PathPrefixNotAvailableException;
import com.telran.org.urlshortener.exception.UserNotFoundException;
import com.telran.org.urlshortener.mapper.Converter;
import com.telran.org.urlshortener.model.StatusState;
import com.telran.org.urlshortener.repository.SubscriptionJpaRepository;
import lombok.RequiredArgsConstructor;
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

    private final Converter<Subscription, SubscriptionCreateDTO, SubscriptionDTO> converter;

    @Override
    public SubscriptionDTO create(SubscriptionCreateDTO dto) {
        User currentUser = new User();
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
                    + "\" is not available till " + found.get().getExpirationDate());
        }
        Subscription subscriptionBeforeDB = converter.dtoToEntity(dto);
        subscriptionBeforeDB.setCreationDate(LocalDate.now());
        subscriptionBeforeDB.setUser(currentUser);
        return converter.entityToDto(repository.save(subscriptionBeforeDB));
    }

    @Override
    public List<SubscriptionDTO> findByUserId(long userId) {
        return repository.findByUserId(userId).stream()
                .map(converter::entityToDto).collect(Collectors.toList());
    }

    @Override
    public SubscriptionDTO findById(long id) {
        return repository.findById(id).map(converter::entityToDto)
                .orElseThrow(() -> new IdNotFoundException("Subscription with id " + id + " is not found."));
    }

    @Override
    public void delete(long id) {
        if (repository.existsById(id)) repository.deleteById(id);
        else throw new IdNotFoundException("Subscription with id " + id + " is not found.");
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void makePayment(long id, long userId) {
        Subscription subscriptionFromDB = repository.findById(id)
                .orElseThrow(() -> new IdNotFoundException("Subscription with id " + id + " is not found."));
        if (!subscriptionFromDB.getUser().getId().equals(userId)) {
            throw new UserNotFoundException("Subscription with id " + id + " does not belong to User with id "
                    + userId + ". User needs to try creating the same subscription.");
        }
        if (subscriptionFromDB.getExpirationDate() == null
                || subscriptionFromDB.getExpirationDate().isBefore(LocalDate.now().plusDays(1))) {
            subscriptionFromDB.setExpirationDate(LocalDate.now().plusMonths(1));
        } else {
            subscriptionFromDB.setExpirationDate(subscriptionFromDB.getExpirationDate().plusMonths(1));
        }
        subscriptionFromDB.setStatus(StatusState.PAID);
        repository.save(subscriptionFromDB);
    }
}
