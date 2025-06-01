package com.bitas.ecommerce.service;

import com.bitas.ecommerce.model.User;
import com.bitas.ecommerce.repository.UserRepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

/**
 * Service class for User entity.
 * Contains business logic for user operations.
 */
public class UserService {
    private final UserRepository userRepository;
    
    /**
     * Constructor with UserRepository dependency
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Get user by ID
     * 
     * @param id User ID
     * @return Optional containing User if found, empty Optional otherwise
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * Get user by username
     * 
     * @param username Username to search for
     * @return Optional containing User if found, empty Optional otherwise
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Get all users
     * 
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Create a new user with hashed password
     *
     * @param user User to create
     * @return Created user with ID
     */
    public User createUser(User user) {
        // Validate user data
        validateUserData(user);
        
        // Check if username already exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        // Set default values if needed
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }
        
        // Hash password
        user.setPassword(hashPassword(user.getPassword()));

        // Save user
        return userRepository.save(user);
    }
    
    /**
     * Update an existing user (does not re-hash password unless changed)
     *
     * @param id User ID
     * @param user User data to update
     * @return Updated user
     */
    public User updateUser(Long id, User user) {
        // Validate user data
        validateUserData(user);
        
        // Check if user exists
        Optional<User> existingUser = userRepository.findById(id);
        if (!existingUser.isPresent()) {
            throw new IllegalArgumentException("User not found with ID: " + id);
        }
        
        // Check if username is being changed and if it already exists
        if (!existingUser.get().getUsername().equals(user.getUsername()) &&
            userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        // Hash password if changed
        if (!user.getPassword().equals(existingUser.get().getPassword())) {
            user.setPassword(hashPassword(user.getPassword()));
        }

        // Set ID and save
        user.setId(id);
        return userRepository.save(user);
    }
    
    /**
     * Delete a user by ID
     * 
     * @param id ID of the user to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteUser(Long id) {
        return userRepository.deleteById(id);
    }
    
    /**
     * Authenticate a user
     * 
     * @param username Username
     * @param password Password
     * @return Optional containing User if authentication successful, empty Optional otherwise
     */
    public Optional<User> authenticateUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String hashedPassword = hashPassword(password);
            // In a real application, you would use a password encoder to compare passwords
            if (user.getPassword().equals(hashedPassword) && user.isActive()) {
                return Optional.of(user);
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Validate user data
     * 
     * @param user User to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateUserData(User user) {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        
        // Add more validation as needed
    }

    /**
     * Hash password using SHA-256
     *
     * @param password Password to hash
     * @return Hashed password
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}

