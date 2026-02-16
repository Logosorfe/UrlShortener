package com.telran.org.urlshortener.controller;

import com.telran.org.urlshortener.dto.SubscriptionDTO;
import com.telran.org.urlshortener.dto.UrlBindingCreateDTO;
import com.telran.org.urlshortener.dto.UrlBindingDTO;
import com.telran.org.urlshortener.service.UrlBindingService;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "URL Bindings",
        description = "Operations for creating, retrieving, resetting and deleting shortened URLs",
        externalDocs = @ExternalDocumentation(
                description = "URL binding module documentation",
                url = "https://example.com/docs/url-bindings"
        )
)
@RestController
@RequestMapping("/url_bindings")
@Validated
@Slf4j
@RequiredArgsConstructor
public class UrlBindingController {
    private final UrlBindingService service;

    @Operation(summary = "Create URL binding",
            description = "Creates a shortened URL for the authenticated user. "
                    + "If a prefix is provided, the user must have an active subscription.",
            operationId = "createUrlBinding",
            tags = {"URL Bindings"},
            security = {@SecurityRequirement(name = "Bearer Authentication")},
            externalDocs = @ExternalDocumentation(description = "URL creation rules",
                    url = "https://example.com/docs/url-bindings#create"))
    @ApiResponses({@ApiResponse(responseCode = "201",
            description = "URL binding successfully created",
            content = @Content(schema = @Schema(implementation = UrlBindingDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid URL or prefix format"),
            @ApiResponse(responseCode = "403", description = "Prefix not allowed for this user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")})
    @PostMapping
    public ResponseEntity<UrlBindingDTO> createUrlBinding(@Valid @RequestBody UrlBindingCreateDTO dto,
                                                          @RequestParam(required = false)
                                                          @Pattern(regexp = "^[A-Za-z0-9_-]{3,50}$",
                                                                  message = "Invalid pathPrefix")
                                                          String pathPrefix) {
        log.debug("createUrlBinding prefix={}, originalUrlLength={}",
                pathPrefix, dto.getOriginalUrl() != null ? dto.getOriginalUrl().length() : 0);
        UrlBindingDTO result = service.create(dto, pathPrefix);
        log.info("UrlBinding created id={}, shortUrl={}", result.getId(), result.getShortUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(summary = "Get URL bindings by user ID",
            description = "Returns all URL bindings belonging to the specified user. "
                    + "Regular users may access only their own bindings.",
            operationId = "getAllUrlBindingsByUserId",
            tags = {"URL Bindings"},
            security = {@SecurityRequirement(name = "Bearer Authentication")})
    @ApiResponses({@ApiResponse(responseCode = "200",
            description = "URL bindings returned",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UrlBindingDTO.class)))),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID")})
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<UrlBindingDTO>> getAllUrlBindingsByUserId(@PathVariable @Positive long userId) {
        log.debug("getAllUrlBindingsByUserId userId={}", userId);
        List<UrlBindingDTO> list = service.findAllByUserId(userId);
        log.info("Returned {} url bindings for userId={}", list.size(), userId);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Get URL binding by UID",
            description = "Returns a URL binding by its unique identifier. "
                    + "Regular users may access only their own bindings.",
            operationId = "getUrlBindingByUid",
            tags = {"URL Bindings"},
            security = {@SecurityRequirement(name = "Bearer Authentication")},
            externalDocs = @ExternalDocumentation(description = "UID format rules",
                    url = "https://example.com/docs/url-bindings#uid"))
    @ApiResponses({@ApiResponse(responseCode = "200",
            description = "URL binding found",
            content = @Content(schema = @Schema(implementation = UrlBindingDTO.class))),
            @ApiResponse(responseCode = "404", description = "URL binding not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "400", description = "Invalid UID format")})
    @GetMapping
    public ResponseEntity<UrlBindingDTO> getUrlBindingByUid(@RequestParam @NotBlank @Pattern
            (regexp = "^/?[A-Za-z0-9_\\-]+(/[A-Za-z0-9_\\-]+)?$",
                    message = "Invalid uId format") String uId) {
        log.debug("getUrlBindingById uId={}", uId);
        UrlBindingDTO dto = service.find(uId);
        log.info("Found UrlBinding id={}, uId={}", dto.getId(), uId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Reset URL binding counter",
            description = "Resets the redirect counter for a URL binding. "
                    + "Only the owner may reset it.",
            operationId = "resetUrlBindingCount",
            tags = {"URL Bindings"},
            security = {@SecurityRequirement(name = "Bearer Authentication")})
    @ApiResponses({@ApiResponse(responseCode = "200",
            description = "Counter reset successfully",
            content = @Content(schema = @Schema(implementation = UrlBindingDTO.class))),
            @ApiResponse(responseCode = "404", description = "URL binding not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "400", description = "Invalid ID")})
    @PatchMapping("/{id}/reset")
    public ResponseEntity<UrlBindingDTO> resetUrlBindingCount(@PathVariable @Positive long id) {
        log.debug("resetUrlBindingCount id={}", id);
        UrlBindingDTO dto = service.reset(id);
        log.info("resetUrlBindingCount succeeded id={}, newCount={}", dto.getId(), dto.getCount());
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Delete URL binding",
            description = "Deletes a URL binding. Only the owner may delete it.",
            operationId = "deleteUrlBinding",
            tags = {"URL Bindings"},
            security = {@SecurityRequirement(name = "Bearer Authentication")})
    @ApiResponses({@ApiResponse(responseCode = "204", description = "URL binding deleted"),
            @ApiResponse(responseCode = "404", description = "URL binding not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "400", description = "Invalid ID")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeUrlBindingById(@PathVariable @Positive long id) {
        log.debug("removeUrlBindingById id={}", id);
        service.delete(id);
        log.info("UrlBinding deleted id={}", id);
        return ResponseEntity.noContent().build();
    }
}