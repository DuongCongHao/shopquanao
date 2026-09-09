package com.example.huganstorev2.models.category.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table (name = "categories")
public class Category {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false)
    private String name;
    private String slug;
    private LocalDateTime createdAt;

    public Category() {}
    public Category(String name, String slug, LocalDateTime createdAt){
        this.name = name;
        this.slug = slug;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getName(){ return name; }
    public String getSlug() { return slug; }
    public  LocalDateTime createdAt() { return createdAt; }

    public void setName(String name) { this.name = name; }
    public  void setSlug(String slug) { this.slug = slug; }
    public  void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
