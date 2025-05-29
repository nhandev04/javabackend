package com.bitas.ecommerce.router;

import com.bitas.ecommerce.controller.ProductController;
import com.bitas.ecommerce.controller.UserController;
import com.bitas.ecommerce.utils.JsonUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Router class for handling HTTP requests and routing them to the appropriate controller.
 * Simulates a RESTful routing system without using a framework.
 */
public class Router {
    private final UserController userController;
    private final ProductController productController;
    private final JsonUtil jsonUtil;

    // Route patterns
    private static final Pattern USER_ID_PATTERN = Pattern.compile("^/users/(\\d+)$");
    private static final Pattern PRODUCT_ID_PATTERN = Pattern.compile("^/products/(\\d+)$");
    private static final Pattern PRODUCT_STOCK_PATTERN = Pattern.compile("^/products/(\\d+)/stock$");
    private static final Pattern PRODUCT_PRICE_PATTERN = Pattern.compile("^/products/(\\d+)/price$");
    private static final Pattern PRODUCT_CATEGORY_PATTERN = Pattern.compile("^/products/category/([^/]+)$");

    // Route handler functional interface
    @FunctionalInterface
    private interface RouteHandler {
        String handle(String path, String body);
    }

    // Route class to encapsulate route information
    private static class Route {
        private final String method;
        private final Object pathMatcher; // Can be String or Pattern
        private final RouteHandler handler;

        public Route(String method, String exactPath, RouteHandler handler) {
            this.method = method;
            this.pathMatcher = exactPath;
            this.handler = handler;
        }

        public Route(String method, Pattern pathPattern, RouteHandler handler) {
            this.method = method;
            this.pathMatcher = pathPattern;
            this.handler = handler;
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

        public String execute(String path, String body) {
            return handler.handle(path, body);
        }
    }

    private final List<Route> routes = new ArrayList<>();

    /**
     * Constructor with controller dependencies
     */
    public Router(UserController userController, ProductController productController, JsonUtil jsonUtil) {
        this.userController = userController;
        this.productController = productController;
        this.jsonUtil = jsonUtil;

        // Initialize routes
        initializeRoutes();
    }

    /**
     * Initialize all routes
     */
    private void initializeRoutes() {
        // User routes
        routes.add(new Route("GET", "/users", (path, body) -> userController.getAllUsers()));
        routes.add(new Route("POST", "/users", (path, body) -> userController.createUser(body)));
        routes.add(new Route("POST", "/users/login", (path, body) -> userController.authenticateUser(body)));
        routes.add(new Route("GET", USER_ID_PATTERN, (path, body) -> {
            String userId = extractIdFromPath(path, USER_ID_PATTERN);
            return userController.getUser(userId);
        }));
        routes.add(new Route("PUT", USER_ID_PATTERN, (path, body) -> {
            String userId = extractIdFromPath(path, USER_ID_PATTERN);
            return userController.updateUser(userId, body);
        }));
        routes.add(new Route("DELETE", USER_ID_PATTERN, (path, body) -> {
            String userId = extractIdFromPath(path, USER_ID_PATTERN);
            return userController.deleteUser(userId);
        }));

        // Product routes
        routes.add(new Route("GET", "/products", (path, body) -> productController.getAllProducts()));
        routes.add(new Route("POST", "/products", (path, body) -> productController.createProduct(body)));
        routes.add(new Route("GET", PRODUCT_ID_PATTERN, (path, body) -> {
            String productId = extractIdFromPath(path, PRODUCT_ID_PATTERN);
            return productController.getProduct(productId);
        }));
        routes.add(new Route("PUT", PRODUCT_ID_PATTERN, (path, body) -> {
            String productId = extractIdFromPath(path, PRODUCT_ID_PATTERN);
            return productController.updateProduct(productId, body);
        }));
        routes.add(new Route("DELETE", PRODUCT_ID_PATTERN, (path, body) -> {
            String productId = extractIdFromPath(path, PRODUCT_ID_PATTERN);
            return productController.deleteProduct(productId);
        }));
        routes.add(new Route("PATCH", PRODUCT_STOCK_PATTERN, (path, body) -> {
            String productId = extractIdFromPath(path, PRODUCT_STOCK_PATTERN);
            return productController.updateStockQuantity(productId, body);
        }));
        routes.add(new Route("PATCH", PRODUCT_PRICE_PATTERN, (path, body) -> {
            String productId = extractIdFromPath(path, PRODUCT_PRICE_PATTERN);
            return productController.updatePrice(productId, body);
        }));
        routes.add(new Route("GET", PRODUCT_CATEGORY_PATTERN, (path, body) -> {
            Matcher matcher = PRODUCT_CATEGORY_PATTERN.matcher(path);
            if (matcher.find()) {
                String category = matcher.group(1);
                return productController.getProductsByCategory(category);
            }
            return null;
        }));
    }

    /**
     * Handle an HTTP request and route it to the appropriate controller method
     * 
     * @param method HTTP method (GET, POST, PUT, DELETE, PATCH)
     * @param path Request path
     * @param headers HTTP headers
     * @param body Request body
     * @return HTTP response
     */
    public String handleRequest(String method, String path, Map<String, String> headers, String body) {
        try {
            // Find matching route
            for (Route route : routes) {
                if (route.matches(method, path)) {
                    return route.execute(path, body);
                }
            }

            // Route not found
            return createNotFoundResponse(method, path);
        } catch (Exception e) {
            return createErrorResponse(500, "Internal Server Error: " + e.getMessage());
        }
    }

    /**
     * Extract ID from path using a regex pattern
     * 
     * @param path Request path
     * @param pattern Regex pattern
     * @return Extracted ID
     */
    private String extractIdFromPath(String path, Pattern pattern) {
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Parse request body from input stream
     * 
     * @param inputStream Request input stream
     * @return Request body as string
     * @throws IOException if an I/O error occurs
     */
    public String parseRequestBody(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    /**
     * Create a 404 Not Found response
     * 
     * @param method HTTP method
     * @param path Request path
     * @return JSON response with error details
     */
    private String createNotFoundResponse(String method, String path) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 404);
        error.put("error", "Not Found");
        error.put("message", "No route found for " + method + " " + path);
        return jsonUtil.toJson(error);
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
