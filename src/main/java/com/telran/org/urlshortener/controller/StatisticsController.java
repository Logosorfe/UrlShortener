package com.telran.org.urlshortener.controller;

import com.telran.org.urlshortener.dto.UrlBindingDTO;
import com.telran.org.urlshortener.service.StatisticsService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/statistics")
@Validated
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService service;

    @GetMapping("/by-user")
    public ResponseEntity<Long> getRequestsCountByUserId(@RequestParam @Positive long userId) {
        return ResponseEntity.ok(service.requestsNumberByUser(userId));
    }

    @GetMapping("/by-url/{id}")
    public ResponseEntity<Long> getRequestsByUrlBindingId(@PathVariable @Positive long id) {
        return ResponseEntity.ok(service.requestsNumberByUrlBinding(id));
    }

    @GetMapping("/top")
    public ResponseEntity<List<UrlBindingDTO>> topTenRequests() {
        return ResponseEntity.ok(service.topTenRequests());
    }
}