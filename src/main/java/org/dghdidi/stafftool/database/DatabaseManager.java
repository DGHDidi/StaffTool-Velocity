package org.dghdidi.stafftool.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.dghdidi.stafftool.StaffTool;
import org.dghdidi.stafftool.database.table.ChatLogTable;
import org.dghdidi.stafftool.database.table.ReportsTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.dghdidi.stafftool.config.LoadConfig.enableChatHistory;
import static org.dghdidi.stafftool.config.LoadConfig.enableReports;

public class DatabaseManager {
    private HikariDataSource dataSource;
    public static DatabaseType type = DatabaseType.MYSQL;
    public static String host;
    public static int port;
    public static String database;
    public static String username;
    public static String password;
    public static String parameters;
    public static int maximumPoolSize;
    public static int minimumIdle;
    public static long connectionTimeoutMs;

    public void init() {
        HikariDataSource newDataSource = createDataSource();
        try (Connection ignored = newDataSource.getConnection()) {
            StaffTool.logger.info("§a数据库连接成功 (" + type.name().toLowerCase() + ")");
            this.dataSource = newDataSource;
            createTables();
        } catch (SQLException e) {
            newDataSource.close();
            StaffTool.logger.warning("§c数据库连接失败，请检查配置文件");
            throw new RuntimeException(e);
        }
    }

    public void reload() {
        HikariDataSource old = this.dataSource;
        HikariDataSource newDataSource = createDataSource();

        try (Connection ignored = newDataSource.getConnection()) {
            StaffTool.logger.info("§a数据库连接成功 (" + type.name().toLowerCase() + ")");
        } catch (SQLException e) {
            newDataSource.close();
            StaffTool.logger.warning("§c数据库连接失败，保留旧数据库连接");
            throw new RuntimeException(e);
        }

        this.dataSource = newDataSource;
        if (old != null && !old.isClosed()) {
            old.close();
        }
        createTables();
    }

    private static HikariDataSource createDataSource() {
        try {
            Class.forName(type.driverClassName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(type + " JDBC driver not found in plugin jar", e);
        }
        return new HikariDataSource(getHikariConfig());
    }

    private static HikariConfig getHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(type.driverClassName());
        config.setJdbcUrl(jdbcUrl());
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setConnectionTimeout(connectionTimeoutMs);
        return config;
    }

    private static String jdbcUrl() {
        String scheme = type == DatabaseType.POSTGRESQL ? "postgresql" : "mysql";
        String suffix = parameters == null ? "" : parameters.trim();
        if (!suffix.isEmpty() && !suffix.startsWith("?")) {
            suffix = "?" + suffix;
        }
        return "jdbc:" + scheme + "://" + host + ":" + port + "/" + database + suffix;
    }

    public static String identityColumnDefinition() {
        return type == DatabaseType.POSTGRESQL
                ? "BIGSERIAL PRIMARY KEY"
                : "BIGINT PRIMARY KEY AUTO_INCREMENT";
    }

    public static String chatLogRetentionSql(int retentionDays) {
        if (type == DatabaseType.POSTGRESQL) {
            return "DELETE FROM chat_log WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '" + retentionDays + " days'";
        }
        return "DELETE FROM chat_log WHERE created_at < CURRENT_TIMESTAMP - INTERVAL " + retentionDays + " DAY";
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Database is not initialized");
        }
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public void executeUpdate(String sql) {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
            StaffTool.logger.info("§a执行 SQL " + sql);
        } catch (SQLException e) {
            StaffTool.logger.severe("§c执行 SQL 失败:");
            StaffTool.logger.severe(sql);
            e.printStackTrace();
        }
    }

    public void createIndex(String sql) {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            if (e.getErrorCode() != 1061 && !"42P07".equals(e.getSQLState())) {
                StaffTool.logger.severe("创建索引失败");
                e.printStackTrace();
            }
        }
    }

    public int executeCommand(String sql) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ResultSet executeQuery(String sql) {
        try {
            Statement statement = getConnection().createStatement();
            return statement.executeQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTables() {
        if (enableChatHistory) {
            ChatLogTable.createTable();
        }
        if (enableReports) {
            ReportsTable.createTable();
        }
    }
}
