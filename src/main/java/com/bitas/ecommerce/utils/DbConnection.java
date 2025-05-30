package com.bitas.ecommerce.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private Connection connection;

    public DbConnection() {
        try {
            this.dbUrl = AppConfig.get("db.url");
            this.dbUser = AppConfig.get("db.username");
            this.dbPassword = AppConfig.get("db.password");

            Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            this.connection = connection;

            DatabaseInitializer dbInitializer = new DatabaseInitializer(connection);
            dbInitializer.init();

        } catch (SQLException e) {
            System.err.println("❌ Failed to connect to the database:");
            e.printStackTrace();
        }
    }
    
    public String getDbPassword() {
        return this.dbPassword;
    }

    public String getDbUrl() {
        return this.dbUrl;
    }

    public String getDbUser() {
        return this.dbUser;
    }

    public Connection getConnection() {
        if (this.connection == null) {
            throw new IllegalStateException("Database connection has not been established.");
        }
        return this.connection;
    }
}
