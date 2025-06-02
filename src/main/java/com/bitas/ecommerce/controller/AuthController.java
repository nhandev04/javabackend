package com.bitas.ecommerce.controller;

import com.bitas.ecommerce.service.AuthService;
import com.bitas.ecommerce.utils.JsonUtil;

import java.util.HashMap;
import java.util.Map;

public class AuthController {
    private final AuthService authService;
    private final JsonUtil jsonUtil;

    public AuthController(AuthService authService) {
        this.authService = authService;
        this.jsonUtil = new JsonUtil();
    }

    public String login(String requestBody) {
        try {
            Map<String, String> body = jsonUtil.fromJson(requestBody, Map.class);
            String username = body.get("username");
            String password = body.get("password");
            String token = authService.login(username, password);
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return jsonUtil.toJson(response);
        } catch (IllegalArgumentException e) {
            return createErrorResponse(401, e.getMessage());
        } catch (Exception e) {
            return createErrorResponse(500, "Login error: " + e.getMessage());
        }
    }

    public String getMe(Map<String, String> headers) {
        try {
            String token = headers.getOrDefault("Authorization", "").replace("Bearer ", "");
            return authService.getMe(token)
                    .map(jsonUtil::toJson)
                    .orElse(createErrorResponse(404, "User not found"));
        } catch (Exception e) {
            return createErrorResponse(401, "Invalid or expired token");
        }
    }

    public String logout(Map<String, String> headers) {
        String token = headers.getOrDefault("Authorization", "").replace("Bearer ", "");
        boolean result = authService.logout(token);
        Map<String, Object> response = new HashMap<>();
        response.put("success", result);
        return jsonUtil.toJson(response);
    }

    private String createErrorResponse(int status, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", status);
        error.put("error", message);
        return jsonUtil.toJson(error);
    }
}

