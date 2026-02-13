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
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
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
    @Transactional
    public UrlBindingDTO create(UrlBindingCreateDTO dto, String pathPrefix) {
        User currentUser = service.getCurrentUser();
        log.debug("create UrlBinding userId={}, prefix={}, originalUrlLength={}",
                currentUser.getId(), pathPrefix, dto.getOriginalUrl() != null ? dto.getOriginalUrl().length() : 0);
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
        byte[] hash = md.digest(dto.getOriginalUrl().getBytes(StandardCharsets.UTF_8));
        String pathSuffix = Base64.getUrlEncoder().withoutPadding().encodeToString(hash).substring(0, 8);
        if (pathPrefix == null || pathPrefix.isEmpty()) {
            log.debug("Creating UrlBinding without prefix, suffix={}", pathSuffix);
            Optional<UrlBinding> existing = repository.findByUId("/" + pathSuffix);
            if (existing.isPresent()) {
                log.info("Existing UrlBinding reused id={} for user={}", existing.get().getId(), currentUser.getId());
                existing.get().setUser(currentUser);
                return converter.entityToDto(repository.save(existing.get()));
            }
            UrlBinding newBinding = converter.dtoToEntity(dto);
            newBinding.setUId("/" + pathSuffix);
            newBinding.setUser(currentUser);
            UrlBinding saved = repository.save(newBinding);
            log.info("UrlBinding created id={}, uId={}", saved.getId(), saved.getUId());
            return converter.entityToDto(saved);
        }
        log.debug("Checking subscription for prefix={} userId={}", pathPrefix, currentUser.getId());
        Optional<Subscription> activePrefix = currentUser.getSubscriptions().stream()
                .filter(s -> s.getPathPrefix().equals(pathPrefix))
                .filter(s -> s.getExpirationDate() != null
                        && s.getExpirationDate().isAfter(LocalDate.now()))
                .findFirst();
        if (activePrefix.isPresent()) {
            log.debug("Active subscription found for prefix={}", pathPrefix);
            String fullUId = pathPrefix + "/" + pathSuffix;
            Optional<UrlBinding> existing = repository.findByUId(fullUId);
            if (existing.isPresent()) {
                log.info("Existing UrlBinding reused id={} for user={}", existing.get().getId(), currentUser.getId());
                existing.get().setUser(currentUser);
                existing.get().setCount(0L);
                return converter.entityToDto(repository.save(existing.get()));
            }
            UrlBinding newBinding = converter.dtoToEntity(dto);
            newBinding.setUId(fullUId);
            newBinding.setUser(currentUser);
            UrlBinding saved = repository.save(newBinding);
            log.info("UrlBinding created id={}, uId={}", saved.getId(), saved.getUId());
            return converter.entityToDto(saved);
        }
        log.warn("Prefix {} is not available for user {}", pathPrefix, currentUser.getId());
        throw new PathPrefixNotAvailableException(
                "Can not create Url binding, as Subscription with prefix \"" + pathPrefix
                        + "\" is neither active nor belongs to user.");
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional(readOnly = true)
    public List<UrlBindingDTO> findAllByUserId(long userId) {
        User currentUser = service.getCurrentUser();
        log.debug("findAllByUserId requestedUserId={} by user={}", userId, currentUser.getId());
        if (currentUser.getRole() == RoleType.ROLE_USER && !Objects.equals(currentUser.getId(), userId)) {
            log.warn("User {} attempted to access bindings of user {}", currentUser.getId(), userId);
            throw new AccessDeniedException("You do not have permission to access this list.");
        }
        List<UrlBindingDTO> list = repository.findByUserId(userId).stream()
                .map(converter::entityToDto)
                .collect(Collectors.toList());
        log.info("Returned {} UrlBindings for userId={}", list.size(), userId);
        return list;
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional(readOnly = true)
    public UrlBindingDTO find(String uId) {
        User currentUser = service.getCurrentUser();
        log.debug("find UrlBinding uId={} by user={}", uId, currentUser.getId());
        UrlBinding binding = repository.findByUId(uId)
                .orElseThrow(() -> {
                    log.warn("UrlBinding not found uId={}", uId);
                    return new IdNotFoundException("Unique id " + uId + " is not found.");
                });
        if (currentUser.getRole() == RoleType.ROLE_USER &&
                !Objects.equals(binding.getUser().getId(), currentUser.getId())) {
            log.warn("User {} attempted to access UrlBinding {} belonging to {}", currentUser.getId(), uId,
                    binding.getUser().getId());
            throw new AccessDeniedException("You do not have permission to delete this URL binding.");
        }
        log.info("UrlBinding found id={}, uId={}", binding.getId(), uId);
        return converter.entityToDto(binding);
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    @Transactional
    public UrlBindingDTO reset(long id) {
        User currentUser = service.getCurrentUser();
        log.debug("reset UrlBinding id={} by user={}", id, currentUser.getId());
        UrlBinding binding = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("UrlBinding not found id={}", id);
                    return new IdNotFoundException("Url binding with id " + id + " is not found.");
                });
        if (!Objects.equals(binding.getUser().getId(), currentUser.getId())) {
            log.warn("User {} attempted to reset UrlBinding {} belonging to {}", currentUser.getId(), id, binding.getUser().getId());
            throw new AccessDeniedException("You do not have permission to reset count of this URL binding.");
        }
        binding.setCount(0L);
        UrlBinding saved = repository.save(binding);
        log.info("UrlBinding reset id={}, newCount=0", id);
        return converter.entityToDto(saved);
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    @Transactional
    public void delete(long id) {
        User currentUser = service.getCurrentUser();
        log.debug("delete UrlBinding id={} by user={}", id, currentUser.getId());
        UrlBinding binding = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("UrlBinding not found id={}", id);
                    return new IdNotFoundException("Url binding with id " + id + " is not found.");
                });
        if (!Objects.equals(binding.getUser().getId(), currentUser.getId())) {
            log.warn("User {} attempted to delete UrlBinding {} belonging to {}", currentUser.getId(), id, binding.getUser().getId());
            throw new AccessDeniedException("You do not have permission to delete this URL binding.");
        }
        repository.delete(binding);
        log.info("UrlBinding deleted id={}", id);
    }
}