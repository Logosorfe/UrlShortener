package com.telran.org.urlshortener.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.telran.org.urlshortener.model.StatusState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pathPrefix;

    private LocalDate creationDate;

    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    private StatusState status = StatusState.UNPAID;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    @ToString.Exclude
    private User user;

    public Subscription(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }
}