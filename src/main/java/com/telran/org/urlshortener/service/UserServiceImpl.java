package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.dto.UserCreateDTO;
import com.telran.org.urlshortener.dto.UserDTO;
import com.telran.org.urlshortener.entity.User;
import com.telran.org.urlshortener.exception.EmailNotUniqueException;
import com.telran.org.urlshortener.exception.UserNotFoundException;
import com.telran.org.urlshortener.mapper.Converter;
import com.telran.org.urlshortener.model.RoleType;
import com.telran.org.urlshortener.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserJpaRepository repository;

    private final PasswordEncoder encoder;

    private final Converter<User, UserCreateDTO, UserDTO> converter;

    @Override
    @Transactional
    public UserDTO create(UserCreateDTO dto) {
        log.debug("create user email={}", dto.getEmail());
        Optional<User> existing = repository.findByEmail(dto.getEmail());
        if (existing.isPresent()) {
            log.warn("User creation failed — email already exists: {}", dto.getEmail());
            throw new EmailNotUniqueException("User with email " + dto.getEmail() + " already exists.");
        }
        User user = converter.dtoToEntity(dto);
        user.setPassword(encoder.encode(user.getPassword()));
        User saved = repository.save(user);
        log.info("User created id={}, email={}", saved.getId(), saved.getEmail());
        return converter.entityToDto(saved);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<UserDTO> findAll() {
        log.debug("findAll users");
        List<UserDTO> list = repository.findAll().stream()
                .map(converter::entityToDto)
                .collect(Collectors.toList());
        log.info("findAll returned {} users", list.size());
        return list;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public UserDTO findById(long id) {
        log.debug("findById id={}", id);
        return repository.findById(id)
                .map(user -> {
                    log.info("User found id={}", id);
                    return converter.entityToDto(user);
                })
                .orElseThrow(() -> {
                    log.warn("User not found id={}", id);
                    return new UserNotFoundException("User with id " + id + " is not found.");
                });
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserDTO update(long id, RoleType newRole) {
        log.debug("update user id={}, newRole={}", id, newRole);
        User user = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User update failed — not found id={}", id);
                    return new UserNotFoundException("User with id " + id + " is not found.");
                });
        user.setRole(newRole);
        User saved = repository.save(user);
        log.info("User updated id={}, newRole={}", id, newRole);
        return converter.entityToDto(saved);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional
    public void delete(long id) {
        User current = getCurrentUser();
        log.debug("delete user id={} by user={}", id, current.getId());
        User userToDelete = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User delete failed — not found id={}", id);
                    return new UserNotFoundException("User with id " + id + " is not found.");
                });
        if (current.getRole() == RoleType.ROLE_USER && !Objects.equals(current.getId(), id)) {
            log.warn("User {} attempted to delete another user {}", current.getId(), id);
            throw new AccessDeniedException("You do not have permission to delete another user.");
        }
        repository.delete(userToDelete);
        log.info("User deleted id={}", id);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional(readOnly = true)
    public UserDTO findByEmail(String email) {
        User current = getCurrentUser();
        log.debug("findByEmail email={} by user={}", email, current.getId());
        User user = repository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found by email={}", email);
                    return new UserNotFoundException("User with email " + email + " is not found.");
                });
        if (current.getRole() == RoleType.ROLE_USER && !Objects.equals(current.getEmail(), email)) {
            log.warn("User {} attempted to access another user's data {}", current.getId(), email);
            throw new AccessDeniedException("You do not have permission to view this user.");
        }
        log.info("User found by email={}", email);
        return converter.entityToDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            log.warn("getCurrentUser failed — no authenticated user");
            throw new SecurityException("User is not authenticated");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails details) {
            String email = details.getUsername();
            log.debug("getCurrentUser principal email={}", email);
            return repository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.error("Authenticated user not found in DB email={}", email);
                        return new UserNotFoundException("User with username " + email + " is not found.");
                    });
        }
        log.error("Invalid authentication principal type={}", principal.getClass().getName());
        throw new IllegalArgumentException("Cannot obtain user from authentication principal");
    }
}