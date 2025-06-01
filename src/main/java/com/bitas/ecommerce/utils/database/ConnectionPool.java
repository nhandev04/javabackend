package com.bitas.ecommerce.utils.database;

import com.bitas.ecommerce.utils.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Vector;

public class ConnectionPool {
    private static final Vector<Connection> pool = new Vector<>();
    private static final int POOL_SIZE = 5;
    private static final int TIMEOUT_MS = 10000; // 5s timeout khi chờ connection

    static {
        try {
            String dbUrl = AppConfig.get("db.url");
            String dbUser = AppConfig.get("db.username");
            String dbPassword = AppConfig.get("db.password");

            for (int i = 0; i < POOL_SIZE; i++) {
                Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                if (i == 0) {
                    new DatabaseInitializer(conn).init();
                }
                pool.add(conn);
            }
        } catch (SQLException e) {
            throw new RuntimeException("❌ Cannot create connection pool", e);
        }
    }

    public static Connection getConnection() {
        long startTime = System.currentTimeMillis();

        synchronized (pool) {
            while (pool.isEmpty()) {
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed > TIMEOUT_MS) {
                    throw new RuntimeException("❌ Timeout waiting for a database connection");
                }
                try {
                    pool.wait(TIMEOUT_MS - elapsed);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("❌ Thread interrupted while waiting for DB connection", e);
                }
            }

            Connection conn = pool.remove(0);

            try {
                if (conn == null || !conn.isValid(2)) {
                    // Reconnect if connection is invalid
                    return recreateConnection();
                }
            } catch (SQLException e) {
                return recreateConnection();
            }

            return conn;
        }
    }

    public static void releaseConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                synchronized (pool) {
                    pool.add(conn);
                    pool.notify();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("❌ Error releasing database connection", e);
        }
    }

    private static Connection recreateConnection() {
        try {
            String dbUrl = AppConfig.get("db.url");
            String dbUser = AppConfig.get("db.username");
            String dbPassword = AppConfig.get("db.password");
            return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (SQLException e) {
            throw new RuntimeException("❌ Cannot recreate DB connection", e);
        }
    }
}
