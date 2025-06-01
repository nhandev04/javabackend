package com.bitas.ecommerce.repository;

import com.bitas.ecommerce.model.Product;
import com.bitas.ecommerce.utils.database.ConnectionPool;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository class for Product entity.
 * Handles database operations related to products.
 */
public class ProductRepository {

    /**
     * Find product by ID
     *
     * @param id Product ID
     * @return Optional containing Product if found, empty Optional otherwise
     */
    public Optional<Product> findById(Long id) {
        // SQL query to find product by ID
        String sql = "SELECT * FROM products WHERE id = ?";
        Connection connection =  ConnectionPool.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Product product = mapResultSetToProduct(rs);
                return Optional.of(product);
            }

        } catch (SQLException e) {
            System.err.println("Error finding product by ID: " + e.getMessage());
        } finally {
            ConnectionPool.releaseConnection(connection);
        }

        return Optional.empty();
    }

    /**
     * Find products by category
     *
     * @param category Category to search for
     * @return List of products in the specified category
     */
    public List<Product> findByCategory(String category) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE category = ?";
        Connection connection =  ConnectionPool.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Product product = mapResultSetToProduct(rs);
                products.add(product);
            }

        } catch (SQLException e) {
            System.err.println("Error finding products by category: " + e.getMessage());
        } finally {
            ConnectionPool.releaseConnection(connection);
        }

        return products;
    }

    /**
     * Find all products
     *
     * @return List of all products
     */
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        Connection connection =  ConnectionPool.getConnection();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Product product = mapResultSetToProduct(rs);
                products.add(product);
            }

        } catch (SQLException e) {
            System.err.println("Error finding all products: " + e.getMessage());
        } finally {
            ConnectionPool.releaseConnection(connection);
        }

        return products;
    }

    /**
     * Save a new product or update an existing one
     *
     * @param product Product to save
     * @return Saved product with ID
     */
    public Product save(Product product) {
        if (product.getId() == null) {
            return insert(product);
        } else {
            return update(product);
        }
    }

    /**
     * Insert a new product
     *
     * @param product Product to insert
     * @return Inserted product with ID
     */
    private Product insert(Product product) {
        String sql = "INSERT INTO products (name, description, price, stock_quantity, category, " +
                "image_url, active, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection connection =  ConnectionPool.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime now = LocalDateTime.now();

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setInt(4, product.getStockQuantity());
            stmt.setString(5, product.getCategory());
            stmt.setString(6, product.getImageUrl());
            stmt.setBoolean(7, product.isActive());
            stmt.setTimestamp(8, Timestamp.valueOf(now));
            stmt.setTimestamp(9, Timestamp.valueOf(now));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        product.setId(generatedKeys.getLong(1));
                    }
                }
            }

            product.setCreatedAt(now);
            product.setUpdatedAt(now);

        } catch (SQLException e) {
            System.err.println("Error inserting product: " + e.getMessage());
        } finally {
            ConnectionPool.releaseConnection(connection);
        }

        return product;
    }

    /**
     * Update an existing product
     *
     * @param product Product to update
     * @return Updated product
     */
    private Product update(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, " +
                "stock_quantity = ?, category = ?, image_url = ?, active = ?, " +
                "updated_at = ? WHERE id = ?";
        Connection connection =  ConnectionPool.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            LocalDateTime now = LocalDateTime.now();

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setInt(4, product.getStockQuantity());
            stmt.setString(5, product.getCategory());
            stmt.setString(6, product.getImageUrl());
            stmt.setBoolean(7, product.isActive());
            stmt.setTimestamp(8, Timestamp.valueOf(now));
            stmt.setLong(9, product.getId());

            stmt.executeUpdate();

            product.setUpdatedAt(now);

        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
        } finally {
            ConnectionPool.releaseConnection(connection);
        }

        return product;
    }

    /**
     * Delete a product by ID
     *
     * @param id ID of the product to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";
        Connection connection =  ConnectionPool.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();

            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting product: " + e.getMessage());
            return false;
        } finally {
            ConnectionPool.releaseConnection(connection);
        }
    }

    /**
     * Map a ResultSet to a Product object
     *
     * @param rs ResultSet containing product data
     * @return Product object
     * @throws SQLException if a database access error occurs
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setStockQuantity(rs.getInt("stock_quantity"));
        product.setCategory(rs.getString("category"));
        product.setImageUrl(rs.getString("image_url"));
        product.setActive(rs.getBoolean("active"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            product.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            product.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return product;
    }
}

