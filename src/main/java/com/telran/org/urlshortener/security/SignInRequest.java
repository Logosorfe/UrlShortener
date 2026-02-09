package com.telran.org.urlshortener.security;

import lombok.Data;

@Data
public class SignInRequest {
    private String login;

    private String password;
}
