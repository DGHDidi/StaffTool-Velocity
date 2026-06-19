package org.dghdidi.stafftool.feature.reports;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import org.dghdidi.stafftool.StaffTool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.dghdidi.stafftool.StaffTool.databaseManager;
import static org.dghdidi.stafftool.feature.reports.ReportPunishment.getRemainTime;
import static org.dghdidi.stafftool.feature.reports.ReportPunishment.isUnderPunish;
import static org.dghdidi.stafftool.util.PlayerUtil.getDisplayName;
import static org.dghdidi.stafftool.util.PlayerUtil.getPlayers;
import static org.dghdidi.stafftool.util.PlayerUtil.sendMessage;
import static org.dghdidi.stafftool.util.PlayerUtil.tabComplete;

public class PlayerCMD implements SimpleCommand {
    public static String reportPlayerPrefix;

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            sendMessage(invocation.source(), "§c只有玩家才能使用此命令");
            return;
        }

        String[] args = invocation.arguments();
        if (args.length < 2) {
            sendMessage(player, "§c用法: /report <ID> <原因>");
            return;
        }

        String reportedName = args[0];
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String playerName = player.getUsername();
        Player reportedPlayer = StaffTool.proxy.getPlayer(reportedName).orElse(null);

        if (isUnderPunish(player)) {
            sendMessage(player, reportPlayerPrefix + "§c§l举报失败 §7(您因为违规举报被工作人员处罚，在 " + getRemainTime(player) + "§7 后才能进行举报)");
            return;
        }
        if (Objects.equals(reportedName, playerName)) {
            sendMessage(player, reportPlayerPrefix + "§c您不能举报你自己!");
            return;
        }
        if (reportedPlayer == null) {
            sendMessage(player, reportPlayerPrefix + "§c该名玩家不存在或离线!");
            return;
        }
        if (reportedPlayer.hasPermission("reports.bypass")) {
            sendMessage(player, reportPlayerPrefix + "§c你不能举报这名玩家!");
            return;
        }
        if (ReportsStorage.containsPair(playerName, reportedName)) {
            sendMessage(player, reportPlayerPrefix + "§c您已举报过玩家 " + getDisplayName(reportedName) + "§c，请勿重复举报!");
            return;
        }

        String serverName = reportedPlayer.getCurrentServer()
                .map(connection -> connection.getServerInfo().getName())
                .orElse("Unknown");
        try {
            String sql = "INSERT INTO report_info (player_name, reported_name, staff_name, server_name, reason) VALUES (?, ?, ?, ?, ?)";
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, playerName);
                statement.setString(2, reportedName);
                statement.setString(3, "null");
                statement.setString(4, serverName);
                statement.setString(5, reason);
                statement.executeUpdate();

                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        int nowId = keys.getInt(1);
                        ReportsStorage.add(nowId, playerName, reportedName);
                        for (Player staff : getPlayers("reports.staff")) {
                            if (!ReportsStorage.showReport(staff, nowId, false)) {
                                sendMessage(staff, "§c该举报不存在!");
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            sendMessage(player, "§c数据库错误，请联系管理员查看服务器后台");
            throw new RuntimeException(e);
        }

        sendMessage(player, reportPlayerPrefix + "§a举报成功，请耐心等待处理!");
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return invocation.arguments().length == 1 ? tabComplete(invocation.arguments()) : List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("reports.player");
    }
}
