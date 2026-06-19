package org.dghdidi.stafftool.feature.chat;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import static org.dghdidi.stafftool.listener.LoginLogoutListener.staffPrefix;
import static org.dghdidi.stafftool.util.PlayerUtil.getDisplayName;
import static org.dghdidi.stafftool.util.PlayerUtil.sendAll;
import static org.dghdidi.stafftool.util.PlayerUtil.sendMessage;

public class StaffChat implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player)) {
            sendMessage(invocation.source(), "§c该命令只能由玩家执行!");
            return;
        }
        String[] args = invocation.arguments();
        if (args.length < 1) {
            sendMessage(invocation.source(), "§c用法: /sc <消息>");
            return;
        }
        sendAll(staffPrefix + getDisplayName(invocation.source()) + "§f: " + String.join(" ", args), "staff.chat.staff");
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("staff.chat.staff");
    }
}
