package org.encinet.noAnyBoom.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WarningUtils {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final long WARNING_COOLDOWN = 3000; // 3 seconds
    private static final Map<String, Long> lastWarningTimes = new ConcurrentHashMap<>();

    private static final String WARNING_TEMPLATE = "<gradient:#ff5555:#aa0000>[NoAnyBoom]</gradient> <yellow><player></yellow> <white><action></white> <yellow><subject></yellow>";

    private static boolean isOnCooldown(String key) {
        long currentTime = System.currentTimeMillis();
        Long lastTime = lastWarningTimes.get(key);
        if (lastTime != null && (currentTime - lastTime) < WarningUtils.WARNING_COOLDOWN) {
            return true;
        }
        lastWarningTimes.put(key, currentTime);
        return false;
    }

    public static void broadcast(String action, Object source, String subject, Object target) {
        String sourceName = "System";
        if (source instanceof Player player) {
            sourceName = player.getName();
        } else if (source instanceof String s && !s.isEmpty()) {
            sourceName = s;
        }

        String cooldownKey = action + ":" + sourceName + ":" + subject;

        if (isOnCooldown(cooldownKey)) {
            return;
        }

        TagResolver tags = TagResolver.builder()
                .tag("player", Tag.inserting(Component.text(sourceName)))
                .tag("action", Tag.inserting(Component.text(action)))
                .tag("subject", Tag.inserting(Component.text(subject)))
                .build();

        Component message = MINI_MESSAGE.deserialize(WARNING_TEMPLATE, tags);

        if (target != null) {
            message = message.append(Component.text(" @ ", NamedTextColor.DARK_GRAY))
                    .append(createTeleportComponent(target));
        }

        Bukkit.getServer().broadcast(message);
    }

    public static void broadcastScanSummary(Object source, Map<String, Integer> bannedBlocksFound, Location center) {
        String sourceName = "System";
        if (source instanceof Player player) {
            sourceName = player.getName();
        } else if (source instanceof String s && !s.isEmpty()) {
            sourceName = s;
        }

        StringBuilder subjectBuilder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Integer> entry : bannedBlocksFound.entrySet()) {
            if (!first) {
                subjectBuilder.append(", ");
            }
            subjectBuilder.append(entry.getKey()).append("×").append(entry.getValue());
            first = false;
        }
        String subject = subjectBuilder.toString();

        String cooldownKey = "scan:" + sourceName + ":" + subject;
        if (isOnCooldown(cooldownKey)) {
            return;
        }

        TagResolver tags = TagResolver.builder()
                .tag("player", Tag.inserting(Component.text(sourceName)))
                .tag("action", Tag.inserting(Component.text("triggered a scan and found")))
                .tag("subject", Tag.inserting(Component.text(subject)))
                .build();

        Component message = MINI_MESSAGE.deserialize(WARNING_TEMPLATE, tags);

        if (center != null) {
            message = message.append(Component.text(" @ ", NamedTextColor.DARK_GRAY))
                    .append(createTeleportComponent(center));
        }

        Bukkit.getServer().broadcast(message);
    }

    private static Component createTeleportComponent(Object target) {
        if (target instanceof Location loc) {
            String worldName = loc.getWorld() != null ? loc.getWorld().getName() : "未知";
            int x = loc.getBlockX();
            int y = loc.getBlockY();
            int z = loc.getBlockZ();
            String tpCommand = String.format("/minecraft:tp @s %d %d %d", x, y, z);
            String coords = String.format("%s (%d,%d,%d)", worldName, x, y, z);
            return MINI_MESSAGE.deserialize(
                    "<gradient:gold:yellow><hover:show_text:'<white>点击传送</white>'><click:run_command:'" + tpCommand + "'>" + coords + "</click></hover></gradient>"
            );
        }
        if (target instanceof Player p) {
            String playerName = p.getName();
            String tpCommand = String.format("/minecraft:tp @s %s", playerName);
            return MINI_MESSAGE.deserialize(
                    "<gradient:aqua:green><hover:show_text:'<white>点击传送到 " + playerName + "</white>'><click:run_command:'" + tpCommand + "'>" + playerName + "</click></hover></gradient>"
            );
        }
        return Component.text("未知位置", NamedTextColor.GRAY);
    }
}
