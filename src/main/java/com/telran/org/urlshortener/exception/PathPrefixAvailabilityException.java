package com.telran.org.urlshortener.exception;

public class PathPrefixAvailabilityException extends RuntimeException {
    public PathPrefixAvailabilityException(String message) {
        super(message);
    }
}