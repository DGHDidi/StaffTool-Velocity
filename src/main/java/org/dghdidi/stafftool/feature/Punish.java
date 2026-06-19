package org.dghdidi.stafftool.feature;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.dghdidi.stafftool.StaffTool;

import java.util.List;

import static org.dghdidi.stafftool.util.PlayerUtil.legacy;
import static org.dghdidi.stafftool.util.PlayerUtil.redClickable;
import static org.dghdidi.stafftool.util.PlayerUtil.sendMessage;
import static org.dghdidi.stafftool.util.PlayerUtil.tabComplete;

public class Punish implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            sendMessage(invocation.source(), "§c该命令只能由玩家执行!");
            return;
        }
        String[] args = invocation.arguments();
        if (args.length != 1) {
            sendMessage(player, "§c用法: /punish <ID>");
            return;
        }
        String id = args[0];

        StaffTool.proxy.getCommandManager().executeAsync(player, "history " + id);
        Component banLine = legacy("封禁类:")
                .append(button(" [7D]", "/mp ipban " + id + " 7d 使用第三方软件破坏游戏规则", "§e点击封禁7天"))
                .append(button(" [14D]", "/mp ipban " + id + " 14d 使用第三方软件破坏游戏规则", "§e点击封禁14天"))
                .append(button(" [30D]", "/mp ipban " + id + " 30d 使用第三方软件破坏游戏规则", "§e点击封禁30天"))
                .append(button(" [90D]", "/mp ipban " + id + " 90d 使用第三方软件破坏游戏规则", "§e点击封禁90天"))
                .append(button(" [360D]", "/mp ipban " + id + " 360d 使用第三方软件破坏游戏规则", "§e点击封禁360天"))
                .append(button(" [永久]", "/mp ipban " + id + " 使用第三方软件破坏游戏规则", "§e点击封禁永久"));

        Component muteLine = legacy("禁言类:")
                .append(button(" [10MIN]", "/mp ipmute " + id + " 10min 言辞过激或违规，请注意言行举止", "§e点击禁言10分钟"))
                .append(button(" [3H]", "/mp ipmute " + id + " 3h 言辞过激或违规，请注意言行举止", "§e点击禁言3小时"))
                .append(button(" [1D]", "/mp ipmute " + id + " 1d 言辞过激或违规，请注意言行举止", "§e点击禁言1天"))
                .append(button(" [3D]", "/mp ipmute " + id + " 3d 言辞过激或违规，请注意言行举止", "§e点击禁言3天"))
                .append(button(" [7D]", "/mp ipmute " + id + " 7d 言辞过激或违规，请注意言行举止", "§e点击禁言7天"))
                .append(button(" [30D]", "/mp ipmute " + id + " 30d 言辞过激或违规，请注意言行举止", "§e点击禁言30天"));

        sendMessage(player, "§c---------------------------------");
        sendMessage(player, "§e§l请选择你需要的处罚: §b§l" + id);
        if (player.hasPermission("staff.punish.ban")) {
            sendMessage(player, banLine);
        }
        if (player.hasPermission("staff.punish.mute")) {
            sendMessage(player, muteLine);
        }
        sendMessage(player, "§c---------------------------------");
    }

    private Component button(String text, String command, String hoverText) {
        return redClickable(text, command, hoverText);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return tabComplete(invocation.arguments());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("staff.punish");
    }
}
