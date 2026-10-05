package com.example.huganstorev2.models.product.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import com.example.huganstorev2.models.category.entity.Category;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity 
@Table (name = "products")
public class Product {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false)
    private String name;
    @Column (unique = true)
    private String slug;
    @Column (columnDefinition = "TEXT")
    private String description;
    @Column (nullable = false)
    private BigDecimal price;
    @Column (name = "img_url")
    private String imgUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_data", columnDefinition = "TEXT")
    private List<String> images = new ArrayList<>();
    @Column (name = "is_published")
    private Boolean isPublished = false;
    
    @ManyToOne 
    @JoinColumn (name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column (name = "updated_at")
    private LocalDateTime updatedAt;

    public Product(){}

    public Product(String name, String slug, String description, BigDecimal price, String imgUrl, Boolean isPublished
        ,LocalDateTime createdAt, LocalDateTime updatedAt, Category category
    ){
        this.name=name;
        this.slug=slug;
        this.description=description;
        this.price=price;
        this.imgUrl=imgUrl;
        this.isPublished=isPublished;
        this.createdAt=LocalDateTime.now();
        this.updatedAt=LocalDateTime.now();
        this.category=category;
    }

    public Long getId(){return id;}
    public String getName(){return name;}
    public String getSlug(){return slug;}
    public String getDescription(){return description;}
    public BigDecimal getPrice(){return price;}
    public String getImgUrl(){return imgUrl;}
    public List<String> getImages(){return images;}
    public Boolean getIsPublished(){return isPublished;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public LocalDateTime getUpdatedAt(){return updatedAt;}
    public Category getCategory(){return category;}
    public List<ProductVariant> getVariants(){return variants;}

    public void setName(String name){this.name=name;}
    public void setSlug(String slug){this.slug=slug;}
    public void setDescription(String description){this.description=description;}
    public void setPrice(BigDecimal price){this.price=price;}
    public void setImgUrl(String imgUrl){this.imgUrl=imgUrl;}
    public void setImages(List<String> images){this.images=images != null ? images : new ArrayList<>();}
    public void setIsPublished(Boolean isPublished){this.isPublished=isPublished;}
    public void setCategory(Category category){this.category=category;}
    public void setVariants(List<ProductVariant> variants) { this.variants = variants; }
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt=createdAt;}
    public void setUpdatedAt(LocalDateTime updatedAt) {this.updatedAt=updatedAt;}
}
