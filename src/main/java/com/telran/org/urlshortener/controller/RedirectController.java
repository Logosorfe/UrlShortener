package com.telran.org.urlshortener.controller;

import com.telran.org.urlshortener.dto.UrlBindingDTO;
import com.telran.org.urlshortener.service.UrlBindingService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RedirectController {
    private final UrlBindingService service;

    @GetMapping("/{uId}")
    public ResponseEntity<Void> redirect(@PathVariable
                                             @NotBlank
                                             @Pattern(regexp = "^/?[A-Za-z0-9_\\-]+(/[A-Za-z0-9_\\-]+)?$",
                                                     message = "Invalid uId format")
                                             String uId) {
        log.debug("Redirect request for uId={}", uId);
        String normalized = uId.startsWith("/") ? uId : "/" + uId;
        UrlBindingDTO dto = service.find(normalized);
        log.info("Redirecting uId={} to {}", uId, dto.getOriginalUrl());
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(dto.getOriginalUrl()))
                .build();
    }
}