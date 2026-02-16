package com.telran.org.urlshortener.utility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UrlValidationServiceTest {
    private UrlValidationService service;

    @BeforeEach
    void setUp() {
        service = new UrlValidationService();
    }

    // -----------------------------
    // normalizeUrl()
    // -----------------------------
    @Test
    void normalizeUrl_shouldTrimWhitespace() {
        String result = service.normalizeUrl("   https://example.com/page   ");
        assertThat(result).isEqualTo("https://example.com/page");
    }

    @Test
    void normalizeUrl_shouldThrowIfNull() {
        assertThatThrownBy(() -> service.normalizeUrl(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("originalUrl must not be null");
    }

    // -----------------------------
    // validateUrl()
    // -----------------------------
    @Test
    void validateUrl_shouldAcceptHttp() {
        service.validateUrl("http://example.com");
    }

    @Test
    void validateUrl_shouldAcceptHttps() {
        service.validateUrl("https://example.com");
    }

    @Test
    void validateUrl_shouldRejectMissingScheme() {
        assertThatThrownBy(() -> service.validateUrl("example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid URL format");
    }

    @Test
    void validateUrl_shouldRejectUnsupportedScheme() {
        assertThatThrownBy(() -> service.validateUrl("ftp://example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid URL format");
    }

    // -----------------------------
    // normalizeUId()
    // -----------------------------
    @Test
    void normalizeUId_shouldAddLeadingSlash() {
        String result = service.normalizeUId("abc");
        assertThat(result).isEqualTo("/abc");
    }

    @Test
    void normalizeUId_shouldKeepLeadingSlash() {
        String result = service.normalizeUId("/abc");
        assertThat(result).isEqualTo("/abc");
    }

    @Test
    void normalizeUId_shouldRejectBlank() {
        assertThatThrownBy(() -> service.normalizeUId("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("uId must not be blank");
    }

    @Test
    void normalizeUId_shouldRejectNull() {
        assertThatThrownBy(() -> service.normalizeUId(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("uId must not be null");
    }

    // -----------------------------
    // isValidUId()
    // -----------------------------
    @Test
    void isValidUId_shouldAcceptSimpleUId() {
        assertThat(service.isValidUId("/abc123")).isTrue();
    }

    @Test
    void isValidUId_shouldAcceptTwoLevelUId() {
        assertThat(service.isValidUId("/abc/xyz")).isTrue();
    }

    @Test
    void isValidUId_shouldRejectInvalidCharacters() {
        assertThat(service.isValidUId("/ab$c")).isFalse();
    }

    @Test
    void isValidUId_shouldRejectNull() {
        assertThat(service.isValidUId(null)).isFalse();
    }

    // -----------------------------
    // isValidRedirectUrl()
    // -----------------------------
    @Test
    void isValidRedirectUrl_shouldAcceptValidExternalUrl() {
        assertThat(service.isValidRedirectUrl("https://google.com")).isTrue();
    }

    @Test
    void isValidRedirectUrl_shouldRejectLocalhost() {
        assertThat(service.isValidRedirectUrl("http://localhost/test")).isFalse();
    }

    @Test
    void isValidRedirectUrl_shouldReject127_0_0_1() {
        assertThat(service.isValidRedirectUrl("http://127.0.0.1/test")).isFalse();
    }

    @Test
    void isValidRedirectUrl_shouldRejectInternalNetwork() {
        assertThat(service.isValidRedirectUrl("http://192.168.1.10/page")).isFalse();
    }

    @Test
    void isValidRedirectUrl_shouldRejectMissingScheme() {
        assertThat(service.isValidRedirectUrl("example.com")).isFalse();
    }

    @Test
    void isValidRedirectUrl_shouldRejectInvalidUrl() {
        assertThat(service.isValidRedirectUrl("http://exa mple.com")).isFalse();
    }
}