package com.telran.org.urlshortener.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignInRequest {
    @NotBlank(message = "login must not be blank")
    @Size(max = 254)
    private String login;

    @NotBlank(message = "password must not be blank")
    @Size(min = 6, max = 100)
    private String password;
}