package org.dghdidi.stafftool.feature;

import com.velocitypowered.api.command.SimpleCommand;
import org.dghdidi.stafftool.StaffTool;
import org.dghdidi.stafftool.config.LoadConfig;

import java.util.Objects;

import static org.dghdidi.stafftool.StaffTool.databaseManager;
import static org.dghdidi.stafftool.util.PlayerUtil.sendMessage;

public class Reload implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length != 1) {
            sendMessage(invocation.source(), "§c用法: /stafftool reload|help");
            return;
        }
        if (Objects.equals(args[0], "reload")) {
            sendMessage(invocation.source(), "§e正在重载配置与数据库连接...");
            StaffTool.proxy.getScheduler().buildTask(StaffTool.plugin, () -> {
                try {
                    LoadConfig.loadConfig();
                    databaseManager.reload();
                    sendMessage(invocation.source(), "§a数据库连接成功，插件已成功重载");
                } catch (Exception e) {
                    sendMessage(invocation.source(), "§c重载失败或数据库连接失败，请检查配置文件");
                    e.printStackTrace();
                }
            }).schedule();
        } else if (Objects.equals(args[0], "help")) {
            sendMessage(invocation.source(), "§8---------§b§lStaffTool§8---------");
            sendMessage(invocation.source(), "§a/punish <ID> §7对玩家实施处罚");
            sendMessage(invocation.source(), "§a/tpto <ID> §7跨服传送到某玩家的位置");
            sendMessage(invocation.source(), "§a/amc|unamc <ID> §7向某玩家发送(解除)查端命令");
            sendMessage(invocation.source(), "§a/chathistory <ID> §7查看某玩家的聊天记录");
            sendMessage(invocation.source(), "§a/staffs §7查看在线工作人员");
            sendMessage(invocation.source(), "§a/stafftool reload §7重载插件");
            sendMessage(invocation.source(), "§8---------------------------");
        } else {
            sendMessage(invocation.source(), "§c用法: /stafftool reload|help");
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("staff.reload");
    }
}
