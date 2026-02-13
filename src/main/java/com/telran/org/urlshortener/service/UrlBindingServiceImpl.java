package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.dto.UrlBindingCreateDTO;
import com.telran.org.urlshortener.dto.UrlBindingDTO;
import com.telran.org.urlshortener.entity.UrlBinding;
import com.telran.org.urlshortener.entity.User;
import com.telran.org.urlshortener.exception.IdNotFoundException;
import com.telran.org.urlshortener.exception.PathPrefixAvailabilityException;
import com.telran.org.urlshortener.mapper.Converter;
import com.telran.org.urlshortener.model.RoleType;
import com.telran.org.urlshortener.repository.UrlBindingJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
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
        Objects.requireNonNull(dto, "UrlBindingCreateDTO must not be null");
        String originalUrl = normalizeUrl(dto.getOriginalUrl());
        validateUrl(originalUrl);
        User currentUser = service.getCurrentUser();
        log.debug("create UrlBinding userId={}, prefix={}, url={}", currentUser.getId(), pathPrefix,
                maskUrl(originalUrl));
        String normalizedPrefix = normalizePrefix(pathPrefix);
        String pathSuffix;
        try {
            pathSuffix = generateSuffix(originalUrl);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
        if (normalizedPrefix == null) {
            String uId = "/" + pathSuffix;
            log.debug("Creating UrlBinding without prefix, suffix={}", pathSuffix);
            Optional<UrlBinding> existing = repository.findByUId(uId);
            if (existing.isPresent()) {
                UrlBinding reused = existing.get();
                reused.setUser(currentUser);
                reused.setCount(0L);
                log.info("Reusing existing UrlBinding id={} for user={}", reused.getId(), currentUser.getId());
                return converter.entityToDto(repository.save(reused));
            }
            UrlBinding newBinding = converter.dtoToEntity(dto);
            newBinding.setUId(uId);
            newBinding.setUser(currentUser);
            UrlBinding saved = repository.save(newBinding);
            log.info("UrlBinding created id={}, uId={}", saved.getId(), saved.getUId());
            return converter.entityToDto(saved);
        }
        log.debug("Checking subscription for prefix={} userId={}", normalizedPrefix, currentUser.getId());
        try {
            validateUserPrefix(currentUser, normalizedPrefix);
        } catch (PathPrefixAvailabilityException e) {
            log.warn("Prefix {} is not available for user {}", normalizedPrefix, currentUser.getId());
            throw new PathPrefixAvailabilityException("Subscription with prefix \"" + normalizedPrefix
                    + "\" is not active or does not belong to user.");
        }
        log.debug("Active subscription found for prefix={}", normalizedPrefix);
        String fullUId = "/" + normalizedPrefix + "/" + pathSuffix;
        Optional<UrlBinding> existing = repository.findByUId(fullUId);
        if (existing.isPresent()) {
            log.info("Existing UrlBinding reused id={} for user={}", existing.get().getId(), currentUser.getId());
            UrlBinding reused = existing.get();
            reused.setUser(currentUser);
            reused.setCount(0L);
            log.info("Reusing existing UrlBinding id={} for user={}", reused.getId(), currentUser.getId());
            return converter.entityToDto(repository.save(reused));
        }
        UrlBinding newBinding = converter.dtoToEntity(dto);
        newBinding.setUId(fullUId);
        newBinding.setUser(currentUser);
        UrlBinding saved = repository.save(newBinding);
        log.info("UrlBinding created id={}, uId={}", saved.getId(), saved.getUId());
        return converter.entityToDto(saved);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional(readOnly = true)
    public List<UrlBindingDTO> findAllByUserId(long userId) {
        validateId(userId);
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
        String normalized = normalizeUId(uId);
        User currentUser = service.getCurrentUser();
        log.debug("find UrlBinding uId={} by user={}", normalized, currentUser.getId());
        UrlBinding binding = repository.findByUId(normalized)
                .orElseThrow(() -> {
                    log.warn("UrlBinding not found uId={}", normalized);
                    return new IdNotFoundException("Unique id " + normalized + " is not found.");
                });
        if (currentUser.getRole() == RoleType.ROLE_USER &&
                !Objects.equals(binding.getUser().getId(), currentUser.getId())) {
            log.warn("User {} attempted to access UrlBinding {} belonging to {}", currentUser.getId(), normalized,
                    binding.getUser().getId());
            throw new AccessDeniedException("You do not have permission to delete this URL binding.");
        }
        log.info("UrlBinding found id={}, uId={}", binding.getId(), normalized);
        return converter.entityToDto(binding);
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    @Transactional
    public UrlBindingDTO reset(long id) {
        validateId(id);
        User currentUser = service.getCurrentUser();
        log.debug("reset UrlBinding id={} by user={}", id, currentUser.getId());
        UrlBinding binding = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("UrlBinding not found id={}", id);
                    return new IdNotFoundException("Url binding with id " + id + " is not found.");
                });
        if (!Objects.equals(binding.getUser().getId(), currentUser.getId())) {
            log.warn("User {} attempted to reset UrlBinding {} belonging to {}", currentUser.getId(), id,
                    binding.getUser().getId());
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
        validateId(id);
        User currentUser = service.getCurrentUser();
        log.debug("delete UrlBinding id={} by user={}", id, currentUser.getId());
        UrlBinding binding = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("UrlBinding not found id={}", id);
                    return new IdNotFoundException("Url binding with id " + id + " is not found.");
                });
        if (!Objects.equals(binding.getUser().getId(), currentUser.getId())) {
            log.warn("User {} attempted to delete UrlBinding {} belonging to {}", currentUser.getId(), id,
                    binding.getUser().getId());
            throw new AccessDeniedException("You do not have permission to delete this URL binding.");
        }
        repository.delete(binding);
        log.info("UrlBinding deleted id={}", id);
    }

    private void validateId(long id) {
        if (id <= 0) throw new IllegalArgumentException("Id must be positive");
    }

    private String normalizeUrl(String url) {
        return Objects.requireNonNull(url, "originalUrl must not be null").trim();
    }

    private void validateUrl(String url) {
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equalsIgnoreCase("http")
                    || scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("URL must use http or https");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL format");
        }
    }

    private String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) return null;
        String p = prefix.trim();
        if (!p.matches("^[A-Za-z0-9_-]{3,50}$")) {
            throw new IllegalArgumentException("Invalid pathPrefix format");
        }
        return p;
    }

    private void validateUserPrefix(User user, String prefix) throws PathPrefixAvailabilityException {
        boolean hasActive = user.getSubscriptions().stream()
                .anyMatch(s -> prefix.equals(s.getPathPrefix()) && s.getExpirationDate() != null
                        && s.getExpirationDate().isAfter(LocalDate.now()));
        if (!hasActive) {
            throw new PathPrefixAvailabilityException("Subscription with prefix \"" + prefix
                    + "\" is not active or does not belong to user.");
        }
    }

    private String normalizeUId(String uId) {
        Objects.requireNonNull(uId, "uId must not be null");
        String trimmed = uId.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException("uId must not be blank");
        if (!trimmed.startsWith("/")) trimmed = "/" + trimmed;
        return trimmed;
    }

    private String generateSuffix(String originalUrl) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(originalUrl.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash).substring(0, 8);
    }

    private String maskUrl(String url) {
        if (url.length() <= 10) return "***";
        return url.substring(0, 5) + "..." + url.substring(url.length() - 5);
    }
}