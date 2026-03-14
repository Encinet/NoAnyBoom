package org.encinet.noAnyBoom.utils;

import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ViolationTracker {
    public static final int THRESHOLD = 3;
    private static final long BAN_BASE_MINUTES = 30;
    private static final Map<UUID, Integer> violations = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> banCounts = new ConcurrentHashMap<>();

    public static void record(Player player) {
        int count = violations.merge(player.getUniqueId(), 1, Integer::sum);
        if (count >= THRESHOLD) {
            violations.remove(player.getUniqueId());
            int bans = banCounts.merge(player.getUniqueId(), 1, Integer::sum);
            long minutes = BAN_BASE_MINUTES * (1L << (bans - 1)); // 30 * 2^(bans-1)
            String banReason = "Banned by NoAnyBoom for repeated violations";
            player.ban(banReason, Duration.ofMinutes(minutes), "NoAnyBoom", true);
            WarningUtils.broadcast("has been banned for " + minutes + " minutes", "NoAnyBoom", player.getName(), null);
        }
    }

    public static Map<UUID, Integer> getAllViolations() {
        return violations;
    }

    public static Map<UUID, Integer> getAllBanCounts() {
        return banCounts;
    }

    public static void resetViolations(UUID uuid) {
        violations.remove(uuid);
    }

    public static void resetBanCount(UUID uuid) {
        banCounts.remove(uuid);
    }

    public static int getViolations(UUID uuid) {
        return violations.getOrDefault(uuid, 0);
    }

    public static int getBanCount(UUID uuid) {
        return banCounts.getOrDefault(uuid, 0);
    }
}