package com.telran.org.urlshortener.service;

import com.telran.org.urlshortener.dto.UserCreateDTO;
import com.telran.org.urlshortener.dto.UserDTO;
import com.telran.org.urlshortener.entity.User;
import com.telran.org.urlshortener.model.RoleType;

import java.util.List;

/**
 * Service interface for managing application users.
 * Provides operations for creating, retrieving, updating, and deleting users,
 * as well as accessing the currently authenticated user.
 *
 * <p>This interface defines the business-level contract for user management.
 * Implementations must enforce security rules, validation, and uniqueness constraints.</p>
 */
public interface UserService {

    /**
     * Creates a new user with the provided registration data.
     *
     * @param dto the user creation request containing email and password
     * @return the created user as a DTO
     * @throws NullPointerException if dto is null
     * @throws IllegalArgumentException if email or password format is invalid
     * @throws com.telran.org.urlshortener.exception.EmailNotUniqueException
     *         if a user with the same email already exists
     */
    UserDTO create(UserCreateDTO dto);

    /**
     * Retrieves all users in the system.
     * Accessible only to administrators.
     *
     * @return list of all users
     */
    List<UserDTO> findAll();

    /**
     * Retrieves a user by its unique identifier.
     *
     * @param id the user ID
     * @return the user DTO
     * @throws IllegalArgumentException if id is not positive
     * @throws com.telran.org.urlshortener.exception.UserNotFoundException
     *         if no user with the given ID exists
     */
    UserDTO findById(long id);

    /**
     * Updates the role of a user.
     *
     * @param id the user ID
     * @param newRole the new role to assign
     * @return the updated user DTO
     * @throws IllegalArgumentException if id is invalid or newRole is null
     * @throws com.telran.org.urlshortener.exception.UserNotFoundException
     *         if the user does not exist
     */
    UserDTO update(long id, RoleType newRole);

    /**
     * Deletes a user by ID.
     * Regular users may delete only themselves; administrators may delete any user.
     *
     * @param id the user ID
     * @throws IllegalArgumentException if id is invalid
     * @throws com.telran.org.urlshortener.exception.UserNotFoundException
     *         if the user does not exist
     * @throws org.springframework.security.access.AccessDeniedException
     *         if the caller is not allowed to delete this user
     */
    void delete(long id);

    /**
     * Retrieves a user by email.
     *
     * @param email the email address
     * @return the user DTO
     * @throws IllegalArgumentException if email is invalid
     * @throws com.telran.org.urlshortener.exception.UserNotFoundException
     *         if no user with the given email exists
     * @throws org.springframework.security.access.AccessDeniedException
     *         if a regular user attempts to access another user's data
     */
    UserDTO findByEmail(String email);

    /**
     * Returns the currently authenticated user.
     *
     * @return the authenticated User entity
     * @throws SecurityException if no user is authenticated
     * @throws com.telran.org.urlshortener.exception.UserNotFoundException
     *         if the authenticated user does not exist in the database
     */
    User getCurrentUser();
}