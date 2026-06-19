package org.dghdidi.stafftool.feature.reports;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.dghdidi.stafftool.StaffTool;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.dghdidi.stafftool.database.table.ReportsTable.getReportInfo;
import static org.dghdidi.stafftool.util.PlayerUtil.clickable;
import static org.dghdidi.stafftool.util.PlayerUtil.getDisplayName;
import static org.dghdidi.stafftool.util.PlayerUtil.legacy;

public class ReportsStorage {
    private static final Set<Integer> set = new HashSet<>();
    private static final Set<Report> reports = new HashSet<>();

    public static void add(int index, String playerName, String reportedName) {
        set.add(index);
        reports.add(new Report(playerName, reportedName));
    }

    public static void del(int index, String playerName, String reportedName) {
        if (!set.contains(index)) {
            return;
        }
        set.remove(index);
        reports.remove(new Report(playerName, reportedName));
    }

    public static int getNum() {
        return set.size();
    }

    public static int getUnacceptedNum() {
        int count = 0;
        for (int index : set) {
            try {
                List<String> reportInfo = getReportInfo(index);
                if (reportInfo == null) {
                    continue;
                }
                String staffName = reportInfo.get(2);
                if (staffName == null || staffName.isBlank() || Objects.equals(staffName, "null")) {
                    count++;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return count;
    }

    public static boolean contains(int index) {
        return set.contains(index);
    }

    public static boolean containsPair(String playerName, String reportedName) {
        return reports.contains(new Report(playerName, reportedName));
    }

    public static void clear() {
        set.clear();
        reports.clear();
    }

    public static boolean showReport(Player player, int index, boolean isSelf) throws SQLException {
        List<String> reportInfo = getReportInfo(index);
        if (reportInfo == null || !set.contains(index)) {
            return false;
        }
        String playerName = reportInfo.get(0);
        String reportedName = reportInfo.get(1);
        String staffName = reportInfo.get(2);
        String reason = reportInfo.get(4);
        if (isSelf ^ Objects.equals(staffName, player.getUsername())) {
            return true;
        }

        Component teleportTo = clickable("§8[§f传送至该玩家§8]", "/reports tpto " + reportedName, "§a点击传送到该玩家的位置");
        Component acceptReport;
        if (Objects.equals(staffName, "null")) {
            acceptReport = clickable("§8[§a受理举报§8]", "/reports accept " + index, "§a点击受理此举报");
        } else if (Objects.equals(staffName, player.getUsername())) {
            acceptReport = clickable("§8[§a§l您已受理§8]", "/reports accept " + index, "§a§l您已受理此举报");
        } else {
            acceptReport = clickable("§8§m[已被" + staffName + "受理]", "", "§c§l此举报已被受理");
        }
        Component completeReport = clickable("§8[§e完成举报§8]", "/reports complete " + index, "§a点击完成处理此举报");
        Component deleteReport = clickable("§8[§c删除举报§8]", "/reports delete " + index, "§c点击删除此举报");
        Component commandLine = Component.empty()
                .append(teleportTo).append(legacy("  "))
                .append(acceptReport).append(legacy("  "))
                .append(completeReport).append(legacy("  "));
        if (player.hasPermission("reports.staff.delete")) {
            commandLine = commandLine.append(deleteReport);
        }

        player.sendMessage(legacy("§8------------------------------------------"));
        player.sendMessage(legacy("§e被举报人: §7" + (isOnline(reportedName) ? getDisplayName(reportedName) + "§8 (§a在线§8)" : "§8" + reportedName + "§8 (§c离线§8)")));
        player.sendMessage(legacy("§a举报人: §7" + (isOnline(playerName) ? getDisplayName(playerName) + "§8 (§a在线§8)" : "§8" + playerName + "§8 (§c离线§8)")));
        player.sendMessage(legacy("§b编号: §7#" + index + "  §b原因: §7" + reason));
        player.sendMessage(Component.empty());
        player.sendMessage(commandLine);
        player.sendMessage(legacy("§8------------------------------------------"));
        return true;
    }

    public static void showReports(Player player) throws SQLException {
        for (int index : set) {
            showReport(player, index, false);
        }
        for (int index : set) {
            showReport(player, index, true);
        }
    }

    public static boolean isOnline(String playerName) {
        return StaffTool.proxy.getPlayer(playerName).isPresent();
    }
}
