package com.bitas.ecommerce.model;

/**
 * User model class representing a user in the system.
 * This class contains user information such as ID, username, email, password, etc.
 */
public class User {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String role;
    private boolean active;
    
    /**
     * Default constructor
     */
    public User() {
    }
    
    /**
     * Constructor with parameters
     */
    public User(Long id, String username, String email, String password, String fullName, String role, boolean active) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                '}';
    }

    // --- Schema DDL moved from UserSchema ---
    public static final String CREATE_TABLE =
            "CREATE TABLE users (" +
            "id BIGINT PRIMARY KEY IDENTITY(1,1)," +
            "username VARCHAR(100) NOT NULL UNIQUE," +
            "email VARCHAR(255) NOT NULL UNIQUE," +
            "password VARCHAR(255) NOT NULL," +
            "full_name VARCHAR(255)," +
            "role VARCHAR(50) NOT NULL DEFAULT 'USER'," +
            "active BIT NOT NULL DEFAULT 1" +
            ");";

    public static final String CREATE_INDEX_ROLE =
            "CREATE INDEX idx_users_role ON users(role);";

    public static final String CREATE_INDEX_ACTIVE =
            "CREATE INDEX idx_users_active ON users(active);";
    // --- End schema DDL ---
}