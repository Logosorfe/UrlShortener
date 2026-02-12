package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.dto.UrlBindingCreateDTO;
import com.telran.org.urlshortener.dto.UrlBindingDTO;
import com.telran.org.urlshortener.entity.Subscription;
import com.telran.org.urlshortener.entity.UrlBinding;
import com.telran.org.urlshortener.entity.User;
import com.telran.org.urlshortener.exception.IdNotFoundException;
import com.telran.org.urlshortener.exception.PathPrefixNotAvailableException;
import com.telran.org.urlshortener.mapper.Converter;
import com.telran.org.urlshortener.model.RoleType;
import com.telran.org.urlshortener.repository.UrlBindingJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlBindingServiceImpl implements UrlBindingService {
    private final UrlBindingJpaRepository repository;

    private final UserService service;

    private final Converter<UrlBinding, UrlBindingCreateDTO, UrlBindingDTO> converter;

    @Override
    @PreAuthorize("hasRole('USER')")
    public UrlBindingDTO create(UrlBindingCreateDTO dto, String pathPrefix) {
        User currentUser = service.getCurrentUser();
        String pathSuffix = Base64.getEncoder().withoutPadding()
                .encodeToString(dto.getOriginalUrl().getBytes(StandardCharsets.UTF_8)).substring(0, 8);
        if (pathPrefix == null || pathPrefix.isEmpty()) {
            Optional<UrlBinding> noPrefix = repository.findByUId("/" + pathSuffix);
            if (noPrefix.isPresent()) {
                noPrefix.get().setUser(currentUser);
                return converter.entityToDto(repository.save(noPrefix.get()));
            }
            UrlBinding urlBindingBeforeDB = converter.dtoToEntity(dto);
            urlBindingBeforeDB.setUId("/" + pathSuffix);
            urlBindingBeforeDB.setUser(currentUser);
            return converter.entityToDto(repository.save(urlBindingBeforeDB));
        }
        Optional<Subscription> userActivePrefix = currentUser.getSubscriptions().stream()
                .filter(s -> s.getPathPrefix().equals(pathPrefix))
                .filter(s -> s.getExpirationDate().isAfter(LocalDate.now())).findFirst();
        if (userActivePrefix.isPresent()) {
            Optional<UrlBinding> withPrefix = repository.findByUId(pathPrefix + "/" + pathSuffix);
            if (withPrefix.isPresent()) {
                withPrefix.get().setUser(currentUser);
                withPrefix.get().setCount(0L);
                return converter.entityToDto(repository.save(withPrefix.get()));
            }
            UrlBinding urlBindingBeforeDB = converter.dtoToEntity(dto);
            urlBindingBeforeDB.setUId(pathPrefix + "/" + pathSuffix);
            urlBindingBeforeDB.setUser(currentUser);
            return converter.entityToDto(repository.save(urlBindingBeforeDB));
        }
        throw new PathPrefixNotAvailableException("Can not create Url binding, as Subscription with prefix \""
                + pathPrefix + "\" is neither active nor belongs to user.");
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<UrlBindingDTO> findAllByUserId(long userId) {
        User currentUser = service.getCurrentUser();
        if (currentUser.getRole() == RoleType.ROLE_USER && currentUser.getId() != userId) {
            throw new AccessDeniedException("You do not have permission to access this list.");
        }
        return repository.findByUserId(userId).stream()
                .map(converter::entityToDto).collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public UrlBindingDTO find(String uId) {
        User currentUser = service.getCurrentUser();
        UrlBinding urlBindingFromDB = repository.findByUId(uId)
                .orElseThrow(() -> new IdNotFoundException("Unique id " + uId + " is not found."));
        if (currentUser.getRole() == RoleType.ROLE_USER
                && !urlBindingFromDB.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this URL binding.");
        }
        return converter.entityToDto(urlBindingFromDB);
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public UrlBindingDTO reset(long id) {
        UrlBinding urlBindingFromDB = repository.findById(id)
                .orElseThrow(() -> new IdNotFoundException("Url binding with id " + id + " is not found."));
        if (!urlBindingFromDB.getUser().getId().equals(service.getCurrentUser().getId())) {
            throw new AccessDeniedException("You do not have permission to reset count of this URL binding.");
        }
        urlBindingFromDB.setCount(0L);
        return converter.entityToDto(repository.save(urlBindingFromDB));
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public void delete(long id) {
        UrlBinding urlBindingFromDB = repository.findById(id)
                .orElseThrow(() -> new IdNotFoundException("Url binding with id " + id + " is not found."));
        if (!urlBindingFromDB.getUser().getId().equals(service.getCurrentUser().getId())) {
            throw new AccessDeniedException("You do not have permission to delete this URL binding.");
        }
        repository.delete(urlBindingFromDB);
    }
}