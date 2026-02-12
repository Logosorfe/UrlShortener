package com.telran.org.urlshortener.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.telran.org.urlshortener.model.RoleType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @ToString.Exclude
    private String password;

    @Enumerated(EnumType.STRING)
    private RoleType role = RoleType.ROLE_USER;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Subscription> subscriptions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<UrlBinding> urlBindings = new ArrayList<>();

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}