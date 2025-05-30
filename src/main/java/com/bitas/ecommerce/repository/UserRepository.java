package com.bitas.ecommerce.repository;

import com.bitas.ecommerce.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository class for User entity.
 * Handles database operations related to users.
 */
public class UserRepository {
    private final Connection connection;

    /**
     * Constructor with DbConnection dependency
     */
    public UserRepository(Connection connection) {
        this.connection = connection;
    }

    /**
     * Find user by ID
     *
     * @param id User ID
     * @return Optional containing User if found, empty Optional otherwise
     */
    public Optional<User> findById(Long id) {
        // SQL query to find user by ID
        String sql = "SELECT * FROM users WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                return Optional.of(user);
            }

        } catch (SQLException e) {
            System.err.println("Error finding user by ID: " + e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * Find user by username
     *
     * @param username Username to search for
     * @return Optional containing User if found, empty Optional otherwise
     */
    public Optional<User> findByUsername(String username) {
        // SQL query to find user by username
        String sql = "SELECT * FROM users WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                return Optional.of(user);
            }

        } catch (SQLException e) {
            System.err.println("Error finding user by username: " + e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * Find all users
     *
     * @return List of all users
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = mapResultSetToUser(rs);
                users.add(user);
            }

        } catch (SQLException e) {
            System.err.println("Error finding all users: " + e.getMessage());
        }

        return users;
    }

    /**
     * Save a new user or update an existing one
     *
     * @param user User to save
     * @return Saved user with ID
     */
    public User save(User user) {
        if (user.getId() == null) {
            return insert(user);
        } else {
            return update(user);
        }
    }

    /**
     * Insert a new user
     *
     * @param user User to insert
     * @return Inserted user with ID
     */
    private User insert(User user) {
        String sql = "INSERT INTO users (username, email, password, full_name, role, active) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getRole());
            stmt.setBoolean(6, user.isActive());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getLong(1));
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
        }

        return user;
    }

    /**
     * Update an existing user
     *
     * @param user User to update
     * @return Updated user
     */
    private User update(User user) {
        String sql = "UPDATE users SET username = ?, email = ?, password = ?, " +
                "full_name = ?, role = ?, active = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getRole());
            stmt.setBoolean(6, user.isActive());
            stmt.setLong(7, user.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
        }

        return user;
    }

    /**
     * Delete a user by ID
     *
     * @param id ID of the user to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();

            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Map a ResultSet to a User object
     *
     * @param rs ResultSet containing user data
     * @return User object
     * @throws SQLException if a database access error occurs
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getBoolean("active"));
        return user;
    }
}