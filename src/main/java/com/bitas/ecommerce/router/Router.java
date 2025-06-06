package com.bitas.ecommerce.router;

import com.bitas.ecommerce.functional.TriFunction;
import com.bitas.ecommerce.utils.JsonUtil;
import com.bitas.ecommerce.controller.UserController;
import com.bitas.ecommerce.controller.AuthController;
import com.bitas.ecommerce.repository.UserRepository;
import com.bitas.ecommerce.service.UserService;
import com.bitas.ecommerce.service.AuthService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Router class for handling HTTP requests and routing them to the appropriate controller.
 * Simulates a RESTful routing system without using a framework.
 */
public class Router {
    private final JsonUtil jsonUtil;
    private final List<Route> routes = new ArrayList<>();

    /**
     * Constructor with only JsonUtil dependency.
     */
    public Router() {
        this.jsonUtil = new JsonUtil();
        // Dependency setup
        UserRepository userRepository = new UserRepository();
        UserService userService = new UserService(userRepository);
        AuthService authService = new AuthService(userService);
        UserController userController = new UserController(userService);
        AuthController authController = new AuthController(authService);

        // User routes
        pushRoute("POST", "/users", (path, body, headers) -> userController.createUser(body));
        pushRoute("PUT", Pattern.compile("/users/\\d+"), (path, body, headers) -> {
            String id = path.substring(path.lastIndexOf('/') + 1);
            return userController.updateUser(id, body);
        });

        // Auth routes
        pushRoute("POST", "/auth/login", (path, body, headers) -> authController.login(body));
        pushRoute("GET", "/auth/getme", (path, body, headers) -> authController.getMe(headers));
        pushRoute("POST", "/auth/logout", (path, body, headers) -> authController.logout(headers));
    }

    /**
     * Dynamically push a route with an action handler.
     */
    public void pushRoute(String method, Object pathMatcher, TriFunction<String, String, Map<String, String>, String> action) {
        routes.add(new Route(method, pathMatcher, action));
    }

    public String handleRequest(String method, String path, Map<String, String> headers, String body) {
        try {
            for (Route route : routes) {
                if (route.matches(method, path)) {
                    return route.execute(path, body, headers);
                }
            }
            return createNotFoundResponse(method, path);
        } catch (Exception e) {
            return createErrorResponse(500, "Internal Server Error: " + e.getMessage());
        }
    }

    public String parseRequestBody(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private String createNotFoundResponse(String method, String path) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 404);
        error.put("error", "Not Found");
        error.put("message", "No route found for " + method + " " + path);
        return jsonUtil.toJson(error);
    }

    private String createErrorResponse(int statusCode, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", statusCode);
        error.put("error", message);
        return jsonUtil.toJson(error);
    }

    // Route class to encapsulate route information
    private static class Route {

        private final String method;
        private final Object pathMatcher; // Can be String or Pattern
        private final TriFunction<String, String, Map<String, String>, String> action;

        public Route(String method, Object pathMatcher, TriFunction<String, String, Map<String, String>, String> action) {
            String API_BASE_PATH = "/api/v1";
            this.method = method;
            this.pathMatcher = API_BASE_PATH +  pathMatcher;
            this.action = action;
        }

        public boolean matches(String method, String path) {
            if (!this.method.equals(method)) {
                return false;
            }
            if (pathMatcher instanceof String) {
                return path.equals(pathMatcher);
            } else if (pathMatcher instanceof Pattern) {
                return ((Pattern) pathMatcher).matcher(path).matches();
            }
            return false;
        }

        public String execute(String path, String body, Map<String, String> headers) {
            return action.apply(path, body, headers);
        }
    }
}
