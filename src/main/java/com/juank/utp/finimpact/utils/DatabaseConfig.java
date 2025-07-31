package com.juank.utp.finimpact.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * Configuración de conexión a la base de datos SQL Server
 */
public class DatabaseConfig {

    private static String SERVER;
    private static String PORT;
    private static String DATABASE_NAME;
    private static String USERNAME;
    private static String PASSWORD;
    private static String CONNECTION_URL;

    static {
        Properties props = new Properties();
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                throw new RuntimeException("No se encontró el archivo database.properties en resources");
            }
            props.load(input);
            SERVER = props.getProperty("server");
            PORT = props.getProperty("port");
            DATABASE_NAME = props.getProperty("database");
            USERNAME = props.getProperty("username");
            PASSWORD = props.getProperty("password");
            CONNECTION_URL = String.format(
                "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=false;trustServerCertificate=true",
                SERVER, PORT, DATABASE_NAME
            );
        } catch (IOException e) {
            throw new RuntimeException("Error cargando database.properties", e);
        }
    }

    /**
     * Obtiene una conexión a la base de datos
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_URL, USERNAME, PASSWORD);
    }
}
