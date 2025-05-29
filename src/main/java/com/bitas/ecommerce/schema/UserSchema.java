package com.bitas.ecommerce.schema;

/**
 * Contains the SQL DDL for the 'users' table.
 */
public class UserSchema {
    /**
     * SQL statement to create the users table.
     */
    public static final String CREATE_TABLE =
            "CREATE TABLE users (" +
                    "id BIGINT PRIMARY KEY IDENTITY(1,1)," +
                    "username VARCHAR(100) NOT NULL UNIQUE," +
                    "email VARCHAR(255) NOT NULL UNIQUE," +
                    "password VARCHAR(255) NOT NULL," +
                    "full_name VARCHAR(255)," +
                    "role VARCHAR(50) NOT NULL DEFAULT 'USER'," +
                    "active BIT NOT NULL DEFAULT 1" +
                    ");";

    /**
     * SQL statement to create an index on the role column.
     */
    public static final String CREATE_INDEX_ROLE =
            "CREATE INDEX idx_users_role ON users(role);";

    /**
     * SQL statement to create an index on the active column.
     */
    public static final String CREATE_INDEX_ACTIVE =
            "CREATE INDEX idx_users_active ON users(active);";
}
