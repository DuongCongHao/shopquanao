package com.example.huganstorev2.models.user.entity;

import java.time.LocalDateTime;

import com.example.huganstorev2.models.cart.entity.Cart;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter 
@Setter 
@Table (name = "users") 
public class User {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    @Column (nullable = false, unique = true)
    private String email;
    private String phone;
    @Column (nullable = false)
    private String fullName;
    private LocalDateTime createdAt;
    @JsonIgnore 
    private String password;
    @Enumerated (EnumType.STRING)
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore 
    private Cart cart;

    public User() {}
    public User(String email, String phone, String password, String fullName){
        this.email = email;
        this.phone = phone;
        this.fullName = fullName;
        this.password = password;
        this.createdAt = LocalDateTime.now();
        this.role = Role.USER;
    }

    // public Long getId() {return id;}
    // public String getEmail() {return email;}
    // public String getPhone() {return phone;}
    // public String getPassword() {return password;}
    // public String getFullName() {return fullName;}
    // public Role getRole() {return role;}
    // public LocalDateTime getCreatedAt() {return createdAt;}

    // public void setEmail(String email) { this.email = email; }
    // public void setPassword(String password) { this.password = password; }
    // public void setFullName(String fullName) { this.fullName = fullName; }
    // public void setPhone(String phone) { this.phone = phone; }
    // public void setRole(Role role) { this.role = role; }
    // public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}