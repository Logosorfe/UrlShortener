package com.telran.org.urlshortener.utility;

import org.springframework.stereotype.Component;

@Component
public class MaskingUtil {

    public String maskEmail(String email) {
        if (email == null) return null;
        int at = email.indexOf('@');
        if (at <= 1) return "****";
        return email.substring(0, 1) + "****" + email.substring(at);
    }

    public String maskLogin(String login) {
        if (login == null) return null;
        return login.length() <= 3 ? "***" : login.substring(0, 3) + "***";
    }
}