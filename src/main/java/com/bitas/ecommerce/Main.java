package com.bitas.ecommerce;

import com.bitas.ecommerce.server.HttpServer;

public class Main {
    public static void main(String[] args) {
        HttpServer server = HttpServer.getInstance();
        try {
            server.createRouter();
            server.startServer();
        } catch (Exception e) {
            System.err.println("❌ Error starting server: " + e.getMessage());
            server.closDatabase(); // nếu cần close cache, pool, etc.
            server.closeServer();
        }
    }
}
