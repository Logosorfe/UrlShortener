package com.telran.org.urlshortener.mapper;

import com.telran.org.urlshortener.dto.UserCreateDTO;
import com.telran.org.urlshortener.dto.UserDTO;
import com.telran.org.urlshortener.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserDtoMapper implements Converter<User, UserCreateDTO, UserDTO> {
    @Override
    public UserDTO entityToDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public User dtoToEntity(UserCreateDTO dto) {
        return new User(dto.getEmail(), dto.getPassword());
    }
}
