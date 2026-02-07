package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.dto.UserCreateDTO;
import com.telran.org.urlshortener.dto.UserDTO;
import com.telran.org.urlshortener.entity.User;
import com.telran.org.urlshortener.exception.EmailNotUniqueException;
import com.telran.org.urlshortener.exception.UserNotFoundException;
import com.telran.org.urlshortener.mapper.Converter;
import com.telran.org.urlshortener.model.RoleType;
import com.telran.org.urlshortener.repository.UserJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private UserJpaRepository repository;

    private Converter<User, UserCreateDTO, UserDTO> converter;

    @Override
    public UserDTO create(UserCreateDTO dto) {
        Optional<User> userFromDB = repository.findByEmail(dto.getEmail());
        if (userFromDB.isPresent()) {
            throw new EmailNotUniqueException("User with email " + dto.getEmail() + " already exists.");
        }
        User userBeforeDB = converter.dtoToEntity(dto);
        return converter.entityToDto(repository.save(userBeforeDB));
    }

    @Override
    public List<UserDTO> findAll() {
        return repository.findAll().stream()
                .map(u -> converter.entityToDto(u)).collect(Collectors.toList());
    }

    @Override
    public User findById(long id) {//UserDTO
        return repository.findById(id)
                //.map(u -> convertor.entityToDto(u))
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " is not found."));
    }

    @Override
    public UserDTO update(long id, RoleType newRole) {
        User userFromDB = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " is not found."));
        userFromDB.setRole(newRole);
        return converter.entityToDto(userFromDB);
    }

    @Override
    public void remove(long id) {
        repository.deleteById(id);
    }

    @Override
    public UserDTO findByEmail(String email) {
        User userFromDB = repository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " is not found."));
        return converter.entityToDto(userFromDB);
    }
}
