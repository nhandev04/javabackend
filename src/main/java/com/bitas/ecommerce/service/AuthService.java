package com.bitas.ecommerce.service;

import com.bitas.ecommerce.model.User;
import com.bitas.ecommerce.utils.auth.JWT;
import com.bitas.ecommerce.utils.AppConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AuthService {
    private final UserService userService;
    private final String jwtSecret;

    public AuthService(UserService userService) {
        this.userService = userService;
        this.jwtSecret = AppConfig.get("jwt.secret");
    }

    public String login(String username, String password) throws Exception {
        Optional<User> userOpt = userService.getUserByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String hashed = userService.authenticateUser(username, password).map(User::getPassword).orElse(null);
            if (hashed != null && hashed.equals(user.getPassword())) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("id", user.getId());
                payload.put("username", user.getUsername());
                payload.put("role", user.getRole());
                String payloadJson = new com.bitas.ecommerce.utils.JsonUtil().toJson(payload);
                return JWT.sign(payloadJson, jwtSecret);
            }
        }
        throw new IllegalArgumentException("Invalid username or password");
    }

    public Optional<User> getMe(String token) throws Exception {
        if (token == null || token.isEmpty()) return Optional.empty();
        if (!JWT.verify(token, jwtSecret)) return Optional.empty();
        String payloadJson = JWT.getPayload(token);
        Map payload = new com.bitas.ecommerce.utils.JsonUtil().fromJson(payloadJson, Map.class);
        Long userId = Long.parseLong(payload.get("id").toString());
        return userService.getUserById(userId);
    }

    public boolean logout(String token) {
        // For stateless JWT, logout is handled on client side (token removal)
        return true;
    }
}

