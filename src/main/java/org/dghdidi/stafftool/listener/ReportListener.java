package org.dghdidi.stafftool.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import org.dghdidi.stafftool.StaffTool;
import org.dghdidi.stafftool.feature.reports.ReportsStorage;

import java.util.concurrent.TimeUnit;

import static org.dghdidi.stafftool.config.LoadConfig.enableReports;
import static org.dghdidi.stafftool.feature.reports.StaffCMD.reportStaffPrefix;
import static org.dghdidi.stafftool.util.PlayerUtil.sendMessage;

public class ReportListener {
    @Subscribe
    public void onStaffJoin(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        if (!enableReports || !player.hasPermission("reports.staff")) {
            return;
        }

        int unacceptedNum = ReportsStorage.getUnacceptedNum();
        if (unacceptedNum == 0) {
            return;
        }

        StaffTool.proxy.getScheduler().buildTask(StaffTool.plugin, () ->
                        sendMessage(player, reportStaffPrefix + "§a当前有 §e" + unacceptedNum + " 条未处理的举报, 使用 §b/reports §a查看"))
                        .delay(1, TimeUnit.SECONDS)
                        .schedule();
    }
}
