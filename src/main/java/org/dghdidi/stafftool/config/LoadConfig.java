package org.dghdidi.stafftool.config;

import org.dghdidi.stafftool.StaffTool;
import org.dghdidi.stafftool.database.DatabaseManager;
import org.dghdidi.stafftool.feature.ChatHistory;
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

        DatabaseManager.mysqlHost = string(config, "MySQL", "host", "localhost");
        DatabaseManager.mysqlDatabase = string(config, "MySQL", "database", "Minecraft");
        DatabaseManager.mysqlPort = integer(config, "MySQL", "port", 3306);
        DatabaseManager.mysqlUsername = string(config, "MySQL", "username", "root");
        DatabaseManager.mysqlPassword = string(config, "MySQL", "password", "");
        DatabaseManager.mysqlParameters = string(config, "MySQL", "parameters", "?useSSL=false&characterEncoding=utf8");
        DatabaseManager.maximumPoolSize = integer(config, "MySQL", "maximumPoolSize", 10);
        DatabaseManager.minimumIdle = integer(config, "MySQL", "minimumIdle", 2);
        DatabaseManager.connectionTimeoutMs = integer(config, "MySQL", "connectionTimeout(ms)", 10000);

        TeleportUtil.teleportDelayMs = integer(config, "Style", "teleportDelay(ms)", 2000);
        TeleportUtil.teleportCommand = string(config, "Style", "teleportCommand", "tp %target%");
        StaffCMD.delay = TeleportUtil.teleportDelayMs;
        PlayerCMD.reportPlayerPrefix = string(config, "Style", "reportPlayerPrefix", "§8[§6举报系统§8] §f");
        StaffCMD.reportStaffPrefix = string(config, "Style", "reportStaffPrefix", "§b[员工] §2[举报] §f");
        AutoMessage.playerPrefix = string(config, "Style", "playerPrefix", "§cInf Staff §8» §f");
        AdminChat.adminPrefix = string(config, "Style", "adminPrefix", "§c[管理] ");
        LoginLogoutListener.staffPrefix = string(config, "Style", "staffPrefix", "§b[员工] ");

        enableStaffChat = bool(config, "Function", "staffChat", true);
        enableReports = bool(config, "Function", "reports", true);
        enableChatHistory = bool(config, "Function", "chatHistory", true);

        CommandRegistrar.unregisterAll();
        registerCommand();
    }

    private static void registerCommand() {
        CommandRegistrar.register("stafftool", new Reload());
        CommandRegistrar.register("mp", new PunishAlert());
        CommandRegistrar.register("punish", new Punish());
        CommandRegistrar.register("unamc", new CancelMessage());
        CommandRegistrar.register("amc", new AutoMessage());
        CommandRegistrar.register("staffs", new OnlineStaff());
        CommandRegistrar.register("tpto", new Teleport());
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
}
