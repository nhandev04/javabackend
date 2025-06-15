package com.bitas.ecommerce.utils.database;

import com.bitas.ecommerce.model.Product;
import com.bitas.ecommerce.model.User;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    private final Connection connection;

    public DatabaseInitializer(Connection connection) {
        this.connection = connection;
    }

    private void tryExecute(Statement stmt, String sql, String description) {
        try {
            stmt.execute(sql);
            System.out.println("✔ " + description);
        } catch (Exception e) {
            System.out.println("✔ " + description + " → " + e.getMessage());
        }
    }

    public void init() {
        try (Statement stmt = connection.createStatement()) {
            // Tạo bảng Product nếu chưa có
            tryExecute(stmt, Product.CREATE_TABLE, "Create Product table");
            tryExecute(stmt, Product.CREATE_INDEX_CATEGORY, "Create index on Product category");
            tryExecute(stmt, Product.CREATE_INDEX_ACTIVE, "Create index on Product active status");

            // Tạo bảng User nếu chưa có
            tryExecute(stmt, User.CREATE_TABLE, "Create User table");
            tryExecute(stmt, User.CREATE_INDEX_ACTIVE, "Create index on User active status");

            System.out.println("Database schema initialized successfully.");
        } catch (Exception e) {
            System.err.println("Unexpected error when initializing schema: " + e.getMessage());
        }
    }
}
