package com.bitas.ecommerce;

import com.bitas.ecommerce.server.HttpServer;

public class Main {
    public static void main(String[] args) {
        HttpServer server = new HttpServer();
        server.start();
    }
}