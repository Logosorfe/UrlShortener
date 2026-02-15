package com.telran.org.urlshortener.controller;

import com.telran.org.urlshortener.exception.IdNotFoundException;
import com.telran.org.urlshortener.service.UrlBindingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RedirectController {
    private final UrlBindingService service;

    @GetMapping("/**")
    public ResponseEntity<Void> redirect(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = (contextPath != null && !contextPath.isEmpty())
                ? requestUri.substring(contextPath.length())
                : requestUri;
        log.debug("Redirect request for raw path={}", path);
        try {
            URI target = service.resolveRedirectUri(path);
            return ResponseEntity.status(HttpStatus.FOUND).location(target).build();
        } catch (IdNotFoundException ex) {
            log.warn("UrlBinding not found for path={}", path);
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException ex) {
            log.warn("Access denied for path={}", path);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException ex) {
            log.warn("Bad request for path={} reason={}", path, ex.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception ex) {
            log.error("Unexpected error while redirecting path={}", path, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}