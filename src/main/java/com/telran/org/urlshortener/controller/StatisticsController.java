package com.telran.org.urlshortener.controller;

import com.telran.org.urlshortener.dto.UrlBindingDTO;
import com.telran.org.urlshortener.service.StatisticsService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/statistics")
@Validated
@Slf4j
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService service;

    @GetMapping("/by-user")
    public ResponseEntity<Long> getRequestsCountByUserId(@RequestParam(required = true) @Positive long userId) {
        log.debug("getRequestsCountByUserId userId={}", userId);
        Long count = service.requestsNumberByUser(userId);
        log.info("Requests count for userId={} is {}", userId, count);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/by-url/{id}")
    public ResponseEntity<Long> getRequestsByUrlBindingId(@PathVariable @Positive long id) {
        log.debug("getRequestsByUrlBindingId id={}", id);
        Long count = service.requestsNumberByUrlBinding(id);
        log.info("Requests count for urlBindingId={} is {}", id, count);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/top")
    public ResponseEntity<List<UrlBindingDTO>> topTenRequests() {
        log.debug("topTenRequests called");
        List<UrlBindingDTO> list = service.topTenRequests();
        log.info("topTenRequests returned {} items", list.size());
        return ResponseEntity.ok(list);
    }
}