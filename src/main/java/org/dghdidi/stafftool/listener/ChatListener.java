package org.dghdidi.stafftool.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import org.dghdidi.stafftool.StaffTool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.dghdidi.stafftool.StaffTool.databaseManager;
import static org.dghdidi.stafftool.StaffTool.logger;
import static org.dghdidi.stafftool.config.LoadConfig.enableChatHistory;

public class ChatListener {
    @Subscribe
    public void onChat(PlayerChatEvent event) {
        if (!enableChatHistory) {
            return;
        }
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (message.startsWith("/") || message.isBlank()) {
            return;
        }

        String playerName = player.getUsername();
        String serverName = player.getCurrentServer()
                .map(connection -> connection.getServerInfo().getName())
                .orElse("未知");

        StaffTool.proxy.getScheduler().buildTask(StaffTool.plugin, () -> {
            String sql = """
                    INSERT INTO chat_log (player_name, message, server_name)
                    VALUES (?, ?, ?)
                    """;

            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, playerName);
                ps.setString(2, message);
                ps.setString(3, serverName);
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.warning("§c聊天记录写入失败: " + playerName);
                e.printStackTrace();
            }
        }).schedule();
    }
}
