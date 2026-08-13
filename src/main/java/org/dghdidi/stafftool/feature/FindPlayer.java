package org.dghdidi.stafftool.feature;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.dghdidi.stafftool.StaffTool;

import java.util.List;

import static org.dghdidi.stafftool.util.PlayerUtil.clickable;
import static org.dghdidi.stafftool.util.PlayerUtil.getDisplayName;
import static org.dghdidi.stafftool.util.PlayerUtil.legacy;
import static org.dghdidi.stafftool.util.PlayerUtil.sendMessage;
import static org.dghdidi.stafftool.util.PlayerUtil.tabComplete;

public class FindPlayer implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length != 1) {
            sendMessage(invocation.source(), "§c用法: /find <ID>");
            return;
        }

        Player target = StaffTool.proxy.getPlayer(args[0]).orElse(null);
        if (target == null) {
            sendMessage(invocation.source(), "§c该玩家不在线或不存在!");
            return;
        }

        String serverName = target.getCurrentServer()
                .map(connection -> connection.getServerInfo().getName())
                .orElse("Unknown");
        String displayName = getDisplayName(target);
        Component result = legacy("§a玩家 " + displayName + " §a当前位于服务器 §e" + serverName)
                .append(legacy("\n"))
                .append(clickable("§8[§b点击传送到该玩家§8]", "/tpto " + target.getUsername(), "§e点击传送到 " + displayName));
        sendMessage(invocation.source(), result);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return tabComplete(invocation.arguments());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("staff.find");
    }
}
