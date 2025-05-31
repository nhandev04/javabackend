package com.bitas.ecommerce.utils.database;

import java.sql.Connection;
import com.bitas.ecommerce.utils.AppConfig;

public class DbConnection {
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public DbConnection() {
        this.dbUrl = AppConfig.get("db.url");
        this.dbUser = AppConfig.get("db.username");
        this.dbPassword = AppConfig.get("db.password");
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
        return ConnectionPool.getConnection();
    }

    public void releaseConnection(Connection conn) {
        ConnectionPool.releaseConnection(conn);
    }
}
