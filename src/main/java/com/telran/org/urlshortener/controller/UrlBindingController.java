package com.telran.org.urlshortener.controller;

import com.telran.org.urlshortener.dto.UrlBindingCreateDTO;
import com.telran.org.urlshortener.dto.UrlBindingDTO;
import com.telran.org.urlshortener.service.UrlBindingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/url_bindings")
@Validated
@RequiredArgsConstructor
public class UrlBindingController {
    private final UrlBindingService service;

    @PostMapping
    public ResponseEntity<UrlBindingDTO> createUrlBinding(@Valid @RequestBody UrlBindingCreateDTO dto,
                                                          @RequestParam(required = false) String pathPrefix) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto, pathPrefix));
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<UrlBindingDTO>> getAllUrlBindingsByUserId(@PathVariable @Positive long userId) {
        return ResponseEntity.ok(service.findAllByUserId(userId));
    }

    @GetMapping
    public ResponseEntity<UrlBindingDTO> getUrlBindingById(@RequestParam @NotBlank String uId) {
        return ResponseEntity.ok(service.find(uId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UrlBindingDTO> resetUrlBindingCount(@PathVariable @Positive long id) {
        return ResponseEntity.ok(service.reset(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeUrlBindingById(@PathVariable @Positive long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
