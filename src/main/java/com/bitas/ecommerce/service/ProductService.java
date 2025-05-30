package com.bitas.ecommerce.service;

import com.bitas.ecommerce.model.Product;
import com.bitas.ecommerce.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Product entity.
 * Contains business logic for product operations.
 */
public class ProductService {
    private final ProductRepository productRepository;

    /**
     * Constructor with ProductRepository dependency
     */
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Get product by ID
     *
     * @param id Product ID
     * @return Optional containing Product if found, empty Optional otherwise
     */
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Get products by category
     *
     * @param category Category to search for
     * @return List of products in the specified category
     */
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    /**
     * Get all products
     *
     * @return List of all products
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Create a new product
     *
     * @param product Product to create
     * @return Created product with ID
     */
    public Product createProduct(Product product) {
        // Validate product data
        validateProductData(product);

        // Set default values if needed
        if (product.getCreatedAt() == null) {
            product.setCreatedAt(LocalDateTime.now());
        }

        if (product.getUpdatedAt() == null) {
            product.setUpdatedAt(LocalDateTime.now());
        }

        // Save product
        return productRepository.save(product);
    }

    /**
     * Update an existing product
     *
     * @param id      Product ID
     * @param product Product data to update
     * @return Updated product
     */
    public Product updateProduct(Long id, Product product) {
        // Validate product data
        validateProductData(product);

        // Check if product exists
        Optional<Product> existingProduct = productRepository.findById(id);
        if (!existingProduct.isPresent()) {
            throw new IllegalArgumentException("Product not found with ID: " + id);
        }

        // Preserve creation date
        product.setCreatedAt(existingProduct.get().getCreatedAt());

        // Update modification date
        product.setUpdatedAt(LocalDateTime.now());

        // Set ID and save
        product.setId(id);
        return productRepository.save(product);
    }

    /**
     * Delete a product by ID
     *
     * @param id ID of the product to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteProduct(Long id) {
        return productRepository.deleteById(id);
    }

    /**
     * Update product stock quantity
     *
     * @param id       Product ID
     * @param quantity New stock quantity
     * @return Updated product
     */
    public Product updateStockQuantity(Long id, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        Optional<Product> productOpt = productRepository.findById(id);
        if (!productOpt.isPresent()) {
            throw new IllegalArgumentException("Product not found with ID: " + id);
        }

        Product product = productOpt.get();
        product.setStockQuantity(quantity);
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    /**
     * Update product price
     *
     * @param id    Product ID
     * @param price New price
     * @return Updated product
     */
    public Product updatePrice(Long id, BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }

        Optional<Product> productOpt = productRepository.findById(id);
        if (!productOpt.isPresent()) {
            throw new IllegalArgumentException("Product not found with ID: " + id);
        }

        Product product = productOpt.get();
        product.setPrice(price);
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    /**
     * Validate product data
     *
     * @param product Product to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateProductData(Product product) {
        if (product.getName() == null || product.getName().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }

        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Product price cannot be negative");
        }

        if (product.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        // Add more validation as needed
    }
}