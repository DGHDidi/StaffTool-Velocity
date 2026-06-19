package org.dghdidi.stafftool.feature.clientchecker;

import com.velocitypowered.api.proxy.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class InfoBook {
    private static final Map<UUID, Future<?>> map = new ConcurrentHashMap<>();

    public static void add(Player player, Future<?> thread) {
        map.put(player.getUniqueId(), thread);
    }

    public static void del(Player player) {
        map.remove(player.getUniqueId());
    }

    public static boolean check(Player player) {
        return map.containsKey(player.getUniqueId());
    }

    public static Future<?> get(Player player) {
        return map.get(player.getUniqueId());
    }
}
