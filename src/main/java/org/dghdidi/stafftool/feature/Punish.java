package org.dghdidi.stafftool.feature;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.dghdidi.stafftool.StaffTool;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import static org.dghdidi.stafftool.util.PlayerUtil.legacy;
import static org.dghdidi.stafftool.util.PlayerUtil.redClickable;
import static org.dghdidi.stafftool.util.PlayerUtil.sendMessage;
import static org.dghdidi.stafftool.util.PlayerUtil.tabComplete;

public class Punish implements SimpleCommand {
    private static String historyCommand = "history %player%";
    private static String menuTitle = "§e§l请选择你需要的处罚: §b§l%player%";
    private static String banLabel = "封禁类:";
    private static String muteLabel = "禁言类:";
    private static List<Option> banOptions = defaultBanOptions();
    private static List<Option> muteOptions = defaultMuteOptions();

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

        String renderedHistoryCommand = render(historyCommand, id);
        if (!renderedHistoryCommand.isBlank()) {
            StaffTool.proxy.getCommandManager().executeAsync(player, renderedHistoryCommand);
        }
        Component banLine = buildLine(banLabel, banOptions, id);
        Component muteLine = buildLine(muteLabel, muteOptions, id);

        sendMessage(player, "§c---------------------------------");
        sendMessage(player, render(menuTitle, id));
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

    private Component buildLine(String label, List<Option> options, String playerName) {
        Component line = legacy(label);
        for (Option option : options) {
            line = line.append(button(option.text(), render(option.command(), playerName), render(option.hover(), playerName)));
        }
        return line;
    }

    private static String render(String value, String playerName) {
        return value == null ? "" : value.replace("%player%", playerName).replace("%target%", playerName);
    }

    public static void configure(Map<String, Object> config) {
        historyCommand = value(config, "historyCommand", "history %player%");
        menuTitle = value(config, "menuTitle", "§e§l请选择你需要的处罚: §b§l%player%");
        banLabel = value(config, "banLabel", "封禁类:");
        muteLabel = value(config, "muteLabel", "禁言类:");
        banOptions = options(config.get("banOptions"), defaultBanOptions());
        muteOptions = options(config.get("muteOptions"), defaultMuteOptions());
    }

    private static String value(Map<String, Object> config, String key, String fallback) {
        Object value = config.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private static List<Option> options(Object value, List<Option> fallback) {
        if (!(value instanceof List<?> list)) {
            return fallback;
        }
        List<Option> parsed = new ArrayList<>();
        for (Object entry : list) {
            if (!(entry instanceof Map<?, ?> item)) {
                continue;
            }
            Option option = new Option(
                    item.get("text") == null ? "" : String.valueOf(item.get("text")),
                    item.get("command") == null ? "" : String.valueOf(item.get("command")),
                    item.get("hover") == null ? "" : String.valueOf(item.get("hover")));
            if (!option.text().isBlank() && !option.command().isBlank()) {
                parsed.add(option);
            }
        }
        return parsed.isEmpty() ? fallback : parsed;
    }

    private static List<Option> defaultBanOptions() {
        return List.of(
                new Option(" [7D]", "/mp ipban %player% 7d 使用第三方软件破坏游戏规则", "§e点击封禁7天"),
                new Option(" [14D]", "/mp ipban %player% 14d 使用第三方软件破坏游戏规则", "§e点击封禁14天"),
                new Option(" [30D]", "/mp ipban %player% 30d 使用第三方软件破坏游戏规则", "§e点击封禁30天"),
                new Option(" [90D]", "/mp ipban %player% 90d 使用第三方软件破坏游戏规则", "§e点击封禁90天"),
                new Option(" [360D]", "/mp ipban %player% 360d 使用第三方软件破坏游戏规则", "§e点击封禁360天"),
                new Option(" [永久]", "/mp ipban %player% 使用第三方软件破坏游戏规则", "§e点击封禁永久")
        );
    }

    private static List<Option> defaultMuteOptions() {
        return List.of(
                new Option(" [10MIN]", "/mp ipmute %player% 10min 言辞过激或违规，请注意言行举止", "§e点击禁言10分钟"),
                new Option(" [3H]", "/mp ipmute %player% 3h 言辞过激或违规，请注意言行举止", "§e点击禁言3小时"),
                new Option(" [1D]", "/mp ipmute %player% 1d 言辞过激或违规，请注意言行举止", "§e点击禁言1天"),
                new Option(" [3D]", "/mp ipmute %player% 3d 言辞过激或违规，请注意言行举止", "§e点击禁言3天"),
                new Option(" [7D]", "/mp ipmute %player% 7d 言辞过激或违规，请注意言行举止", "§e点击禁言7天"),
                new Option(" [30D]", "/mp ipmute %player% 30d 言辞过激或违规，请注意言行举止", "§e点击禁言30天")
        );
    }

    private record Option(String text, String command, String hover) {
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
