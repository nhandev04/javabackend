package com.bitas.ecommerce.controller;

import com.bitas.ecommerce.model.User;
import com.bitas.ecommerce.service.UserService;
import com.bitas.ecommerce.utils.JsonUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller class for handling user-related HTTP requests.
 * Maps HTTP requests to service methods and formats responses.
 */
public class UserController {
    private final UserService userService;
    private final JsonUtil jsonUtil;

    /**
     * Constructor with UserService dependency
     */
    public UserController(UserService userService) {
        this.userService = userService;
        this.jsonUtil = new JsonUtil();
    }

    /**
     * Handle GET request for a user by ID
     *
     * @param requestBody User ID
     * @return JSON response with user data or error message
     */
    public String getUser(String requestBody) {

        Map<String, Object> bodyMap = jsonUtil.fromJson(requestBody, Map.class);
        String id = bodyMap.get("id") != null ? bodyMap.get("id").toString() : null;

        try {
            Long userId = Long.parseLong(id);
            Optional<User> userOpt = userService.getUserById(userId);

            if (userOpt.isPresent()) {
                return jsonUtil.toJson(userOpt.get());
            } else {
                return createErrorResponse(404, "User not found with ID: " + id);
            }
        } catch (NumberFormatException e) {
            return createErrorResponse(400, "Invalid user ID format");
        } catch (Exception e) {
            return createErrorResponse(500, "Error retrieving user: " + e.getMessage());
        }
    }

    /**
     * Handle GET request for all users
     *
     * @return JSON response with list of users
     */
    public String getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return jsonUtil.toJson(users);
        } catch (Exception e) {
            return createErrorResponse(500, "Error retrieving users: " + e.getMessage());
        }
    }

    /**
     * Handle POST request to create a new user
     *
     * @param requestBody JSON request body with user data
     * @return JSON response with created user or error message
     */
    public String createUser(String requestBody) {
        try {
            User user = jsonUtil.fromJson(requestBody, User.class);
            User createdUser = userService.createUser(user);
            return jsonUtil.toJson(createdUser);
        } catch (IllegalArgumentException e) {
            return createErrorResponse(400, e.getMessage());
        } catch (Exception e) {
            return createErrorResponse(500, "Error creating user: " + e.getMessage());
        }
    }

    /**
     * Handle PUT request to update an existing user
     *
     * @param id          User ID
     * @param requestBody JSON request body with updated user data
     * @return JSON response with updated user or error message
     */
    public String updateUser(String id, String requestBody) {
        try {
            Long userId = Long.parseLong(id);
            User user = jsonUtil.fromJson(requestBody, User.class);
            User updatedUser = userService.updateUser(userId, user);
            return jsonUtil.toJson(updatedUser);
        } catch (NumberFormatException e) {
            return createErrorResponse(400, "Invalid user ID format");
        } catch (IllegalArgumentException e) {
            return createErrorResponse(400, e.getMessage());
        } catch (Exception e) {
            return createErrorResponse(500, "Error updating user: " + e.getMessage());
        }
    }

    /**
     * Handle DELETE request to delete a user
     *
     * @param id User ID
     * @return JSON response with success or error message
     */
    public String deleteUser(String id) {
        try {
            Long userId = Long.parseLong(id);
            boolean deleted = userService.deleteUser(userId);

            if (deleted) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "User deleted successfully");
                return jsonUtil.toJson(response);
            } else {
                return createErrorResponse(404, "User not found with ID: " + id);
            }
        } catch (NumberFormatException e) {
            return createErrorResponse(400, "Invalid user ID format");
        } catch (Exception e) {
            return createErrorResponse(500, "Error deleting user: " + e.getMessage());
        }
    }

    /**
     * Handle POST request for user authentication
     *
     * @param requestBody JSON request body with username and password
     * @return JSON response with authenticated user or error message
     */
    public String authenticateUser(String requestBody) {
        try {
            Map<String, String> credentials = jsonUtil.fromJson(requestBody, Map.class);
            String username = credentials.get("username");
            String password = credentials.get("password");

            if (username == null || password == null) {
                return createErrorResponse(400, "Username and password are required");
            }

            Optional<User> userOpt = userService.authenticateUser(username, password);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // Don't include password in response
                user.setPassword(null);
                return jsonUtil.toJson(user);
            } else {
                return createErrorResponse(401, "Invalid username or password");
            }
        } catch (Exception e) {
            return createErrorResponse(500, "Error during authentication: " + e.getMessage());
        }
    }

    /**
     * Create a JSON error response
     *
     * @param statusCode HTTP status code
     * @param message    Error message
     * @return JSON string with error details
     */
    private String createErrorResponse(int statusCode, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", statusCode);
        error.put("error", message);
        return jsonUtil.toJson(error);
    }

}