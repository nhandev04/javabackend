package com.bitas.ecommerce;

import com.bitas.ecommerce.server.HttpServer;


public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to the E-commerce Application!");
        HttpServer server = new HttpServer();
        server.start();
    }
}