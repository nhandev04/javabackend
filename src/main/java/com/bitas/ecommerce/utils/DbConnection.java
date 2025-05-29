package com.bitas.ecommerce.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {
    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    public DbConnection() {
        try {
            this.dbUrl = AppConfig.get("db.url");
            this.dbUser = AppConfig.get("db.username");
            this.dbPassword = AppConfig.get("db.password");

            Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

            DatabaseInitializer dbInitializer = new DatabaseInitializer(this);
            dbInitializer.init();
            
            System.out.println("✅ Connected to SQL Server successfully!" + connection.getClientInfo());


        } catch (SQLException e) {
            System.err.println("❌ Failed to connect to the database:");
            e.printStackTrace();
        }
    }


    public String getDbPassword() {
        return dbPassword;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }
}
