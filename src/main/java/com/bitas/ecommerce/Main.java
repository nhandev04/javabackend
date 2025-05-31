package com.bitas.ecommerce;

import com.bitas.ecommerce.server.HttpServer;

public class Main {
    public static void main(String[] args) {
        // Initialize the HTTP server
        HttpServer server = new HttpServer();

        try {
            // Create repositories and services (connection pool đã init sẵn)
            server.createRouter();

            // Start the server
            server.startServer();
        } catch (Exception e) {
            System.err.println("❌ Error starting server: " + e.getMessage());
            server.closDatabase(); // nếu cần close cache, pool, etc.
            server.closeServer();
        }
    }
}
