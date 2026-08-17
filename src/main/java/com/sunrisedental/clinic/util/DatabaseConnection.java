package com.sunrisedental.clinic.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages MySQL database connections using JDBC.
 */
public final class DatabaseConnection {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new IllegalStateException("db.properties not found in classpath.");
            }
            PROPS.load(input);
            Class.forName(PROPS.getProperty("db.driver"));
        } catch (IOException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError("Failed to load database configuration: " + e.getMessage());
        }
    }

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                PROPS.getProperty("db.url"),
                PROPS.getProperty("db.username"),
                PROPS.getProperty("db.password")
        );
    }

    static Connection getServerConnection() throws SQLException {
        String databaseUrl = PROPS.getProperty("db.url");
        int queryIndex = databaseUrl.indexOf('?');
        String pathPart = queryIndex >= 0
                ? databaseUrl.substring(0, queryIndex)
                : databaseUrl;
        String queryPart = queryIndex >= 0
                ? databaseUrl.substring(queryIndex)
                : "";
        int lastSlash = pathPart.lastIndexOf('/');
        String serverUrl = pathPart.substring(0, lastSlash + 1) + queryPart;

        return DriverManager.getConnection(
                serverUrl,
                PROPS.getProperty("db.username"),
                PROPS.getProperty("db.password")
        );
    }

    static String getDatabaseName() {
        String databaseUrl = PROPS.getProperty("db.url");
        int queryIndex = databaseUrl.indexOf('?');
        String pathPart = queryIndex >= 0
                ? databaseUrl.substring(0, queryIndex)
                : databaseUrl;
        String databaseName = pathPart.substring(pathPart.lastIndexOf('/') + 1);

        if (!databaseName.matches("[A-Za-z0-9_]+")) {
            throw new IllegalStateException("Invalid database name in db.properties.");
        }
        return databaseName;
    }
}
