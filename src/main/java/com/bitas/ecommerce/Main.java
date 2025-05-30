package com.bitas.ecommerce;

import com.bitas.ecommerce.server.HttpServer;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        // Initialize the HTTP server
        HttpServer server = new HttpServer();
        try {
            // Connect to the database
            Connection serverConnection = server.connectDatabase();

            // Create repositories and services
            server.createRouter(serverConnection);

            // Start the server to listen for incoming requests
            server.startServer();
        } catch (Exception e) {
            System.err.println("Error starting server: " + e.getMessage());
            server.closDatabase();
            server.closeServer();
        }
    }
}