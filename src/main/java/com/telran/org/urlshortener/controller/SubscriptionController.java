package com.telran.org.urlshortener.controller;

import com.telran.org.urlshortener.dto.SubscriptionCreateDTO;
import com.telran.org.urlshortener.dto.SubscriptionDTO;
import com.telran.org.urlshortener.service.SubscriptionService;
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
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Subscriptions",
        description = "Operations for managing user subscriptions and path prefixes",
        externalDocs = @ExternalDocumentation(
                description = "Subscription module documentation",
                url = "https://example.com/docs/subscriptions"))
@RestController
@RequestMapping("/subscriptions")
@Validated
@Slf4j
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService service;

    @Operation(summary = "Create subscription",
            description = "Creates a new subscription for the authenticated user. "
                    + "A subscription grants rights to use a specific path prefix.",
            operationId = "createSubscription", tags = {"Subscriptions"},
            security = {@SecurityRequirement(name = "Bearer Authentication")},
            externalDocs = @ExternalDocumentation(description = "Subscription creation rules",
                    url = "https://example.com/docs/subscriptions#create"))
    @ApiResponses({@ApiResponse(responseCode = "201",
            description = "Subscription successfully created",
            content = @Content(schema = @Schema(implementation = SubscriptionDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid prefix or request body"),
            @ApiResponse(responseCode = "409", description = "Prefix already in use"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")})
    @PostMapping
    public ResponseEntity<SubscriptionDTO> createSubscription(@Valid @RequestBody SubscriptionCreateDTO dto) {
        log.debug("createSubscription pathPrefix={}", dto.getPathPrefix());
        SubscriptionDTO created = service.create(dto);
        log.info("Subscription created id={}, pathPrefix={}", created.getId(), created.getPathPrefix());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get subscriptions by user ID",
            description = "Returns all subscriptions belonging to the specified user. "
                    + "Regular users may access only their own subscriptions.",
            operationId = "getSubscriptionsByUserId",
            tags = {"Subscriptions"},
            security = {@SecurityRequirement(name = "Bearer Authentication")})
    @ApiResponses({@ApiResponse(responseCode = "200",
            description = "Subscriptions returned",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = SubscriptionDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid user ID"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")})
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<SubscriptionDTO>> getSubscriptionsByUserId(@PathVariable @Positive long userId) {
        log.debug("getSubscriptionsByUser userId={}", userId);
        List<SubscriptionDTO> list = service.findAllByUserId(userId);
        log.info("Returned {} subscriptions for userId={}", list.size(), userId);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Get subscription by ID",
            description = "Returns subscription details by ID. "
                    + "Regular users may access only their own subscriptions.",
            operationId = "getSubscriptionById",
            tags = {"Subscriptions"},
            security = {@SecurityRequirement(name = "Bearer Authentication")})
    @ApiResponses({@ApiResponse(responseCode = "200",
            description = "Subscription found",
            content = @Content(schema = @Schema(implementation = SubscriptionDTO.class))),
            @ApiResponse(responseCode = "404", description = "Subscription not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "400", description = "Invalid ID")})
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionDTO> getSubscriptionById(@PathVariable @Positive long id) {
        log.debug("getSubscriptionById id={}", id);
        SubscriptionDTO dto = service.findById(id);
        log.info("getSubscriptionById succeeded id={}, pathPrefix={}", dto.getId(), dto.getPathPrefix());
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Delete subscription",
            description = "Deletes a subscription. Only the owner may delete it.",
            operationId = "deleteSubscription",
            tags = {"Subscriptions"},
            security = {@SecurityRequirement(name = "Bearer Authentication")})
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Subscription deleted"),
            @ApiResponse(responseCode = "404", description = "Subscription not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "400", description = "Invalid ID")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeSubscriptionById(@PathVariable @Positive long id) {
        log.debug("removeSubscriptionById id={}", id);
        service.delete(id);
        log.info("Subscription deleted id={}", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Process subscription payment",
            description = "Starts payment process for a subscription. " + "Only ADMIN may perform payment.",
            operationId = "makePayment",
            tags = {"Subscriptions"},
            security = {@SecurityRequirement(name = "Bearer Authentication")},
            externalDocs = @ExternalDocumentation(description = "Payment processing rules",
                    url = "https://example.com/docs/subscriptions#payment"))
    @ApiResponses({@ApiResponse(responseCode = "202", description = "Payment accepted"),
            @ApiResponse(responseCode = "404", description = "Subscription not found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "400", description = "Invalid ID")})
    @PostMapping("/{id}/pay")
    public ResponseEntity<Void> makePayment(@PathVariable @Positive long id) {
        log.debug("makePayment subscriptionId={}", id);
        service.makePayment(id);
        log.info("Payment process started for subscriptionId={}", id);
        return ResponseEntity.accepted().build();
    }
}