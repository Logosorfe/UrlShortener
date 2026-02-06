package com.telran.org.urlshortener.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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

    private final String baseUrl = "localhost:8080";

    private String uId;

    private Long count;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonBackReference
    @ToString.Exclude
    private User user;

    public UrlBinding(String originalUrl) {
        this.originalUrl = originalUrl;
    }
}
