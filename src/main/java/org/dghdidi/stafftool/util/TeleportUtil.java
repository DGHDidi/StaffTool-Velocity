package org.dghdidi.stafftool.util;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.dghdidi.stafftool.StaffTool;

import java.util.concurrent.TimeUnit;

public class TeleportUtil {
    public static int teleportDelayMs = 2000;
    public static String teleportCommand = "tp %target%";

    public static void teleportToPlayer(Player staff, Player target) {
        String command = formatTeleportCommand(target.getUsername());
        RegisteredServer targetServer = target.getCurrentServer().map(ServerConnection::getServer).orElse(null);
        if (targetServer == null) {
            PlayerUtil.sendMessage(staff, "§c目标玩家当前不在任何服务器");
            return;
        }

        if (staff.getCurrentServer().map(connection -> connection.getServer().equals(targetServer)).orElse(false)) {
            runBackendCommand(staff, command);
            return;
        }

        String serverName = targetServer.getServerInfo().getName();
        PlayerUtil.sendMessage(staff, "§7正在连接到服务器 " + serverName + "...");
        staff.createConnectionRequest(targetServer).connect().thenAccept(result -> {
            if (!result.isSuccessful()) {
                PlayerUtil.sendMessage(staff, "§c连接到服务器 " + serverName + " 失败，无法传送到目标玩家");
                return;
            }
            StaffTool.proxy.getScheduler().buildTask(StaffTool.plugin, () -> runBackendCommand(staff, command))
                    .delay(teleportDelayMs, TimeUnit.MILLISECONDS)
                    .schedule();
        });
    }

    public static String targetServerName(Player target) {
        return target.getCurrentServer()
                .map(connection -> connection.getServerInfo().getName())
                .orElse("Unknown");
    }

    private static String formatTeleportCommand(String targetName) {
        return teleportCommand.replace("%target%", targetName).replace("{target}", targetName);
    }

    private static void runBackendCommand(Player player, String command) {
        String normalized = command.startsWith("/") ? command : "/" + command;
        player.spoofChatInput(normalized);
    }
}
