package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.entity.Subscription;
import com.telran.org.urlshortener.entity.UrlBinding;
import com.telran.org.urlshortener.entity.User;
import com.telran.org.urlshortener.exception.IdNotFoundException;
import com.telran.org.urlshortener.exception.PathPrefixNotAvailableException;
import com.telran.org.urlshortener.repository.UrlBindingJpaRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class UrlBindingServiceImpl implements UrlBindingService {
    UrlBindingJpaRepository repository;

    UserService service;

    @Override
    public UrlBinding create(UrlBinding urlBinding, String pathPrefix) {
        User currentUser = service.findById(0);
        String pathSuffix = Base64.getEncoder()
                .encodeToString(urlBinding.getOriginalUrl().getBytes(StandardCharsets.UTF_8)).substring(1, 8);
        if (pathPrefix == null || pathPrefix.isEmpty()) {
            Optional<UrlBinding> noPrefix = service.findAll().stream()
                    .flatMap(u -> u.getUrlBindings().stream())
                    .filter(ub -> ub.getUId().equals("/" + pathSuffix)).findFirst();
            if (noPrefix.isPresent()) {
                noPrefix.get().setUser(currentUser);
                return noPrefix.get();
            }
            urlBinding.setUId("/" + pathSuffix);
            urlBinding.setUser(currentUser);
            return repository.save(urlBinding);
        }
        Optional<Subscription> userActivePrefix = currentUser.getSubscriptions().stream()
                .filter(s -> s.getPathPrefix().equals(pathPrefix))
                .filter(s -> s.getExpirationDate().isAfter(LocalDate.now())).findFirst();
        if (userActivePrefix.isPresent()) {
            Optional<UrlBinding> withPrefix = service.findAll().stream()
                    .flatMap(u -> u.getUrlBindings().stream())
                    .filter(ub -> ub.getUId().equals(pathPrefix + "/" + pathSuffix)).findFirst();
            if (withPrefix.isPresent()) {
                withPrefix.get().setUser(currentUser);
                withPrefix.get().setCount(0L);
                return withPrefix.get();
            }
            urlBinding.setUId(pathPrefix + "/" + pathSuffix);
            urlBinding.setUser(currentUser);
            return repository.save(urlBinding);
        }
        throw new PathPrefixNotAvailableException("Can not create UrlBinding, as Subscription with prefix \""
                + pathPrefix + "\" is neither active nor belongs to user.");
    }

    @Override
    public List<UrlBinding> findAllByUserId(long userId) {
        return service.findById(userId).getUrlBindings();
    }

    @Override
    public UrlBinding find(String uId) {
        return repository.findByUId(uId)
                .orElseThrow(() -> new IdNotFoundException("Unique id " + uId + " is not found."));
    }

    @Override
    public UrlBinding reset(long id) {
        UrlBinding urlBindingFromDB = repository.findById(id)
                .orElseThrow(() -> new IdNotFoundException("UrlBinding with id " + id + " is not found."));
        urlBindingFromDB.setCount(0L);
        return urlBindingFromDB;
    }

    @Override
    public void remove(long id) {
        repository.deleteById(id);
    }
}