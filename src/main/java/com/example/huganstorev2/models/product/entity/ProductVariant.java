package com.example.huganstorev2.models.product.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity 
@Table (name = "product_variant")
public class ProductVariant {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column (nullable = false)
    private String size;
    @Column (nullable = false)
    private String color;
    @Column (nullable = false)
    private BigDecimal price;
    @Column (nullable = false)
    private Integer stock;
    @Column (nullable = false)
    private String sku; // Mã hàng
    @Column (name = "img_url")
    private String imgUrl;

    @ManyToOne 
    @JoinColumn (name="product_id", nullable = false)
    private Product product;

    public ProductVariant(){}
    
    public ProductVariant(String size, String color, 
        BigDecimal price, Integer stock, String sku, String imgUrl
    ){
        this.size=size;
        this.color=color;
        this.price=price;
        this.stock=stock;
        this.sku=sku;
        this.imgUrl=imgUrl;
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public String getSize() { return size; }
    public String getColor() { return color; }
    public BigDecimal getPrice() { return price; }
    public Integer getStock() { return stock; }
    public String getSku() { return sku; }
    public String getImgUrl() { return imgUrl; }

    public void setProduct(Product product) { this.product = product; }
    public void setSize(String size) { this.size = size; }
    public void setColor(String color) { this.color = color; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setStock(Integer stock) { this.stock = stock; }
    public void setSku(String sku) { this.sku = sku; }
    public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }
}
