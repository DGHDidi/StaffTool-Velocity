package org.dghdidi.stafftool;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.dghdidi.stafftool.config.CreateConfig;
import org.dghdidi.stafftool.config.LoadConfig;
import org.dghdidi.stafftool.database.DatabaseManager;
import org.dghdidi.stafftool.database.task.ChatLogCleanTask;
import org.dghdidi.stafftool.feature.clientchecker.Service;
import org.dghdidi.stafftool.listener.ChatListener;
import org.dghdidi.stafftool.listener.LoginLogoutListener;
import org.dghdidi.stafftool.listener.ReportListener;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Plugin(
        id = "stafftool",
        name = "StaffTool",
        version = "1.0-SNAPSHOT",
        authors = {"DGH_Didi"}
)
public final class StaffTool {
    public static final Logger logger = Logger.getLogger("StaffTool");

    public static StaffTool plugin;
    public static ProxyServer proxy;
    public static LuckPerms luckPerms;
    public static DatabaseManager databaseManager;

    private final Path dataDirectory;

    @Inject
    public StaffTool(ProxyServer proxy, @DataDirectory Path dataDirectory) {
        StaffTool.proxy = proxy;
        StaffTool.plugin = this;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        CreateConfig.createDefaultConfig(this);

        try {
            luckPerms = LuckPermsProvider.get();
        } catch (IllegalStateException e) {
            logger.warning("§c§l无法获取 LuckPerms API,请确认 Velocity 已安装 LuckPerms。");
        }

        proxy.getEventManager().register(this, new LoginLogoutListener());
        proxy.getEventManager().register(this, new ChatListener());
        proxy.getEventManager().register(this, new ReportListener());

        try {
            LoadConfig.loadConfig();
        } catch (Exception e) {
            logger.warning("§c§l加载配置文件失败");
            e.printStackTrace();
        }

        try {
            databaseManager = new DatabaseManager();
            databaseManager.init();
        } catch (RuntimeException e) {
            logger.warning("§c§l数据库连接失败，请检查配置文件");
            e.printStackTrace();
        }

        proxy.getScheduler().buildTask(this, new ChatLogCleanTask()).delay(0, TimeUnit.HOURS).repeat(24, TimeUnit.HOURS).schedule();
        logger.info("§a§l插件已成功加载");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (databaseManager != null) {
            databaseManager.close();
        }
        Service.shutdown();
        logger.info("§a§l插件已成功卸载");
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }
}
