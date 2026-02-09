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
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(long id) {
        if (repository.existsById(id)) repository.deleteById(id);
        else throw new UserNotFoundException("User with id " + id + " is not found.");
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public UserDTO findByEmail(String email) {
        User userFromDB = repository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " is not found."));
        return converter.entityToDto(userFromDB);
    }
}
