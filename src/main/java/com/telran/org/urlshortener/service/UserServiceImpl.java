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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserJpaRepository repository;

    private final PasswordEncoder encoder;

    private final Converter<User, UserCreateDTO, UserDTO> converter;

    @Override
    public UserDTO create(UserCreateDTO dto) {
        Optional<User> userFromDB = repository.findByEmail(dto.getEmail());
        if (userFromDB.isPresent()) {
            throw new EmailNotUniqueException("User with email " + dto.getEmail() + " already exists.");
        }
        dto.setPassword(encoder.encode(dto.getPassword()));
        return converter.entityToDto(repository.save(converter.dtoToEntity(dto)));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDTO> findUsers() {
        return repository.findAll().stream()
                .map(converter::entityToDto).collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserDTO findById(long id) {
        return repository.findById(id).map(converter::entityToDto)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " is not found."));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserDTO update(long id, RoleType newRole) {
        User userFromDB = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " is not found."));
        userFromDB.setRole(newRole);
        return converter.entityToDto(repository.save(userFromDB));
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void delete(long id) {
        User currentUser = getCurrentUser();
        User userToDelete = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " is not found."));
        if (currentUser.getRole() == RoleType.ROLE_USER && currentUser.getId() != id) {
            throw new AccessDeniedException("You do not have permission to delete another user.");
        }
        repository.delete(userToDelete);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public UserDTO findByEmail(String email) {
        User currentUser = getCurrentUser();
        User userFromDB = repository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " is not found."));
        if (currentUser.getRole() == RoleType.ROLE_USER && !currentUser.getEmail().equals(email)) {
            throw new AccessDeniedException("You do not have permission to view this user.");
        }
        return converter.entityToDto(userFromDB);
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return repository.findByEmail(username)
                    .orElseThrow(() -> new UserNotFoundException("User with username "
                            + username + " is not found."));
        } else {
            throw new IllegalArgumentException("Cannot obtain user from authentication principal");
        }
    }
}