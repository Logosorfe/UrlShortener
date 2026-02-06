package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.entity.User;
import com.telran.org.urlshortener.exception.EmailNotUniqueException;
import com.telran.org.urlshortener.exception.UserNotFoundException;
import com.telran.org.urlshortener.model.RoleType;
import com.telran.org.urlshortener.repository.UserJpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private UserJpaRepository repository;

    @Override
    public User create(User user) {
        Optional<User> userFromDB = repository.findByEmail(user.getEmail());
        if (userFromDB.isPresent()) {
            throw new EmailNotUniqueException("User with email " + user.getEmail() + " already exists.");
        }
        return repository.save(user);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll();
    }

    @Override
    public User findById(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " is not found."));
    }

    @Override
    public User update(long id, RoleType newRole) {
        User userFromDB = findById(id);
        userFromDB.setRole(newRole);
        return userFromDB;
    }

    @Override
    public void remove(long id) {
        repository.deleteById(id);
    }

    @Override
    public User findByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " is not found."));
    }
}
