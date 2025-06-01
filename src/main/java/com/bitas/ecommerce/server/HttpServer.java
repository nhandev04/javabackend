package com.bitas.ecommerce.server;

import com.bitas.ecommerce.controller.ProductController;
import com.bitas.ecommerce.controller.UserController;
import com.bitas.ecommerce.controller.AuthController;
import com.bitas.ecommerce.repository.ProductRepository;
import com.bitas.ecommerce.repository.UserRepository;
import com.bitas.ecommerce.router.Router;
import com.bitas.ecommerce.service.ProductService;
import com.bitas.ecommerce.service.UserService;
import com.bitas.ecommerce.service.AuthService;
import com.bitas.ecommerce.utils.AppConfig;
import com.bitas.ecommerce.utils.database.DbConnection;
import com.bitas.ecommerce.utils.JsonUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HttpServer {

    private final int PORT;
    private final Router router;
    private Connection connection;

    private boolean isRunning = true;

    public HttpServer() {
        this.PORT = AppConfig.getInt("server.port"); // Default to 8080 if not set
        this.router = new Router();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening http://localhost:" + PORT);

            int cpu = Runtime.getRuntime().availableProcessors();

            int threadCount = cpu * 4;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(() -> this.handleClient(clientSocket));
            }

        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }
    }

    public Connection connectDatabase() {
        // Create a new DbConnection instance to establish the connection
        DbConnection dbConnection = new DbConnection();
        this.connection = dbConnection.getConnection();
        
        System.out.println("✅ Connected to SQL Server successfully");
        return dbConnection.getConnection();
    }

    public void createRouter() {
        // Initialize repositories
        UserRepository userRepository = new UserRepository();
        ProductRepository productRepository = new ProductRepository();

        // Initialize services
        UserService userService = new UserService(userRepository);
        ProductService productService = new ProductService(productRepository);
        AuthService authService = new AuthService(userService); // Add AuthService

        // Initialize controllers
        UserController userController = new UserController(userService);
        ProductController productController = new ProductController(productService);
        AuthController authController = new AuthController(authService); // Add AuthController

        // User routes
        router.pushRoute("GET", "/users", (path, body, headers) -> userController.getAllUsers());
        router.pushRoute("GET", "/users/:id", (path, body, headers) -> userController.getUser(body));
        router.pushRoute("POST", "/users", (path, body, headers) -> userController.createUser(body));
        router.pushRoute("PUT", "/users/:id", (path, body, headers) -> {
            String id = path.split("/")[2];
            return userController.updateUser(id, body);
        });
        router.pushRoute("DELETE", "/users/:id", (path, body, headers) -> {
            String id = path.split("/")[2];
            return userController.deleteUser(id);
        });
        router.pushRoute("POST", "/users/authenticate", (path, body, headers) -> userController.authenticateUser(body));

        // Product routes
        router.pushRoute("GET", "/products", (path, body, headers) -> productController.getAllProducts());
        router.pushRoute("GET", "/products/:id", (path, body, headers) -> productController.getProduct(body));
        router.pushRoute("POST", "/products", (path, body, headers) -> productController.createProduct(body));
        router.pushRoute("PUT", "/products/:id", (path, body, headers) -> {
            String id = path.split("/")[2];
            return productController.updateProduct(id, body);
        });
        router.pushRoute("DELETE", "/products/:id", (path, body, headers) -> {
            String id = path.split("/")[2];
            return productController.deleteProduct(id);
        });
        router.pushRoute("PATCH", "/products/:id/stock", (path, body, headers) -> {
            String id = path.split("/")[2];
            return productController.updateStockQuantity(id, body);
        });
        router.pushRoute("PATCH", "/products/:id/price", (path, body, headers) -> {
            String id = path.split("/")[2];
            return productController.updatePrice(id, body);
        });
        router.pushRoute("GET", "/products/category/:category", (path, body, headers) -> {
            String[] parts = path.split("/");
            String category = parts.length > 3 ? parts[3] : "";
            return productController.getProductsByCategory(category);
        });

        // Auth routes
        router.pushRoute("POST", "/auth/login", (path, body, headers) -> authController.login(body));
        router.pushRoute("GET", "/auth/me", (path, body, headers) -> authController.getMe(headers));
        router.pushRoute("POST", "/auth/logout", (path, body, headers) -> authController.logout(headers));
    }

    private void handleClient(Socket socket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {
            // Read the first line of the HTTP request
            String requestLine = in.readLine();
            if (requestLine == null) {
                return; // Empty request
            }

            System.out.println("📥 Request: " + requestLine);

            // Parse the request line (e.g., "GET /users HTTP/1.1")
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 3) {
                this.sendErrorResponse(out, 400, "Bad Request");
                return;
            }

            String method = requestParts[0]; // GET, POST, PUT, DELETE, etc.
            String path = requestParts[1];   // /users, /products, etc.

            // Parse headers
            Map<String, String> headers = new HashMap<>();
            String headerLine;
            while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
                int colonPos = headerLine.indexOf(':');
                if (colonPos > 0) {
                    String headerName = headerLine.substring(0, colonPos).trim();
                    String headerValue = headerLine.substring(colonPos + 1).trim();
                    headers.put(headerName, headerValue);
                }
            }

            // Read request body if present
            String body = "";
            if (headers.containsKey("Content-Length")) {
                int contentLength = Integer.parseInt(headers.get("Content-Length"));
                if (contentLength > 0) {
                    char[] bodyChars = new char[contentLength];
                    in.read(bodyChars, 0, contentLength);
                    body = new String(bodyChars);
                }
            }

            // Use the router to handle the request
            String responseBody = this.router.handleRequest(method, path, headers, body);

            // Create HTTP response
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: application/json\r\n" +
                    "Content-Length: " + responseBody.length() + "\r\n" +
                    "Access-Control-Allow-Origin: *\r\n" +  // Allow CORS
                    "Access-Control-Allow-Methods: GET, POST, PUT, DELETE, PATCH\r\n" +
                    "Access-Control-Allow-Headers: Content-Type\r\n" +
                    "\r\n" +
                    responseBody;

            // Send the response
            out.write(response);
            out.flush();
        } catch (IOException e) {
            System.err.println("❗ Error handling client: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("❗ Error closing socket: " + e.getMessage());
            }
        }
    }


    private void sendErrorResponse(BufferedWriter out, int statusCode, String message) throws IOException {
        Map<String, Object> error = new HashMap<>();
        error.put("status", statusCode);
        error.put("error", message);

        String errorJson = new JsonUtil().toJson(error);

        String response = "HTTP/1.1 " + statusCode + " " + message + "\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + errorJson.length() + "\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Access-Control-Allow-Methods: GET, POST, PUT, DELETE, PATCH\r\n" +
                "Access-Control-Allow-Headers: Content-Type\r\n" +
                "\r\n" +
                errorJson;

        out.write(response);
        out.flush();
    }

    public Connection getConnection() {
        return this.connection;
    }

    public int getPort() {
        return this.PORT;
    }

    public Router getRouter() {
        return this.router;
    }


    public void closDatabase() {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (java.sql.SQLException e) {
                System.err.println("❗ Error closing database connection: " + e.getMessage());
            }
        }
    }

    public void closeServer() {
        this.isRunning = false;
    }
}
