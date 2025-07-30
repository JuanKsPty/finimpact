package com.juank.utp.finimpact.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Configuración de conexión a la base de datos SQL Server
 */
public class DatabaseConfig {

    // TODO: Configurar estos valores según tu entorno
    private static final String SERVER = "localhost"; // O tu servidor
    private static final String PORT = "1433";
    private static final String DATABASE_NAME = "finimpact";
    private static final String USERNAME = "sa"; // TODO: Agregar usuario
    private static final String PASSWORD = "Password123#"; // TODO: Agregar contraseña

    private static final String CONNECTION_URL = String.format(
        "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=false;trustServerCertificate=true",
        SERVER, PORT, DATABASE_NAME
    );

    /**
     * Obtiene una conexión a la base de datos
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(CONNECTION_URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver de SQL Server no encontrado", e);
        }
    }

    /**
     * Cierra una conexión de forma segura
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
}
