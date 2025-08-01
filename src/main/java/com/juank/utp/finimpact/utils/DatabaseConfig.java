package com.juank.utp.finimpact.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Configuración de conexión a la base de datos SQL Server con pool de conexiones
 */
public class DatabaseConfig {

    private static String SERVER;
    private static String PORT;
    private static String DATABASE_NAME;
    private static String USERNAME;
    private static String PASSWORD;
    private static String CONNECTION_URL;

    // Pool de conexiones
    private static final int MAX_CONNECTIONS = 5;
    private static final BlockingQueue<Connection> connectionPool = new LinkedBlockingQueue<>();
    private static volatile boolean poolInitialized = false;

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
                "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=false;trustServerCertificate=true;loginTimeout=10",
                SERVER, PORT, DATABASE_NAME
            );

            // Inicializar el pool
            initializePool();

        } catch (IOException e) {
            throw new RuntimeException("Error cargando database.properties", e);
        }
    }

    /**
     * Inicializa el pool de conexiones
     */
    private static synchronized void initializePool() {
        if (!poolInitialized) {
            try {
                for (int i = 0; i < MAX_CONNECTIONS; i++) {
                    Connection conn = createConnection();
                    connectionPool.offer(conn);
                }
                poolInitialized = true;
                System.out.println("✅ Pool de conexiones inicializado con " + MAX_CONNECTIONS + " conexiones");
            } catch (SQLException e) {
                System.err.println("❌ Error inicializando pool de conexiones: " + e.getMessage());
                throw new RuntimeException("Error inicializando pool de conexiones", e);
            }
        }
    }

    /**
     * Crea una nueva conexión
     */
    private static Connection createConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_URL, USERNAME, PASSWORD);
    }

    /**
     * Obtiene una conexión del pool
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = connectionPool.poll(5, TimeUnit.SECONDS);
            if (conn == null || conn.isClosed()) {
                // Si no hay conexiones disponibles o está cerrada, crear una nueva
                System.out.println("⚠️ Creando nueva conexión - pool agotado o conexión cerrada");
                return createConnection();
            }
            return new PooledConnection(conn);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Timeout obteniendo conexión del pool", e);
        }
    }

    /**
     * Devuelve una conexión al pool
     */
    public static void returnConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    connectionPool.offer(conn);
                }
            } catch (SQLException e) {
                System.err.println("Error devolviendo conexión al pool: " + e.getMessage());
            }
        }
    }

    /**
     * Wrapper para conexiones del pool que las devuelve automáticamente al cerrar
     */
    private static class PooledConnection implements Connection {
        private final Connection actualConnection;
        private boolean closed = false;

        public PooledConnection(Connection actualConnection) {
            this.actualConnection = actualConnection;
        }

        @Override
        public void close() throws SQLException {
            if (!closed) {
                closed = true;
                returnConnection(actualConnection);
            }
        }

        // Delegar todos los métodos a la conexión real
        @Override
        public java.sql.Statement createStatement() throws SQLException {
            return actualConnection.createStatement();
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String sql) throws SQLException {
            return actualConnection.prepareStatement(sql);
        }

        @Override
        public java.sql.CallableStatement prepareCall(String sql) throws SQLException {
            return actualConnection.prepareCall(sql);
        }

        @Override
        public String nativeSQL(String sql) throws SQLException {
            return actualConnection.nativeSQL(sql);
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException {
            actualConnection.setAutoCommit(autoCommit);
        }

        @Override
        public boolean getAutoCommit() throws SQLException {
            return actualConnection.getAutoCommit();
        }

        @Override
        public void commit() throws SQLException {
            actualConnection.commit();
        }

        @Override
        public void rollback() throws SQLException {
            actualConnection.rollback();
        }

        @Override
        public boolean isClosed() throws SQLException {
            return closed || actualConnection.isClosed();
        }

        @Override
        public java.sql.DatabaseMetaData getMetaData() throws SQLException {
            return actualConnection.getMetaData();
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException {
            actualConnection.setReadOnly(readOnly);
        }

        @Override
        public boolean isReadOnly() throws SQLException {
            return actualConnection.isReadOnly();
        }

        @Override
        public void setCatalog(String catalog) throws SQLException {
            actualConnection.setCatalog(catalog);
        }

        @Override
        public String getCatalog() throws SQLException {
            return actualConnection.getCatalog();
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException {
            actualConnection.setTransactionIsolation(level);
        }

        @Override
        public int getTransactionIsolation() throws SQLException {
            return actualConnection.getTransactionIsolation();
        }

        @Override
        public java.sql.SQLWarning getWarnings() throws SQLException {
            return actualConnection.getWarnings();
        }

        @Override
        public void clearWarnings() throws SQLException {
            actualConnection.clearWarnings();
        }

        @Override
        public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            return actualConnection.createStatement(resultSetType, resultSetConcurrency);
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return actualConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            return actualConnection.prepareCall(sql, resultSetType, resultSetConcurrency);
        }

        @Override
        public java.util.Map<String, Class<?>> getTypeMap() throws SQLException {
            return actualConnection.getTypeMap();
        }

        @Override
        public void setTypeMap(java.util.Map<String, Class<?>> map) throws SQLException {
            actualConnection.setTypeMap(map);
        }

        @Override
        public void setHoldability(int holdability) throws SQLException {
            actualConnection.setHoldability(holdability);
        }

        @Override
        public int getHoldability() throws SQLException {
            return actualConnection.getHoldability();
        }

        @Override
        public java.sql.Savepoint setSavepoint() throws SQLException {
            return actualConnection.setSavepoint();
        }

        @Override
        public java.sql.Savepoint setSavepoint(String name) throws SQLException {
            return actualConnection.setSavepoint(name);
        }

        @Override
        public void rollback(java.sql.Savepoint savepoint) throws SQLException {
            actualConnection.rollback(savepoint);
        }

        @Override
        public void releaseSavepoint(java.sql.Savepoint savepoint) throws SQLException {
            actualConnection.releaseSavepoint(savepoint);
        }

        @Override
        public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return actualConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return actualConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return actualConnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            return actualConnection.prepareStatement(sql, autoGeneratedKeys);
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            return actualConnection.prepareStatement(sql, columnIndexes);
        }

        @Override
        public java.sql.PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            return actualConnection.prepareStatement(sql, columnNames);
        }

        @Override
        public java.sql.Clob createClob() throws SQLException {
            return actualConnection.createClob();
        }

        @Override
        public java.sql.Blob createBlob() throws SQLException {
            return actualConnection.createBlob();
        }

        @Override
        public java.sql.NClob createNClob() throws SQLException {
            return actualConnection.createNClob();
        }

        @Override
        public java.sql.SQLXML createSQLXML() throws SQLException {
            return actualConnection.createSQLXML();
        }

        @Override
        public boolean isValid(int timeout) throws SQLException {
            return actualConnection.isValid(timeout);
        }

        @Override
        public void setClientInfo(String name, String value) throws java.sql.SQLClientInfoException {
            actualConnection.setClientInfo(name, value);
        }

        @Override
        public void setClientInfo(Properties properties) throws java.sql.SQLClientInfoException {
            actualConnection.setClientInfo(properties);
        }

        @Override
        public String getClientInfo(String name) throws SQLException {
            return actualConnection.getClientInfo(name);
        }

        @Override
        public Properties getClientInfo() throws SQLException {
            return actualConnection.getClientInfo();
        }

        @Override
        public java.sql.Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            return actualConnection.createArrayOf(typeName, elements);
        }

        @Override
        public java.sql.Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            return actualConnection.createStruct(typeName, attributes);
        }

        @Override
        public void setSchema(String schema) throws SQLException {
            actualConnection.setSchema(schema);
        }

        @Override
        public String getSchema() throws SQLException {
            return actualConnection.getSchema();
        }

        @Override
        public void abort(java.util.concurrent.Executor executor) throws SQLException {
            actualConnection.abort(executor);
        }

        @Override
        public void setNetworkTimeout(java.util.concurrent.Executor executor, int milliseconds) throws SQLException {
            actualConnection.setNetworkTimeout(executor, milliseconds);
        }

        @Override
        public int getNetworkTimeout() throws SQLException {
            return actualConnection.getNetworkTimeout();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return actualConnection.unwrap(iface);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return actualConnection.isWrapperFor(iface);
        }
    }

    /**
     * Cierra todas las conexiones del pool
     */
    public static void closePool() {
        Connection conn;
        while ((conn = connectionPool.poll()) != null) {
            try {
                if (conn instanceof PooledConnection) {
                    // Obtener la conexión real
                    conn = ((PooledConnection) conn).actualConnection;
                }
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error cerrando conexión del pool: " + e.getMessage());
            }
        }
        poolInitialized = false;
        System.out.println("🔒 Pool de conexiones cerrado");
    }
}
