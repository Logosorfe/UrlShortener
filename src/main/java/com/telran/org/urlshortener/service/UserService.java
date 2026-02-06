package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.entity.User;
import com.telran.org.urlshortener.model.RoleType;

import java.util.List;

public interface UserService {
    User create(User user);

    List<User> findAll();

    User findById(long id);

    User update(long id, RoleType newRole);

    void remove(long id);

    User findByEmail(String email);
}
