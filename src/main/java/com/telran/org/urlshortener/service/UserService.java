package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.dto.UserCreateDTO;
import com.telran.org.urlshortener.dto.UserDTO;
import com.telran.org.urlshortener.entity.User;
import com.telran.org.urlshortener.model.RoleType;

import java.util.List;

public interface UserService {
    UserDTO create(UserCreateDTO dto);

    List<UserDTO> findAll();

    User findById(long id);

    UserDTO update(long id, RoleType newRole);

    void remove(long id);

    UserDTO findByEmail(String email);
}
