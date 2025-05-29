package com.bitas.ecommerce.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a product in the e-commerce system.
 * Contains product information such as ID, name, description, price, stock, etc.
 */
public class Product {
    /**
     * Unique identifier for the product.
     */
    private Long id;
    /**
     * Name of the product.
     */
    private String name;
    /**
     * Description of the product.
     */
    private String description;
    /**
     * Price of the product.
     */
    private BigDecimal price;
    /**
     * Quantity of the product in stock.
     */
    private int stockQuantity;
    /**
     * Category of the product.
     */
    private String category;
    /**
     * URL of the product image.
     */
    private String imageUrl;
    /**
     * Indicates if the product is active.
     */
    private boolean active;
    /**
     * Timestamp when the product was created.
     */
    private LocalDateTime createdAt;
    /**
     * Timestamp when the product was last updated.
     */
    private LocalDateTime updatedAt;

    /**
     * Default constructor
     */
    public Product() {
        // Default constructor
    }

    /**
     * Parameterized constructor
     */
    public Product(Long id, String name, String description, BigDecimal price, int stockQuantity,
                   String category, String imageUrl, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
        this.imageUrl = imageUrl;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                ", category='" + category + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}