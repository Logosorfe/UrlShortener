package com.telran.org.urlshortener.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDTO {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;
}
