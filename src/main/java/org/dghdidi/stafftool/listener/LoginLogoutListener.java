package org.dghdidi.stafftool.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;

import static org.dghdidi.stafftool.util.PlayerUtil.getDisplayName;
import static org.dghdidi.stafftool.util.PlayerUtil.sendAll;

public class LoginLogoutListener {
    public static String staffPrefix;

    @Subscribe
    public void onPlayerJoin(PostLoginEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("staff.notify")) {
            sendAll(staffPrefix + getDisplayName(player) + " §e上线了", "staff.notify");
        }
    }

    @Subscribe
    public void onPlayerExit(DisconnectEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("stafftool.notify")) {
            sendAll(staffPrefix + getDisplayName(player) + " §e离线了", "staff.notify");
        }
    }
}
