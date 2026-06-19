package org.dghdidi.stafftool.feature;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.dghdidi.stafftool.StaffTool;

import java.util.ArrayList;
import java.util.List;

import static org.dghdidi.stafftool.StaffTool.luckPerms;
import static org.dghdidi.stafftool.util.PlayerUtil.getDisplayName;
import static org.dghdidi.stafftool.util.PlayerUtil.sendMessage;

public class OnlineStaff implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        List<Player> staffs = new ArrayList<>();
        for (Player player : StaffTool.proxy.getAllPlayers()) {
            if (player.hasPermission("staff.online")) {
                staffs.add(player);
            }
        }
        staffs.sort((o1, o2) -> {
            int w1 = weight(o1);
            int w2 = weight(o2);
            return w1 == w2 ? o1.getUsername().compareToIgnoreCase(o2.getUsername()) : Integer.compare(w2, w1);
        });

        sendMessage(invocation.source(), "§e在线工作人员 §a" + staffs.size() + "人§e:");
        for (Player staff : staffs) {
            String server = staff.getCurrentServer()
                    .map(connection -> " §7(" + connection.getServerInfo().getName() + ")")
                    .orElse(" §7(Unknown)");
            sendMessage(invocation.source(), getDisplayName(staff) + server);
        }
    }

    private int weight(Player player) {
        if (luckPerms == null) {
            return 0;
        }
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return 0;
        }
        Group group = luckPerms.getGroupManager().getGroup(user.getPrimaryGroup());
        return group == null ? 0 : group.getWeight().orElse(0);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("staff.online");
    }
}
