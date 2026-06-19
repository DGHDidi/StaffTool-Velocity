package org.dghdidi.stafftool.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import org.dghdidi.stafftool.feature.reports.ReportsStorage;

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

        sendMessage(player, reportStaffPrefix + "§a当前有 §e" + unacceptedNum + " §a个未受理举报，输入 §b/reports §a查看详情.");
    }
}
