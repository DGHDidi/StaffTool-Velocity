package org.dghdidi.stafftool.util;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.model.user.User;
import org.dghdidi.stafftool.StaffTool;

import java.util.ArrayList;
import java.util.List;

import static org.dghdidi.stafftool.StaffTool.luckPerms;

public class PlayerUtil {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    public static List<Player> getPlayers(String perm) {
        List<Player> players = new ArrayList<>();
        for (Player player : StaffTool.proxy.getAllPlayers()) {
            if (player.hasPermission(perm)) {
                players.add(player);
            }
        }
        return players;
    }

    public static String getDisplayName(CommandSource source) {
        return source instanceof Player player ? getDisplayName(player) : "CONSOLE";
    }

    public static String getDisplayName(Player player) {
        if (player == null) {
            return "Unknown";
        }
        if (luckPerms == null) {
            return "§7" + player.getUsername();
        }
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            StaffTool.logger.info("§c§l无法获取玩家 " + player.getUsername() + " 的 LuckPerms 用户数据");
            return "§7" + player.getUsername();
        }
        String prefix = user.getCachedData().getMetaData().getPrefix() == null ? "§7" : user.getCachedData().getMetaData().getPrefix();
        String suffix = user.getCachedData().getMetaData().getSuffix() == null ? "§7" : user.getCachedData().getMetaData().getSuffix();
        return transColor(prefix) + player.getUsername() + transColor(suffix);
    }

    public static String getDisplayName(String playerName) {
        return StaffTool.proxy.getPlayer(playerName).map(PlayerUtil::getDisplayName).orElse("§8" + playerName);
    }

    public static String transColor(String arg) {
        return arg == null ? "" : arg.replace('&', '§');
    }

    public static Component legacy(String msg) {
        return LEGACY.deserialize(msg == null ? "" : msg);
    }

    public static void sendMessage(CommandSource source, String msg) {
        source.sendMessage(legacy(msg));
    }

    public static void sendMessage(CommandSource source, Component component) {
        source.sendMessage(component);
    }

    public static void sendMessage(String playerName, String msg) {
        StaffTool.proxy.getPlayer(playerName).ifPresent(player -> sendMessage(player, msg));
    }

    public static void sendAll(String msg) {
        Component component = legacy(msg);
        for (Player player : StaffTool.proxy.getAllPlayers()) {
            player.sendMessage(component);
        }
    }

    public static void sendAll(String msg, String perm) {
        Component component = legacy(msg);
        for (Player player : getPlayers(perm)) {
            player.sendMessage(component);
        }
    }

    public static List<String> tabComplete(String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        String partialID = args[0].toLowerCase();
        List<String> matchedIDs = new ArrayList<>();
        for (Player player : StaffTool.proxy.getAllPlayers()) {
            String id = player.getUsername();
            if (id.toLowerCase().startsWith(partialID)) {
                matchedIDs.add(id);
            }
        }
        return matchedIDs;
    }

    public static Component clickable(String text, String command, String hoverText) {
        Component component = legacy(text);
        if (command != null && !command.isBlank()) {
            component = component.clickEvent(ClickEvent.runCommand(command));
        }
        if (hoverText != null && !hoverText.isBlank()) {
            component = component.hoverEvent(HoverEvent.showText(legacy(hoverText)));
        }
        return component;
    }

    public static Component redClickable(String text, String command, String hoverText) {
        return clickable(text, command, hoverText).color(NamedTextColor.RED);
    }
}
