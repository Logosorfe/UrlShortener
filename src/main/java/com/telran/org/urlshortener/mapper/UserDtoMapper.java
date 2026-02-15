package com.telran.org.urlshortener.mapper;

import com.telran.org.urlshortener.dto.UserCreateDTO;
import com.telran.org.urlshortener.dto.UserDTO;
import com.telran.org.urlshortener.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between User entities and User DTOs.
 * Handles transformation of user data for API responses and persistence.
 */
@Component
public class UserDtoMapper implements Converter<User, UserCreateDTO, UserDTO> {
    /**
     * Converts a User entity into a UserDTO.
     *
     * @param user the entity to convert
     * @return the corresponding DTO
     */
    @Override
    public UserDTO entityToDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
    /**
     * Converts a UserCreateDTO into a User entity.
     *
     * @param dto the DTO containing user creation data
     * @return the new User entity */
    @Override
    public User dtoToEntity(UserCreateDTO dto) {
        return new User(dto.getEmail(), dto.getPassword());
    }
}