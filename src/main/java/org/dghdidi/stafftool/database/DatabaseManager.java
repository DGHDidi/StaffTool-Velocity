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
    private static final String MYSQL_DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    private HikariDataSource dataSource;
    public static String mysqlHost;
    public static int mysqlPort;
    public static String mysqlDatabase;
    public static String mysqlUsername;
    public static String mysqlPassword;
    public static String mysqlParameters;
    public static int maximumPoolSize;
    public static int minimumIdle;
    public static long connectionTimeoutMs;

    public void init() {
        HikariDataSource newDataSource = createDataSource();
        try (Connection ignored = newDataSource.getConnection()) {
            StaffTool.logger.info("§a数据库连接成功");
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
            StaffTool.logger.info("§a数据库连接成功");
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
            Class.forName(MYSQL_DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver not found in plugin jar", e);
        }
        return new HikariDataSource(getHikariConfig());
    }

    private static HikariConfig getHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(MYSQL_DRIVER_CLASS);
        config.setJdbcUrl("jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlDatabase + mysqlParameters);
        config.setUsername(mysqlUsername);
        config.setPassword(mysqlPassword);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setConnectionTimeout(connectionTimeoutMs);
        return config;
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
            if (e.getErrorCode() != 1061) {
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
