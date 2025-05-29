package com.bitas.ecommerce.controller;

import com.bitas.ecommerce.model.Product;
import com.bitas.ecommerce.service.ProductService;
import com.bitas.ecommerce.utils.JsonUtil;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller class for handling product-related HTTP requests.
 * Maps HTTP requests to service methods and formats responses.
 */
public class ProductController {
    private final ProductService productService;
    private final JsonUtil jsonUtil;
    
    /**
     * Constructor with ProductService dependency
     */
    public ProductController(ProductService productService, JsonUtil jsonUtil) {
        this.productService = productService;
        this.jsonUtil = jsonUtil;
    }
    
    /**
     * Handle GET request for a product by ID
     * 
     * @param id Product ID
     * @return JSON response with product data or error message
     */
    public String getProduct(String id) {
        try {
            Long productId = Long.parseLong(id);
            Optional<Product> productOpt = productService.getProductById(productId);
            
            if (productOpt.isPresent()) {
                return jsonUtil.toJson(productOpt.get());
            } else {
                return createErrorResponse(404, "Product not found with ID: " + id);
            }
        } catch (NumberFormatException e) {
            return createErrorResponse(400, "Invalid product ID format");
        } catch (Exception e) {
            return createErrorResponse(500, "Error retrieving product: " + e.getMessage());
        }
    }
    
    /**
     * Handle GET request for products by category
     * 
     * @param category Category to search for
     * @return JSON response with list of products
     */
    public String getProductsByCategory(String category) {
        try {
            List<Product> products = productService.getProductsByCategory(category);
            return jsonUtil.toJson(products);
        } catch (Exception e) {
            return createErrorResponse(500, "Error retrieving products by category: " + e.getMessage());
        }
    }
    
    /**
     * Handle GET request for all products
     * 
     * @return JSON response with list of products
     */
    public String getAllProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            return jsonUtil.toJson(products);
        } catch (Exception e) {
            return createErrorResponse(500, "Error retrieving products: " + e.getMessage());
        }
    }
    
    /**
     * Handle POST request to create a new product
     * 
     * @param requestBody JSON request body with product data
     * @return JSON response with created product or error message
     */
    public String createProduct(String requestBody) {
        try {
            Product product = jsonUtil.fromJson(requestBody, Product.class);
            Product createdProduct = productService.createProduct(product);
            return jsonUtil.toJson(createdProduct);
        } catch (IllegalArgumentException e) {
            return createErrorResponse(400, e.getMessage());
        } catch (Exception e) {
            return createErrorResponse(500, "Error creating product: " + e.getMessage());
        }
    }
    
    /**
     * Handle PUT request to update an existing product
     * 
     * @param id Product ID
     * @param requestBody JSON request body with updated product data
     * @return JSON response with updated product or error message
     */
    public String updateProduct(String id, String requestBody) {
        try {
            Long productId = Long.parseLong(id);
            Product product = jsonUtil.fromJson(requestBody, Product.class);
            Product updatedProduct = productService.updateProduct(productId, product);
            return jsonUtil.toJson(updatedProduct);
        } catch (NumberFormatException e) {
            return createErrorResponse(400, "Invalid product ID format");
        } catch (IllegalArgumentException e) {
            return createErrorResponse(400, e.getMessage());
        } catch (Exception e) {
            return createErrorResponse(500, "Error updating product: " + e.getMessage());
        }
    }
    
    /**
     * Handle DELETE request to delete a product
     * 
     * @param id Product ID
     * @return JSON response with success or error message
     */
    public String deleteProduct(String id) {
        try {
            Long productId = Long.parseLong(id);
            boolean deleted = productService.deleteProduct(productId);
            
            if (deleted) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Product deleted successfully");
                return jsonUtil.toJson(response);
            } else {
                return createErrorResponse(404, "Product not found with ID: " + id);
            }
        } catch (NumberFormatException e) {
            return createErrorResponse(400, "Invalid product ID format");
        } catch (Exception e) {
            return createErrorResponse(500, "Error deleting product: " + e.getMessage());
        }
    }
    
    /**
     * Handle PATCH request to update product stock quantity
     * 
     * @param id Product ID
     * @param requestBody JSON request body with stock quantity data
     * @return JSON response with updated product or error message
     */
    public String updateStockQuantity(String id, String requestBody) {
        try {
            Long productId = Long.parseLong(id);
            Map<String, Object> data = jsonUtil.fromJson(requestBody, Map.class);
            
            if (!data.containsKey("stockQuantity")) {
                return createErrorResponse(400, "Stock quantity is required");
            }
            
            int quantity = ((Number) data.get("stockQuantity")).intValue();
            Product updatedProduct = productService.updateStockQuantity(productId, quantity);
            return jsonUtil.toJson(updatedProduct);
        } catch (NumberFormatException e) {
            return createErrorResponse(400, "Invalid product ID or stock quantity format");
        } catch (IllegalArgumentException e) {
            return createErrorResponse(400, e.getMessage());
        } catch (Exception e) {
            return createErrorResponse(500, "Error updating stock quantity: " + e.getMessage());
        }
    }
    
    /**
     * Handle PATCH request to update product price
     * 
     * @param id Product ID
     * @param requestBody JSON request body with price data
     * @return JSON response with updated product or error message
     */
    public String updatePrice(String id, String requestBody) {
        try {
            Long productId = Long.parseLong(id);
            Map<String, Object> data = jsonUtil.fromJson(requestBody, Map.class);
            
            if (!data.containsKey("price")) {
                return createErrorResponse(400, "Price is required");
            }
            
            BigDecimal price;
            Object priceObj = data.get("price");
            if (priceObj instanceof Number) {
                price = new BigDecimal(priceObj.toString());
            } else {
                return createErrorResponse(400, "Invalid price format");
            }
            
            Product updatedProduct = productService.updatePrice(productId, price);
            return jsonUtil.toJson(updatedProduct);
        } catch (NumberFormatException e) {
            return createErrorResponse(400, "Invalid product ID or price format");
        } catch (IllegalArgumentException e) {
            return createErrorResponse(400, e.getMessage());
        } catch (Exception e) {
            return createErrorResponse(500, "Error updating price: " + e.getMessage());
        }
    }
    
    /**
     * Create a JSON error response
     * 
     * @param statusCode HTTP status code
     * @param message Error message
     * @return JSON string with error details
     */
    private String createErrorResponse(int statusCode, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", statusCode);
        error.put("error", message);
        return jsonUtil.toJson(error);
    }
}