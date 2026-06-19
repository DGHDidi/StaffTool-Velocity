package org.dghdidi.stafftool.feature.clientchecker;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import org.dghdidi.stafftool.StaffTool;

import java.util.List;

import static org.dghdidi.stafftool.listener.LoginLogoutListener.staffPrefix;
import static org.dghdidi.stafftool.util.PlayerUtil.getDisplayName;
import static org.dghdidi.stafftool.util.PlayerUtil.sendAll;
import static org.dghdidi.stafftool.util.PlayerUtil.sendMessage;
import static org.dghdidi.stafftool.util.PlayerUtil.tabComplete;

public class CancelMessage implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player)) {
            sendMessage(invocation.source(), "§c只有玩家才能使用此命令");
            return;
        }
        String[] args = invocation.arguments();
        if (args.length != 1) {
            sendMessage(invocation.source(), "§c用法: /unamc <ID>");
            return;
        }
        String id = args[0];
        Player player = StaffTool.proxy.getPlayer(id).orElse(null);
        if (player == null || !player.isActive()) {
            sendMessage(invocation.source(), "§c当前玩家不在线");
            return;
        }
        if (!InfoBook.check(player)) {
            sendMessage(invocation.source(), "§c当前玩家没有被发送查端命令");
            return;
        }
        InfoBook.get(player).cancel(true);
        InfoBook.del(player);
        sendMessage(invocation.source(), "§a§l成功解除对玩家 §e§l" + id + "§a§l 的查端命令");
        sendMessage(player, "§a§l您的查端命令已被工作人员解除");
        sendAll(staffPrefix + getDisplayName(invocation.source()) + " §a解除了对玩家 " + getDisplayName(player) + " §a的查端命令", "staff.notify");
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return tabComplete(invocation.arguments());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("staff.amc");
    }
}
