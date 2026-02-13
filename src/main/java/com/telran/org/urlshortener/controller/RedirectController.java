package com.telran.org.urlshortener.controller;

import com.telran.org.urlshortener.dto.UrlBindingDTO;
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
import java.util.List;

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
        List<String> protectedPrefixes = List.of(
                "/url_bindings", "/users", "/subscriptions", "/statistics"
        );
        for (String p : protectedPrefixes) {
            if (path.equals(p) || path.startsWith(p + "/")) {
                log.debug("Path {} matches protected prefix {}, skipping redirect", path, p);
                return ResponseEntity.notFound().build();
            }
        }
        String normalized = path.startsWith("/") ? path : "/" + path;
        if (!isValidUId(normalized)) {
            log.warn("Invalid uId format: {}", normalized);
            return ResponseEntity.badRequest().build();
        }
        try {
            UrlBindingDTO dto = service.find(normalized);
            String target = dto.getOriginalUrl();
            if (!isValidRedirectUrl(target)) {
                log.warn("Blocked redirect to unsafe URL: {}", target);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            log.info("Redirecting {} -> {}", normalized, target);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(target))
                    .build();
        } catch (IdNotFoundException ex) {
            log.warn("UrlBinding not found for uId={}", normalized);
            return ResponseEntity.notFound().build();

        } catch (AccessDeniedException ex) {
            log.warn("Access denied for uId={}", normalized);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        } catch (Exception ex) {
            log.error("Unexpected error while redirecting uId={}", normalized, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private boolean isValidUId(String uId) {
        return uId.matches("^/[A-Za-z0-9_\\-]+(/[A-Za-z0-9_\\-]+)?$");
    }

    private boolean isValidRedirectUrl(String url) {
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            if (scheme == null || !scheme.equalsIgnoreCase("http")
                    && !scheme.equalsIgnoreCase("https")) {
                return false;
            }
            String host = uri.getHost();
            List<String> hosts = List.of(
                    "localhost", "127.0.0.1"
            );
            if (host == null || hosts.contains(host)) return false;
            List<String> internal = List.of(
                    "10.", "192.168.", "172.16."
            );
            return internal.stream().noneMatch(host::startsWith);
        } catch (Exception e) {
            return false;
        }
    }
}