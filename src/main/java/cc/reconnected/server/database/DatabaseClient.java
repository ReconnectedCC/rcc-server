package cc.reconnected.server.database;

import cc.reconnected.server.RccServer;

import java.sql.*;

public class DatabaseClient {
    private Connection connection;
    public Connection connection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(RccServer.CONFIG.databaseUrl());
        }
        return connection;
    }
}
