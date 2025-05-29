package com.bitas.ecommerce.server;

import com.bitas.ecommerce.utils.AppConfig;
import com.bitas.ecommerce.utils.DbConnection;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HttpServer {

    private final int PORT;

    public HttpServer() {
        // Load configuration settings
        this.PORT = AppConfig.getInt("server.port"); // Default to 8080 if not set
    }

    public void start() {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening http://localhost:" + PORT);

            DbConnection connection = new DbConnection();

            System.out.println("Database connection established: " + (connection.getDbUser() != null));

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
            System.out.println("📥 Request: " + requestLine);

            // Skip the following header lines
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
//                System.out.println("📄 Header: " + line);
            }

            // Create a simple HTTP response
            String responseBody = "Hello from Java";
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "Content-Length: " + responseBody.length() + "\r\n" +
                    "\r\n" +
                    responseBody;

            // Send the response
            out.write(response);
            out.flush();
            socket.close();
        } catch (IOException e) {
            System.err.println("❗ Error handling client: " + e.getMessage());
        }
    }

}