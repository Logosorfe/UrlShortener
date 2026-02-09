package com.telran.org.urlshortener.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;

@Entity
@Table(name = "url_bindings")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class UrlBinding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalUrl;

    @Value("${app.base-url}")
    private String baseUrl;

    private String uId;

    private Long count = 0L;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    @ToString.Exclude
    private User user;

    public UrlBinding(String originalUrl) {
        this.originalUrl = originalUrl;
    }
}
