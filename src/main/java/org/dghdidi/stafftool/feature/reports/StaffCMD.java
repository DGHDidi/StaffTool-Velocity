package org.dghdidi.stafftool.feature.reports;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import org.dghdidi.stafftool.StaffTool;
import org.dghdidi.stafftool.database.table.ReportsTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import static org.dghdidi.stafftool.StaffTool.databaseManager;
import static org.dghdidi.stafftool.feature.reports.PlayerCMD.reportPlayerPrefix;
import static org.dghdidi.stafftool.feature.reports.ReportPunishment.addPunish;
import static org.dghdidi.stafftool.feature.reports.ReportPunishment.convertToMilliseconds;
import static org.dghdidi.stafftool.feature.reports.ReportPunishment.isUnderPunish;
import static org.dghdidi.stafftool.feature.reports.ReportPunishment.removePunish;
import static org.dghdidi.stafftool.feature.reports.ReportsStorage.clear;
import static org.dghdidi.stafftool.feature.reports.ReportsStorage.contains;
import static org.dghdidi.stafftool.feature.reports.ReportsStorage.del;
import static org.dghdidi.stafftool.feature.reports.ReportsStorage.showReports;
import static org.dghdidi.stafftool.util.PlayerUtil.getDisplayName;
import static org.dghdidi.stafftool.util.PlayerUtil.getPlayers;
import static org.dghdidi.stafftool.util.PlayerUtil.sendMessage;
import static org.dghdidi.stafftool.util.PlayerUtil.tabComplete;
import static org.dghdidi.stafftool.util.TeleportUtil.targetServerName;
import static org.dghdidi.stafftool.util.TeleportUtil.teleportToPlayer;

public class StaffCMD implements SimpleCommand {
    public static int delay;
    public static String reportStaffPrefix;

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player sender)) {
            sendMessage(invocation.source(), reportPlayerPrefix + "§c只有玩家才能使用此命令!");
            return;
        }
        String[] args = invocation.arguments();
        if (args.length == 0) {
            showAllReports(sender);
        } else if (Objects.equals(args[0].toLowerCase(), "accept")) {
            acceptReport(sender, args);
        } else if (Objects.equals(args[0].toLowerCase(), "complete")) {
            completeReport(sender, args);
        } else if (Objects.equals(args[0].toLowerCase(), "delete")) {
            deleteReport(sender, args);
        } else if (Objects.equals(args[0].toLowerCase(), "tpto")) {
            teleportTo(sender, args);
        } else if (Objects.equals(args[0].toLowerCase(), "punish")) {
            punish(sender, args);
        } else if (Objects.equals(args[0].toLowerCase(), "cancelpunish")) {
            cancelReportPunish(sender, args);
        } else if (Objects.equals(args[0].toLowerCase(), "stats")) {
            getStats(sender, args);
        } else if (Objects.equals(args[0].toLowerCase(), "help")) {
            getHelp(sender);
        } else if (Objects.equals(args[0].toLowerCase(), "clear")) {
            clearReports(sender, args);
        } else {
            sendMessage(sender, "§c未知命令. 请输入 /reports help 查看可用命令!");
        }
    }

    private void showAllReports(Player sender) {
        if (ReportsStorage.getNum() == 0) {
            sendMessage(sender, reportPlayerPrefix + "§c当前没有未处理的举报!");
            return;
        }
        try {
            showReports(sender);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void acceptReport(Player sender, String[] args) {
        if (!check(sender, "reports.staff.accept")) return;
        if (args.length != 2) {
            sendMessage(sender, "§c用法: /reports accept <编号>");
            return;
        }
        Integer index = parseIndex(sender, args[1], "/reports accept 233");
        if (index == null) return;
        try {
            List<String> result = ReportsTable.getReportInfo(index);
            if (result == null || !contains(index)) {
                sendMessage(sender, "§c该举报不存在!");
                return;
            }
            if (!Objects.equals(result.get(2), "null")) {
                sendMessage(sender, "§c该举报已被受理!");
                return;
            }
            try (Connection connection = databaseManager.getConnection();
                 PreparedStatement statement = connection.prepareStatement("UPDATE report_info SET staff_name = ? WHERE id = ?")) {
                statement.setString(1, sender.getUsername());
                statement.setInt(2, index);
                statement.executeUpdate();
            }
            staffBC("§7" + getDisplayName(sender) + " §e受理了举报 §7#" + index);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void completeReport(Player sender, String[] args) {
        if (!check(sender, "reports.staff.complete")) return;
        if (args.length != 2) {
            sendMessage(sender, "§c用法: /reports complete <编号>");
            return;
        }
        Integer index = parseIndex(sender, args[1], "/reports complete 233");
        if (index == null) return;
        try {
            List<String> result = ReportsTable.getReportInfo(index);
            if (result == null || !contains(index)) {
                sendMessage(sender, "§c该举报不存在!");
                return;
            }
            String staffName = result.get(2), playerName = result.get(0), reportedName = result.get(1);
            if (!Objects.equals(staffName, sender.getUsername())) {
                sendMessage(sender, "§c请处理自己受理的举报!");
                return;
            }
            del(index, playerName, reportedName);
            staffBC("§7" + getDisplayName(sender) + " §a处理了举报 §7#" + index);
            StaffTool.proxy.getPlayer(playerName).ifPresent(player ->
                    sendMessage(player, reportPlayerPrefix + "§a§l您的举报已被工作人员完成处理，感谢您对游戏环境的维护!"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteReport(Player sender, String[] args) {
        if (!check(sender, "reports.staff.delete")) return;
        if (args.length != 2) {
            sendMessage(sender, "§c用法: /reports delete <编号>");
            return;
        }
        Integer index = parseIndex(sender, args[1], "/reports delete 233");
        if (index == null) return;
        try {
            List<String> result = ReportsTable.getReportInfo(index);
            if (result == null || !contains(index)) {
                sendMessage(sender, "§c该举报不存在!");
                return;
            }
            del(index, result.get(0), result.get(1));
            staffBC("§7" + getDisplayName(sender) + " §c删除了举报 §7#" + index);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void teleportTo(Player sender, String[] args) {
        if (!check(sender, "reports.staff.tpto")) return;
        if (args.length != 2) {
            sendMessage(sender, "§c用法: /reports tpto <ID>");
            return;
        }
        String targetID = args[1];
        Player targetPlayer = StaffTool.proxy.getPlayer(targetID).orElse(null);
        if (targetPlayer == null) {
            sendMessage(sender, "§c该名玩家不在线或不存在!");
            return;
        }
        String serverName = targetServerName(targetPlayer);
        staffBC(getDisplayName(sender) + " §a正在前往服务器 §7" + serverName + " §a处理对于玩家 §7" + getDisplayName(targetID) + " §a的举报");
        teleportToPlayer(sender, targetPlayer);
    }

    private void punish(Player sender, String[] args) {
        if (!check(sender, "reports.staff.punish")) return;
        if (args.length != 3) {
            sendMessage(sender, "§c用法: /reports punish <ID> <时长>");
            return;
        }
        Player targetPlayer = StaffTool.proxy.getPlayer(args[1]).orElse(null);
        if (targetPlayer == null) {
            sendMessage(sender, "§c该名玩家不在线或不存在!");
            return;
        }
        staffBC(getDisplayName(sender) + "§c 因为违规举报惩罚了 " + getDisplayName(targetPlayer));
        addPunish(targetPlayer, convertToMilliseconds(sender, args[2]));
    }

    private void cancelReportPunish(Player sender, String[] args) {
        if (!check(sender, "reports.staff.punish")) return;
        if (args.length != 2) {
            sendMessage(sender, "§c用法: /reports CancelPunish <ID>");
            return;
        }
        Player targetPlayer = StaffTool.proxy.getPlayer(args[1]).orElse(null);
        if (targetPlayer == null) {
            sendMessage(sender, "§c该名玩家不在线或不存在!");
            return;
        }
        if (!isUnderPunish(targetPlayer)) {
            sendMessage(sender, "§c该名玩家没有被处罚!");
            return;
        }
        staffBC(getDisplayName(sender) + "§a 取消了对 " + getDisplayName(targetPlayer) + " §a的违规举报处罚");
        removePunish(targetPlayer);
    }

    private void getStats(Player sender, String[] args) {
        if (!check(sender, "reports.admin.stats")) return;
        if (args.length != 2) {
            sendMessage(sender, "§c用法: /reports stats <ID>");
            return;
        }
        int count = 0;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) AS count FROM report_info WHERE staff_name = ?")) {
            statement.setString(1, args[1]);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    count = resultSet.getInt("count");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        sendMessage(sender, reportPlayerPrefix + "§a工作人员 §b" + args[1] + " §a至现在已处理 §e" + count + " §a个举报.");
    }

    private void clearReports(Player sender, String[] args) {
        if (!check(sender, "reports.admin.clear")) return;
        if (args.length == 2 && Objects.equals(args[1], "confirm")) {
            int rowsAffected = databaseManager.executeCommand("DELETE FROM report_info");
            clear();
            staffBC(getDisplayName(sender) + " §c§l清除了所有举报记录");
            sendMessage(sender, reportPlayerPrefix + "§a§l清除成功 §a共清除了 §e" + rowsAffected + " §a条记录.");
        } else {
            sendMessage(sender, reportPlayerPrefix + "§c您确定要清除所有举报吗? 确认请输入 §b/reports clear confirm");
        }
    }

    private void getHelp(Player sender) {
        sendMessage(sender, "§8--------------§e§lReports§8--------------");
        sendMessage(sender, "§a/report <ID> <原因> §7举报某位玩家 §8(reports.player)");
        sendMessage(sender, "§a/reports §7显示当前未处理的举报 §8(reports.staff)");
        sendMessage(sender, "§a/reports accept <编号> §7受理某举报 §8(reports.staff.accept)");
        sendMessage(sender, "§a/reports complete <编号> §7处理某举报 §8(reports.staff.complete)");
        sendMessage(sender, "§a/reports delete <编号> §7删除某举报 §8(reports.staff.delete)");
        sendMessage(sender, "§a/reports tpto <ID> §7跨服传送到某玩家 §8(reports.staff.tpto)");
        sendMessage(sender, "§a/reports punish <ID> <时长> §7惩罚某玩家不得举报 §8(reports.staff.punish)");
        sendMessage(sender, "§a/reports cancelpunish <ID> §7解除对某玩家的惩罚 §8(reports.staff.punish)");
        sendMessage(sender, "§a/reports stats <ID> §7查看某工作人员的举报处理情况 §8(reports.admin.stats)");
        sendMessage(sender, "§a/reports clear §7删除所有举报 §8(reports.admin.clear)");
        sendMessage(sender, "§8-----------------------------------");
    }

    private boolean check(Player sender, String permission) {
        if (!sender.hasPermission(permission)) {
            sendMessage(sender, "§c你没有权限使用此命令!");
            return false;
        }
        return true;
    }

    private Integer parseIndex(Player sender, String text, String example) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            sendMessage(sender, "§c数字格式不符! 例: " + example);
            return null;
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 2 && (args[0].equalsIgnoreCase("tpto") || args[0].equalsIgnoreCase("punish") || args[0].equalsIgnoreCase("cancelpunish") || args[0].equalsIgnoreCase("stats"))) {
            return tabComplete(new String[]{args[1]});
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("reports.staff");
    }

    private static void staffBC(String msg) {
        for (Player staff : getPlayers("reports.staff")) {
            sendMessage(staff, reportStaffPrefix + msg);
        }
    }
}
