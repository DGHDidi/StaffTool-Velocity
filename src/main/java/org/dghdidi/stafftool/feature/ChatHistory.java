package org.dghdidi.stafftool.feature;

import com.velocitypowered.api.command.SimpleCommand;
import org.dghdidi.stafftool.StaffTool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;

import static org.dghdidi.stafftool.StaffTool.databaseManager;
import static org.dghdidi.stafftool.StaffTool.logger;
import static org.dghdidi.stafftool.util.PlayerUtil.sendMessage;
import static org.dghdidi.stafftool.util.PlayerUtil.tabComplete;

public class ChatHistory implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length != 2) {
            sendMessage(invocation.source(), "§c用法: /chathistory <ID> <页码>");
            return;
        }

        if (databaseManager == null) {
            sendMessage(invocation.source(), "§c数据库未初始化，请联系管理员检查配置");
            return;
        }

        String playerId = args[0];
        int page;
        try {
            page = Integer.parseInt(args[1]);
            if (page <= 0) {
                sendMessage(invocation.source(), "§c页码必须大于 0");
                return;
            }
        } catch (NumberFormatException e) {
            sendMessage(invocation.source(), "§c页码必须是数字");
            return;
        }

        final int pageSize = 15;
        final String countSql = "SELECT COUNT(*) AS record_count FROM chat_log WHERE player_name = ?";
        final String querySql = "SELECT player_name, message, server_name, created_at " +
                "FROM chat_log WHERE player_name = ? " +
                "ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?";

        try (Connection connection = databaseManager.getConnection()) {
            int total;
            try (PreparedStatement countStatement = connection.prepareStatement(countSql)) {
                countStatement.setString(1, playerId);
                try (ResultSet resultSet = countStatement.executeQuery()) {
                    total = resultSet.next() ? resultSet.getInt("record_count") : 0;
                }
            }

            if (total == 0) {
                sendMessage(invocation.source(), "§e没有找到玩家 §f" + playerId + " §e的聊天记录");
                return;
            }

            int pageLimit = (total + pageSize - 1) / pageSize;
            if (page > pageLimit) {
                sendMessage(invocation.source(), "§c页码超出范围，最大页码为 §f" + pageLimit);
                return;
            }

            sendMessage(invocation.source(), "§e" + playerId + "§a 的聊天记录 §7第 " + page + " 页/共 " + pageLimit + " 页");

            try (PreparedStatement queryStatement = connection.prepareStatement(querySql)) {
                queryStatement.setString(1, playerId);
                queryStatement.setInt(2, pageSize);
                queryStatement.setInt(3, (page - 1) * pageSize);

                try (ResultSet resultSet = queryStatement.executeQuery()) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss");
                    while (resultSet.next()) {
                        String message = resultSet.getString("message");
                        String serverName = resultSet.getString("server_name");
                        if (serverName == null || serverName.isBlank()) {
                            serverName = "Unknown";
                        }
                        Timestamp createdAt = resultSet.getTimestamp("created_at");
                        LocalDateTime localDateTime = createdAt.toLocalDateTime();
                        String timeText = localDateTime.format(formatter);

                        sendMessage(invocation.source(), "§7<" + timeText + " " + serverName + "> §f" + message);
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "抓取聊天记录失败", e);
            sendMessage(invocation.source(), "§c抓取聊天记录失败!");
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return invocation.arguments().length == 1 ? tabComplete(invocation.arguments()) : List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("staff.chathistory");
    }
}
