package org.dghdidi.stafftool.feature;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import org.dghdidi.stafftool.StaffTool;

import java.util.List;

import static org.dghdidi.stafftool.util.PlayerUtil.sendMessage;
import static org.dghdidi.stafftool.util.PlayerUtil.tabComplete;
import static org.dghdidi.stafftool.util.TeleportUtil.teleportToPlayer;

public class Teleport implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            sendMessage(invocation.source(), "§c只有玩家才能执行此命令");
            return;
        }
        String[] args = invocation.arguments();
        if (args.length != 1) {
            sendMessage(player, "§c用法: /tpto <ID>");
            return;
        }
        Player targetPlayer = StaffTool.proxy.getPlayer(args[0]).orElse(null);
        if (targetPlayer == null) {
            sendMessage(player, "§c当前玩家不在线或不存在");
            return;
        }
        teleportToPlayer(player, targetPlayer);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return tabComplete(invocation.arguments());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("staff.tpto");
    }
}
