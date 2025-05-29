package com.bitas.ecommerce.utils;

import com.bitas.ecommerce.schema.ProductSchema;
import com.bitas.ecommerce.schema.UserSchema;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseInitializer {

    private final DbConnection dbConnection;

    public DatabaseInitializer(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public void init() {
        try (Connection conn = DriverManager.getConnection(
                dbConnection.getDbUrl(),
                dbConnection.getDbUser(),
                dbConnection.getDbPassword());
             Statement stmt = conn.createStatement()) {

            // Tạo bảng Product nếu chưa có
            stmt.execute(ProductSchema.CREATE_TABLE);
            stmt.execute(ProductSchema.CREATE_INDEX_CATEGORY);
            stmt.execute(ProductSchema.CREATE_INDEX_ACTIVE);

            // Tạo bảng User nếu chưa có
            stmt.execute(UserSchema.CREATE_TABLE);
            stmt.execute(UserSchema.CREATE_INDEX_ACTIVE);

            System.out.println("Database schema initialized successfully.");

        } catch (Exception e) {
            System.err.println("Error initializing database schema: " + e.getMessage());
        }
    }
}
