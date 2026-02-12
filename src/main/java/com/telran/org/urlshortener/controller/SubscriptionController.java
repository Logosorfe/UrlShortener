package com.telran.org.urlshortener.controller;

import com.telran.org.urlshortener.dto.SubscriptionCreateDTO;
import com.telran.org.urlshortener.dto.SubscriptionDTO;
import com.telran.org.urlshortener.service.SubscriptionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscriptions")
@Validated
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService service;

    @PostMapping
    public ResponseEntity<SubscriptionDTO> registerSubscription(@Valid @RequestBody SubscriptionCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionDTO>> getAllSubscriptionsByUserId(@RequestParam @Positive long userId) {
        return ResponseEntity.ok(service.findByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionDTO> getSubscriptionById(@PathVariable @Positive long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeSubscriptionById(@PathVariable @Positive long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> paySubscription(@PathVariable @Positive long id) {
        service.makePayment(id);
        return ResponseEntity.accepted().body("Payment process started");
    }
}