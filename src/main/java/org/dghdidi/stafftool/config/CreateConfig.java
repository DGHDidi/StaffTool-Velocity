package org.dghdidi.stafftool.config;

import org.dghdidi.stafftool.StaffTool;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class CreateConfig {
    public static void createDefaultConfig(StaffTool plugin) {
        Path dataFolder = plugin.getDataDirectory();
        Path configFile = dataFolder.resolve("config.yml");
        if (Files.exists(configFile)) {
            return;
        }

        try {
            Files.createDirectories(dataFolder);
            try (InputStream inputStream = plugin.getClass().getClassLoader().getResourceAsStream("defaultConfig.yml")) {
                if (inputStream == null) {
                    StaffTool.logger.warning("§c默认配置 defaultConfig.yml 不存在");
                    return;
                }
                Files.copy(inputStream, configFile);
            }
        } catch (IOException e) {
            StaffTool.logger.warning("§c创建默认配置失败");
            e.printStackTrace();
        }
    }
}
