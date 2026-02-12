package com.telran.org.urlshortener.configuration;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Bean
    public SecretKey jwtSigningKey(@Value("${app.jwt.secret}") String jwtTokenSigningKey) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtTokenSigningKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}