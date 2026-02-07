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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {
    private SubscriptionJpaRepository repository;

    private UserService service;

    private Converter<Subscription, SubscriptionCreateDTO, SubscriptionDTO> converter;

    @Override
    public SubscriptionDTO create(SubscriptionCreateDTO dto) {
        User currentUser = service.findById(0);
        Optional<Subscription> found = repository.findAll().stream()
                .filter(s -> s.getPathPrefix().equals(dto.getPathPrefix())).findFirst();
        if (found.isPresent()) {
            if (found.get().getExpirationDate() == null
                    || found.get().getExpirationDate().isBefore(LocalDate.now().plusDays(1))) {
                found.get().setCreationDate(LocalDate.now());
                found.get().setStatus(StatusState.UNPAID);
                found.get().setUser(currentUser);
                return converter.entityToDto(found.get());
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
        return service.findById(userId).getSubscriptions().stream()
                .map(s -> converter.entityToDto(s)).collect(Collectors.toList());
    }

    @Override
    public SubscriptionDTO findById(long id) {
        return repository.findById(id).map(converter::entityToDto)
                .orElseThrow(() -> new IdNotFoundException("Subscription with id " + id + " is not found."));
    }

    @Override
    public void remove(long id) {
        repository.deleteById(id);
    }

    @Override
    public void makePayment(long id, long userId) {
        Subscription subscriptionFromDB = repository.findById(id)
                .orElseThrow(() -> new IdNotFoundException("Subscription with id " + id + " is not found."));
        if (subscriptionFromDB.getUser().getId() != userId) {
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
    }
}
