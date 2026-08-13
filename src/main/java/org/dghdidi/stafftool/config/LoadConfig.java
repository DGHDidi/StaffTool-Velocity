package org.dghdidi.stafftool.config;

import org.dghdidi.stafftool.StaffTool;
import org.dghdidi.stafftool.database.DatabaseManager;
import org.dghdidi.stafftool.database.DatabaseType;
import org.dghdidi.stafftool.feature.ChatHistory;
import org.dghdidi.stafftool.feature.FindPlayer;
import org.dghdidi.stafftool.feature.OnlineStaff;
import org.dghdidi.stafftool.feature.Punish;
import org.dghdidi.stafftool.feature.PunishAlert;
import org.dghdidi.stafftool.feature.Reload;
import org.dghdidi.stafftool.feature.Teleport;
import org.dghdidi.stafftool.feature.chat.AdminChat;
import org.dghdidi.stafftool.feature.chat.StaffChat;
import org.dghdidi.stafftool.feature.clientchecker.AutoMessage;
import org.dghdidi.stafftool.feature.clientchecker.CancelMessage;
import org.dghdidi.stafftool.feature.reports.PlayerCMD;
import org.dghdidi.stafftool.feature.reports.StaffCMD;
import org.dghdidi.stafftool.listener.LoginLogoutListener;
import org.dghdidi.stafftool.util.CommandRegistrar;
import org.dghdidi.stafftool.util.TeleportUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;

import static org.dghdidi.stafftool.StaffTool.plugin;

public class LoadConfig {
    private static boolean enableStaffChat;
    public static boolean enableReports;
    public static boolean enableChatHistory;
    public static int chatHistoryRetentionDays;
    private static boolean enableClientChecker;
    private static boolean enablePunish;
    private static boolean enableOnlineStaff;
    private static boolean enableTeleport;
    private static boolean enableFindPlayer;

    public static void loadConfig() throws IOException {
        Path configFile = plugin.getDataDirectory().resolve("config.yml");
        if (!Files.exists(configFile)) {
            StaffTool.logger.log(Level.WARNING, "§e配置文件不存在，已创建默认配置文件，请自行配置参数");
            CreateConfig.createDefaultConfig(plugin);
            return;
        }

        Map<String, Object> config;
        try (InputStream inputStream = Files.newInputStream(configFile)) {
            config = new Yaml().load(inputStream);
        }
        if (config == null) {
            config = Map.of();
        }

        boolean legacyMySqlConfig = section(config, "Database").isEmpty() && !section(config, "MySQL").isEmpty();
        String databaseSection = legacyMySqlConfig ? "MySQL" : "Database";
        DatabaseManager.type = legacyMySqlConfig
                ? DatabaseType.MYSQL
                : DatabaseType.fromConfig(string(config, databaseSection, "type", "mysql"));
        DatabaseManager.host = string(config, databaseSection, "host", "localhost");
        DatabaseManager.database = string(config, databaseSection, "database", "Minecraft");
        DatabaseManager.port = integer(config, databaseSection, "port", DatabaseManager.type.defaultPort());
        DatabaseManager.username = string(config, databaseSection, "username", "root");
        DatabaseManager.password = string(config, databaseSection, "password", "");
        String defaultParameters = DatabaseManager.type == DatabaseType.POSTGRESQL
                ? "?sslmode=disable"
                : "?useSSL=false&characterEncoding=utf8";
        DatabaseManager.parameters = string(config, databaseSection, "parameters", defaultParameters);
        DatabaseManager.maximumPoolSize = integer(config, databaseSection, "maximumPoolSize", 10);
        DatabaseManager.minimumIdle = integer(config, databaseSection, "minimumIdle", 2);
        DatabaseManager.connectionTimeoutMs = integer(config, databaseSection, "connectionTimeout(ms)", 10000);

        TeleportUtil.teleportDelayMs = integer(config, "Teleport", "teleportDelay(ms)",
                integer(config, "Style", "teleportDelay(ms)", 2000));
        TeleportUtil.teleportCommand = string(config, "Teleport", "teleportCommand",
                string(config, "Style", "teleportCommand", "tp %target%"));
        StaffCMD.delay = TeleportUtil.teleportDelayMs;
        PlayerCMD.reportPlayerPrefix = string(config, "Prefixes", "reportPlayerPrefix",
                string(config, "Style", "reportPlayerPrefix", "§8[§6举报系统§8] §f"));
        StaffCMD.reportStaffPrefix = string(config, "Prefixes", "reportStaffPrefix",
                string(config, "Style", "reportStaffPrefix", "§b[员工] §2[举报] §f"));
        AutoMessage.playerPrefix = string(config, "Prefixes", "playerPrefix",
                string(config, "Style", "playerPrefix", "§cInf Staff §8» §f"));
        AdminChat.adminPrefix = string(config, "Prefixes", "adminPrefix",
                string(config, "Style", "adminPrefix", "§c[管理] "));
        LoginLogoutListener.staffPrefix = string(config, "Prefixes", "staffPrefix",
                string(config, "Style", "staffPrefix", "§b[员工] "));

        enableStaffChat = feature(config, "staffChat", true);
        enableReports = feature(config, "reports", true);
        enableChatHistory = feature(config, "chatHistory", true);
        enableClientChecker = feature(config, "clientChecker", true);
        enablePunish = feature(config, "punish", true);
        enableOnlineStaff = feature(config, "onlineStaff", true);
        enableTeleport = feature(config, "teleport", true);
        enableFindPlayer = feature(config, "findPlayer", true);
        chatHistoryRetentionDays = positiveInteger(config, "ChatHistory", "retentionDays", 7);
        AutoMessage.messageCount = positiveInteger(config, "ClientChecker", "messageCount", 20);
        AutoMessage.intervalMs = positiveInteger(config, "ClientChecker", "interval(ms)", 15000);
        AutoMessage.titleStayTicks = positiveInteger(config, "ClientChecker", "titleStay(ticks)", 250);
        Punish.configure(section(config, "Punish"));

        CommandRegistrar.unregisterAll();
        registerCommand();
    }

    private static void registerCommand() {
        CommandRegistrar.register("stafftool", new Reload());
        CommandRegistrar.register("mp", new PunishAlert());
        if (enablePunish) {
            CommandRegistrar.register("punish", new Punish());
        }
        if (enableClientChecker) {
            CommandRegistrar.register("unamc", new CancelMessage());
            CommandRegistrar.register("amc", new AutoMessage());
        }
        if (enableOnlineStaff) {
            CommandRegistrar.register("staffs", new OnlineStaff());
        }
        if (enableTeleport) {
            CommandRegistrar.register("tpto", new Teleport());
        }
        if (enableFindPlayer) {
            CommandRegistrar.register("find", new FindPlayer());
        }
        if (enableStaffChat) {
            CommandRegistrar.register("sc", new StaffChat(), "staffchat");
            CommandRegistrar.register("ac", new AdminChat(), "adminchat");
        }
        if (enableChatHistory) {
            CommandRegistrar.register("chathistory", new ChatHistory());
        }
        if (enableReports) {
            CommandRegistrar.register("report", new PlayerCMD());
            CommandRegistrar.register("reports", new StaffCMD());
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> section(Map<String, Object> config, String key) {
        Object value = config.get(key);
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private static String string(Map<String, Object> config, String section, String key, String fallback) {
        Object value = section(config, section).get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private static int integer(Map<String, Object> config, String section, String key, int fallback) {
        Object value = section(config, section).get(key);
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private static boolean bool(Map<String, Object> config, String section, String key, boolean fallback) {
        Object value = section(config, section).get(key);
        if (value == null) {
            return fallback;
        }
        return value instanceof Boolean bool ? bool : Boolean.parseBoolean(String.valueOf(value));
    }

    private static boolean feature(Map<String, Object> config, String key, boolean fallback) {
        return bool(config, "Features", key, bool(config, "Function", key, fallback));
    }

    private static int positiveInteger(Map<String, Object> config, String section, String key, int fallback) {
        int value = integer(config, section, key, fallback);
        if (value <= 0) {
            throw new IllegalArgumentException(section + "." + key + " 必须大于 0");
        }
        return value;
    }
}
