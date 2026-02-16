package com.telran.org.urlshortener.controller;

import com.telran.org.urlshortener.dto.UrlBindingDTO;
import com.telran.org.urlshortener.service.StatisticsService;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Statistics",
        description = "Operations for retrieving URL usage statistics",
        externalDocs = @ExternalDocumentation(
                description = "Statistics module documentation",
                url = "https://example.com/docs/statistics"
        ))
@RestController
@RequestMapping("/statistics")
@Validated
@Slf4j
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService service;

    @Operation(summary = "Get total requests by user ID",
            description = "Returns the total number of redirect requests for all URL bindings "
                    + "belonging to the specified user. Regular users may access only their own statistics.",
            operationId = "getRequestsCountByUserId",
            tags = {"Statistics"},
            security = {@SecurityRequirement(name = "Bearer Authentication")},
            externalDocs = @ExternalDocumentation(description = "User statistics rules",
                    url = "https://example.com/docs/statistics#user"))
    @ApiResponses({@ApiResponse(responseCode = "200",
            description = "Statistics returned", content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "400", description = "Invalid user ID"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")})
    @GetMapping("/by-user")
    public ResponseEntity<Long> getRequestsCountByUserId(@RequestParam(required = true) @Positive long userId) {
        log.debug("getRequestsCountByUserId userId={}", userId);
        Long count = service.requestsNumberByUser(userId);
        log.info("Requests count for userId={} is {}", userId, count);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Get requests count by URL binding ID",
            description = "Returns the number of redirect requests for a specific URL binding. "
                    + "Regular users may access only their own bindings.",
            operationId = "getRequestsByUrlBindingId",
            tags = {"Statistics"},
            security = {@SecurityRequirement(name = "Bearer Authentication")},
            externalDocs = @ExternalDocumentation(description = "URL binding statistics rules",
                    url = "https://example.com/docs/statistics#url-binding"))
    @ApiResponses({@ApiResponse(responseCode = "200",
            description = "Statistics returned", content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "404", description = "URL binding not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "400", description = "Invalid ID")})
    @GetMapping("/by-url/{id}")
    public ResponseEntity<Long> getRequestsByUrlBindingId(@PathVariable @Positive long id) {
        log.debug("getRequestsByUrlBindingId id={}", id);
        Long count = service.requestsNumberByUrlBinding(id);
        log.info("Requests count for urlBindingId={} is {}", id, count);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Get top 10 most requested URLs",
            description = "Returns the top 10 most frequently accessed URL bindings in the system. "
                    + "Accessible only to administrators.",
            operationId = "topTenRequests",
            tags = {"Statistics"},
            security = {@SecurityRequirement(name = "Bearer Authentication")},
            externalDocs = @ExternalDocumentation(description = "Top statistics rules",
                    url = "https://example.com/docs/statistics#top"))
    @ApiResponses({@ApiResponse(responseCode = "200",
            description = "Top 10 list returned",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UrlBindingDTO.class)))),
            @ApiResponse(responseCode = "403", description = "Access denied (ADMIN only)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")})
    @GetMapping("/top")
    public ResponseEntity<List<UrlBindingDTO>> topTenRequests() {
        log.debug("topTenRequests called");
        List<UrlBindingDTO> list = service.topTenRequests();
        log.info("topTenRequests returned {} items", list.size());
        return ResponseEntity.ok(list);
    }
}