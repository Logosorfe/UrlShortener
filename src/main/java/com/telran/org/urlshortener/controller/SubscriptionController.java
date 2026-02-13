package com.telran.org.urlshortener.controller;

import com.telran.org.urlshortener.dto.SubscriptionCreateDTO;
import com.telran.org.urlshortener.dto.SubscriptionDTO;
import com.telran.org.urlshortener.service.SubscriptionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscriptions")
@Validated
@Slf4j
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService service;

    @PostMapping
    public ResponseEntity<SubscriptionDTO> createSubscription(@Valid @RequestBody SubscriptionCreateDTO dto) {
        log.debug("createSubscription pathPrefix={}", dto.getPathPrefix());
        SubscriptionDTO created = service.create(dto);
        log.info("Subscription created id={}, pathPrefix={}", created.getId(), created.getPathPrefix());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<SubscriptionDTO>> getSubscriptionsByUserId(@PathVariable @Positive long userId) {
        log.debug("getSubscriptionsByUser userId={}", userId);
        List<SubscriptionDTO> list = service.findAllByUserId(userId);
        log.info("Returned {} subscriptions for userId={}", list.size(), userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionDTO> getSubscriptionById(@PathVariable @Positive long id) {
        log.debug("getSubscriptionById id={}", id);
        SubscriptionDTO dto = service.findById(id);
        log.info("getSubscriptionById succeeded id={}, pathPrefix={}", dto.getId(), dto.getPathPrefix());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeSubscriptionById(@PathVariable @Positive long id) {
        log.debug("removeSubscriptionById id={}", id);
        service.delete(id);
        log.info("Subscription deleted id={}", id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<Void> makePayment(@PathVariable @Positive long id) {
        log.debug("makePayment subscriptionId={}", id);
        service.makePayment(id);
        log.info("Payment process started for subscriptionId={}", id);
        return ResponseEntity.accepted().build();
    }
}