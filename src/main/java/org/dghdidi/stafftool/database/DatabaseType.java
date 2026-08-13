package org.dghdidi.stafftool.database;

import java.util.Locale;

public enum DatabaseType {
    MYSQL("com.mysql.cj.jdbc.Driver", 3306),
    POSTGRESQL("org.postgresql.Driver", 5432);

    private final String driverClassName;
    private final int defaultPort;

    DatabaseType(String driverClassName, int defaultPort) {
        this.driverClassName = driverClassName;
        this.defaultPort = defaultPort;
    }

    public String driverClassName() {
        return driverClassName;
    }

    public int defaultPort() {
        return defaultPort;
    }

    public static DatabaseType fromConfig(String value) {
        String normalized = value == null ? "mysql" : value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "mysql", "mariadb" -> MYSQL;
            case "postgres", "postgresql" -> POSTGRESQL;
            default -> throw new IllegalArgumentException("不支持的数据库类型: " + value + "，可用值: mysql, postgresql");
        };
    }
}
