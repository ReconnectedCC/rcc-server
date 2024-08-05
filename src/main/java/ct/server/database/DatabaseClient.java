package ct.server.database;

import ct.server.CtServer;

import java.sql.*;

public class DatabaseClient {
    private Connection connection;
    public Connection connection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(CtServer.CONFIG.databaseUrl());
        }
        return connection;
    }

    // Prepare a connection to the DB ready for use
    // TODO: Consider creating a pool
    public DatabaseClient() throws SQLException {
        connection();
    }
}
