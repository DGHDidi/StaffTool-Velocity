package org.dghdidi.stafftool.feature.clientchecker;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.dghdidi.stafftool.StaffTool;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import static org.dghdidi.stafftool.feature.clientchecker.Service.executorService;
import static org.dghdidi.stafftool.listener.LoginLogoutListener.staffPrefix;
import static org.dghdidi.stafftool.util.PlayerUtil.getDisplayName;
import static org.dghdidi.stafftool.util.PlayerUtil.legacy;
import static org.dghdidi.stafftool.util.PlayerUtil.sendAll;
import static org.dghdidi.stafftool.util.PlayerUtil.sendMessage;
import static org.dghdidi.stafftool.util.PlayerUtil.tabComplete;

public class AutoMessage implements SimpleCommand {
    public static String playerPrefix;

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player staff)) {
            sendMessage(invocation.source(), "§c只有玩家才能使用此命令");
            return;
        }
        String[] args = invocation.arguments();
        if (args.length != 2) {
            sendMessage(staff, "§c用法: /amc <ID> <你的QQ>");
            return;
        }
        String targetID = args[0];
        String qq = args[1];
        Player player = StaffTool.proxy.getPlayer(targetID).orElse(null);
        if (player == null) {
            sendMessage(staff, "§c当前玩家不在线或不存在");
            return;
        }
        if (InfoBook.check(player)) {
            sendMessage(staff, "§c当前玩家已经被发送查端信息");
            return;
        }
        sendAll(staffPrefix + getDisplayName(staff) + " §a正在向 " + getDisplayName(player) + " §a发送查端信息", "staff.notify");
        InfoBook.add(player, executorService.submit((Callable<Void>) () -> {
            runTask(player, staff, qq, targetID);
            return null;
        }));
    }

    private void runTask(Player player, Player staff, String qq, String targetID) {
        int counter = 1;
        while (counter <= 20) {
            if (!player.isActive()) {
                sendMessage(staff, "§a§l玩家§e§l" + targetID + "§c§l已经离线§7(视为拒绝查端), §a§l请执行处罚");
                StaffTool.proxy.getCommandManager().executeAsync(staff, "punish " + targetID);
                InfoBook.del(player);
                return;
            }
            sendMessage(staff, "§a§l已向玩家 §e§l" + targetID + "§a§l 发送查端信息§7(" + counter + "/20)!");
            sendMessage(player, playerPrefix + "§f您的游戏行为被 §c§l检测异常§f，为创造良好的游戏环境，需要对您的客户端进行检查，请您§e在五分钟内§f添加工作人员QQ: §b" + qq + "，§e§l无视或退出服务器§f将会被视为作弊§c§l封禁§f，感谢您的理解和配合");
            sendTitle(player, "§e请在五分钟内添加QQ§b§l" + qq, 10, 250, 10);
            counter++;
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                StaffTool.logger.log(Level.WARNING, "§c线程等待异常§e(可能是有工作人员取消信息发送)");
                if (InfoBook.check(player)) {
                    InfoBook.del(player);
                }
                return;
            }
        }
        sendMessage(staff, "§a§l查端信息已发送完毕，请判断是否施加处罚");
        InfoBook.del(player);
    }

    public void sendTitle(Player player, String subTitle, int fadeIn, int stay, int fadeOut) {
        Component title = Component.text("查端通知")
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.BOLD);
        player.showTitle(Title.title(
                title,
                legacy(subTitle),
                Title.Times.times(ticks(fadeIn), ticks(stay), ticks(fadeOut))
        ));
    }

    private Duration ticks(int ticks) {
        return Duration.ofMillis(ticks * 50L);
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
