package org.dghdidi.stafftool.feature.reports;

import com.velocitypowered.api.proxy.Player;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.dghdidi.stafftool.feature.reports.PlayerCMD.reportPlayerPrefix;
import static org.dghdidi.stafftool.util.PlayerUtil.sendMessage;

public class ReportPunishment {
    private static final Map<UUID, Long> map = new ConcurrentHashMap<>();

    public static void addPunish(Player player, long timeStamp) {
        map.put(player.getUniqueId(), timeStamp + System.currentTimeMillis());
        sendMessage(player, reportPlayerPrefix + "§c您因为违规举报被工作人员处罚，在 " + formatTime(timeStamp) + "§c后才能进行举报");
    }

    public static void removePunish(Player player) {
        map.remove(player.getUniqueId());
        sendMessage(player, reportPlayerPrefix + "§a对您违规举报的处罚已被工作人员撤销，很抱歉给您带来的不便!");
    }

    public static boolean isUnderPunish(Player player) {
        Long time = map.get(player.getUniqueId());
        if (time == null) {
            return false;
        }
        if (time <= System.currentTimeMillis()) {
            map.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    public static String getRemainTime(Player player) {
        long end = map.get(player.getUniqueId());
        return formatTime(end - System.currentTimeMillis());
    }

    public static long convertToMilliseconds(Player sender, String input) {
        Pattern pattern = Pattern.compile("(\\d+)([a-zA-Z]+)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.matches()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            return switch (unit.toLowerCase()) {
                case "d" -> Duration.ofDays(value).toMillis();
                case "h" -> Duration.ofHours(value).toMillis();
                case "min" -> Duration.ofMinutes(value).toMillis();
                case "s" -> Duration.ofSeconds(value).toMillis();
                default -> {
                    sendMessage(sender, "§c您输入的时间格式有误!");
                    throw new IllegalArgumentException("无法识别的时间单位");
                }
            };
        }
        sendMessage(sender, "§c您输入的时间格式有误!");
        throw new IllegalArgumentException("无效的时间字符串");
    }

    public static String formatTime(long milliseconds) {
        Duration duration = Duration.ofMillis(milliseconds);
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        String result = "";
        if (days != 0) {
            result += days + " 天 ";
        }
        if (hours != 0 || days != 0) {
            result += hours + " 小时 ";
        }
        return result + minutes + " 分钟";
    }
}
