package org.dghdidi.stafftool.feature;

import com.velocitypowered.api.command.SimpleCommand;
import org.dghdidi.stafftool.StaffTool;
import org.dghdidi.stafftool.config.LoadConfig;

import java.util.List;

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
        if (args[0].equalsIgnoreCase("reload")) {
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
        } else if (args[0].equalsIgnoreCase("help")) {
            showHelp(invocation);
        } else {
            sendMessage(invocation.source(), "§c用法: /stafftool reload|help");
        }
    }

    private void showHelp(Invocation invocation) {
        sendMessage(invocation.source(), "§8---------§b§lStaffTool§8---------");
        if (LoadConfig.isPunishEnabled()) {
            sendMessage(invocation.source(), "§a/punish <ID> §7打开玩家处罚快捷面板");
        }
        if (LoadConfig.isTeleportEnabled()) {
            sendMessage(invocation.source(), "§a/tpto <ID> §7跨服传送到某玩家的位置");
        }
        if (LoadConfig.isFindPlayerEnabled()) {
            sendMessage(invocation.source(), "§a/find <ID> §7查找玩家所在服务器并快捷传送");
        }
        if (LoadConfig.isClientCheckerEnabled()) {
            sendMessage(invocation.source(), "§a/amc <ID> §7向某玩家发送查端提醒");
            sendMessage(invocation.source(), "§a/unamc <ID> §7解除某玩家的查端提醒");
        }
        if (LoadConfig.isChatHistoryEnabled()) {
            sendMessage(invocation.source(), "§a/chathistory <ID> §7查看某玩家的聊天记录");
        }
        if (LoadConfig.isOnlineStaffEnabled()) {
            sendMessage(invocation.source(), "§a/staffs §7查看在线工作人员");
        }
        if (LoadConfig.isStaffChatEnabled()) {
            sendMessage(invocation.source(), "§a/sc <消息> §7发送工作人员频道消息");
            sendMessage(invocation.source(), "§a/ac <消息> §7发送管理员频道消息");
        }
        if (LoadConfig.isReportsEnabled()) {
            sendMessage(invocation.source(), "§a/report <ID> <原因> §7举报某位玩家");
            sendMessage(invocation.source(), "§a/reports §7查看当前举报 §8(/reports help 查看详情)");
        }
        sendMessage(invocation.source(), "§a/stafftool reload §7重载配置与数据库连接");
        sendMessage(invocation.source(), "§a/stafftool help §7查看本帮助");
        sendMessage(invocation.source(), "§8---------------------------");
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length != 1) {
            return List.of();
        }
        String input = args[0].toLowerCase();
        return List.of("help", "reload").stream()
                .filter(option -> option.startsWith(input))
                .toList();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("staff.reload");
    }
}
