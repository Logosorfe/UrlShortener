package com.telran.org.urlshortener.exception;

public class PathPrefixNotAvailableException extends RuntimeException {
    public PathPrefixNotAvailableException(String message) {
        super(message);
    }
}
