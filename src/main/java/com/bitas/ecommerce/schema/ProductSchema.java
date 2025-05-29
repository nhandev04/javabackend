package com.bitas.ecommerce.schema;

/**
 * Contains the SQL DDL for the 'products' table.
 */
public class ProductSchema {
    /**
     * SQL statement to create the products table.
     */
    public static final String CREATE_TABLE =
            "CREATE TABLE products (" +
                    "id BIGINT PRIMARY KEY IDENTITY(1,1)," +
                    "name VARCHAR(255) NOT NULL," +
                    "description TEXT," +
                    "price DECIMAL(15, 2) NOT NULL," +
                    "stock_quantity INT NOT NULL DEFAULT 0," +
                    "category VARCHAR(100)," +
                    "image_url VARCHAR(512)," +
                    "active BIT NOT NULL DEFAULT 1," +
                    "created_at DATETIME2 NOT NULL DEFAULT GETDATE()," +
                    "updated_at DATETIME2 NULL" +
                    ");";

    /**
     * SQL statement to create an index on the category column.
     */
    public static final String CREATE_INDEX_CATEGORY =
            "CREATE INDEX idx_products_category ON products(category);";

    /**
     * SQL statement to create an index on the active column.
     */
    public static final String CREATE_INDEX_ACTIVE =
            "CREATE INDEX idx_products_active ON products(active);";
}
