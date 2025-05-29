package com.bitas.ecommerce.server;

import com.bitas.ecommerce.controller.ProductController;
import com.bitas.ecommerce.controller.UserController;
import com.bitas.ecommerce.repository.ProductRepository;
import com.bitas.ecommerce.repository.UserRepository;
import com.bitas.ecommerce.router.Router;
import com.bitas.ecommerce.service.ProductService;
import com.bitas.ecommerce.service.UserService;
import com.bitas.ecommerce.utils.AppConfig;
import com.bitas.ecommerce.utils.DbConnection;
import com.bitas.ecommerce.utils.JsonUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HttpServer {

    private final int PORT;
    private final Router router;
    private final DbConnection dbConnection;

    public HttpServer() {
        // Load configuration settings
        this.PORT = AppConfig.getInt("server.port"); // Default to 8080 if not set

        // Initialize database connection
        this.dbConnection = new DbConnection();

        // Initialize repositories
        UserRepository userRepository = new UserRepository(dbConnection);
        ProductRepository productRepository = new ProductRepository(dbConnection);

        // Initialize services
        UserService userService = new UserService(userRepository);
        ProductService productService = new ProductService(productRepository);

        // Initialize JSON utility
        JsonUtil jsonUtil = new JsonUtil();

        // Initialize controllers
        UserController userController = new UserController(userService, jsonUtil);
        ProductController productController = new ProductController(productService, jsonUtil);

        // Initialize router
        this.router = new Router(userController, productController, jsonUtil);
    }

    public void start() {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening http://localhost:" + PORT);

            System.out.println("Database connection established: " + (dbConnection.getDbUser() != null));

            int cpu = Runtime.getRuntime().availableProcessors();
            System.out.println("Available processors: " + cpu);

            int threadCount = cpu * 4;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }
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
                sendErrorResponse(out, 400, "Bad Request");
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
            String responseBody = router.handleRequest(method, path, headers, body);

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

    /**
     * Send an error response to the client
     * 
     * @param out BufferedWriter to write the response
     * @param statusCode HTTP status code
     * @param message Error message
     * @throws IOException if an I/O error occurs
     */
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

}
