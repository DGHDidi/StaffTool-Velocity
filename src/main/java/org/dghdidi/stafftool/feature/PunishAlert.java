package org.dghdidi.stafftool.feature;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import org.dghdidi.stafftool.StaffTool;

import static org.dghdidi.stafftool.util.PlayerUtil.legacy;
import static org.dghdidi.stafftool.util.PlayerUtil.sendMessage;

public class PunishAlert implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            sendMessage(invocation.source(), "§c该命令只能由玩家执行!");
            return;
        }
        String message = "§c§l一位玩家因为违反游戏规定而被处罚 §e使用 §a/report <ID> §e进行举报!";
        StaffTool.proxy.sendMessage(legacy(message));
        String[] args = invocation.arguments();
        if (args.length > 0) {
            StaffTool.proxy.getCommandManager().executeAsync(player, String.join(" ", args));
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("staff.alert") && invocation.source().hasPermission("staff.bc");
    }
}
