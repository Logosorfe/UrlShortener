package com.telran.org.urlshortener.controller;

import com.telran.org.urlshortener.dto.UrlBindingCreateDTO;
import com.telran.org.urlshortener.dto.UrlBindingDTO;
import com.telran.org.urlshortener.service.UrlBindingService;
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
@RequestMapping("/url_bindings")
@Validated
@Slf4j
@RequiredArgsConstructor
public class UrlBindingController {

    private final UrlBindingService service;

    @PostMapping
    public ResponseEntity<UrlBindingDTO> createUrlBinding(@Valid @RequestBody UrlBindingCreateDTO dto,
                                                          @RequestParam(required = false) String pathPrefix) {
        log.debug("createUrlBinding prefix={}, originalUrlLength={}",
                pathPrefix, dto.getOriginalUrl() != null ? dto.getOriginalUrl().length() : 0);
        UrlBindingDTO result = service.create(dto, pathPrefix);
        log.info("UrlBinding created id={}, shortUrl={}", result.getId(), result.getShortUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<UrlBindingDTO>> getAllUrlBindingsByUserId(@PathVariable long userId) {
        log.debug("getAllUrlBindingsByUserId userId={}", userId);
        List<UrlBindingDTO> list = service.findAllByUserId(userId);
        log.info("Returned {} url bindings for userId={}", list.size(), userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping
    public ResponseEntity<UrlBindingDTO> getUrlBindingByUid(@RequestParam String uId) {
        log.debug("getUrlBindingById uId={}", uId);
        UrlBindingDTO dto = service.find(uId);
        log.info("Found UrlBinding id={}, uId={}", dto.getId(), uId);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{id}/reset")
    public ResponseEntity<UrlBindingDTO> resetUrlBindingCount(@PathVariable @Positive long id) {
        log.debug("resetUrlBindingCount id={}", id);
        UrlBindingDTO dto = service.reset(id);
        log.info("resetUrlBindingCount succeeded id={}, newCount={}", dto.getId(), dto.getCount());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeUrlBindingById(@PathVariable long id) {
        log.debug("removeUrlBindingById id={}", id);
        service.delete(id);
        log.info("UrlBinding deleted id={}", id);
        return ResponseEntity.noContent().build();
    }
}
