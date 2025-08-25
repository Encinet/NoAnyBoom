package org.encinet.noAnyBoom.utils;

import net.kyori.adventure.text.Component;
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
        
        // 警告冷却时间（毫秒）
        private static final long WARNING_COOLDOWN = 3000; // 3秒冷却
        
        // 警告冷却记录
        private static final Map<String, Long> lastWarningTimes = new ConcurrentHashMap<>();
        private static final Map<String, Integer> warningCounts = new ConcurrentHashMap<>();

        // 使用 MiniMessage 格式的警告模板（优化配色）
        private static final String ITEM_WARNING = "<gradient:#ff5555:#aa0000>[<bold>⚠</bold> 警告]</gradient> <white>玩家 <yellow><player></yellow> 试图使用违禁物品 <yellow><item></yellow>";
        private static final String BLOCK_WARNING = "<gradient:#ff5555:#aa0000>[<bold>⚠</bold> 警告]</gradient> <white>玩家 <yellow><player></yellow> 试图放置违禁方块 <yellow><block></yellow>";
        private static final String ENTITY_WARNING = "<gradient:#ff5555:#aa0000>[<bold>⚠</bold> 警告]</gradient> <white>玩家 <yellow><player></yellow> 试图放置违禁实体 <yellow><entity></yellow>";
        private static final String COMMAND_WARNING = "<gradient:#ff5555:#aa0000>[<bold>⚠</bold> 警告]</gradient> <white>玩家 <yellow><player></yellow> 使用了违禁命令 <yellow><command></yellow>";

        // 创建可点击的传送组件（支持坐标和玩家）
        private static Component createTeleportComponent(Object target) {
                if (target == null) {
                        return MINI_MESSAGE.deserialize("<gray>未知位置</gray>");
                }

                if (target instanceof Location) {
                        Location location = (Location) target;
                        String worldName = location.getWorld() != null ? location.getWorld().getName() : "未知世界";
                        int x = location.getBlockX();
                        int y = location.getBlockY();
                        int z = location.getBlockZ();

                        String tpCommand = String.format("/minecraft:tp @s %d %d %d", x, y, z);
                        String coords = String.format("坐标 %s (%d,%d,%d)", worldName, x, y, z);

                        return MINI_MESSAGE.deserialize(
                                        "<gradient:gold:yellow><hover:show_text:'<white>点击传送到该位置</white>'><click:run_command:'"
                                                        + tpCommand + "'>" + coords + "</click></hover></gradient>");
                } else if (target instanceof Player) {
                        Player player = (Player) target;
                        String playerName = player.getName();
                        String tpCommand = String.format("/minecraft:tp @s %s", playerName);

                        return MINI_MESSAGE.deserialize(
                                        "<gradient:aqua:green><hover:show_text:'<white>点击传送到玩家 " + playerName
                                                        + "</white>'><click:run_command:'" + tpCommand + "'>玩家 "
                                                        + playerName + "</click></hover></gradient>");
                } else {
                        return Component.text("无效目标");
                }
        }

        /**
         * 检查是否在冷却时间内
         *
         * @param key 冷却键名
         * @param cooldown 冷却时间（毫秒）
         * @return 是否在冷却中
         */
        private static boolean isOnCooldown(String key, long cooldown) {
                long currentTime = System.currentTimeMillis();
                Long lastTime = lastWarningTimes.get(key);
                
                if (lastTime == null) {
                        lastWarningTimes.put(key, currentTime);
                        warningCounts.put(key, 1);
                        return false;
                }
                
                if (currentTime - lastTime < cooldown) {
                        warningCounts.put(key, warningCounts.getOrDefault(key, 0) + 1);
                        return true;
                }
                
                lastWarningTimes.put(key, currentTime);
                int count = warningCounts.getOrDefault(key, 0);
                warningCounts.put(key, 1);
                
                // 如果有累积的警告，发送汇总消息
                if (count > 1) {
                        broadcastAggregatedWarning(key, count);
                }
                
                return false;
        }
        
        /**
         * 广播累积的警告消息
         *
         * @param key 警告键名
         * @param count 警告次数
         */
        private static void broadcastAggregatedWarning(String key, int count) {
                String[] parts = key.split(":");
                String type = parts[0];
                String playerName = parts.length > 1 ? parts[1] : "系统";
                
                Component component = MINI_MESSAGE.deserialize(
                                "<gradient:#ff5555:#aa0000>[<bold>⚠</bold> 警告汇总]</gradient> <white>玩家 <yellow>" + playerName +
                                "</yellow> 在短时间内触发了 <yellow>" + count + "</yellow> 次 " + type + " 违规</white>");
                
                Bukkit.getServer().broadcast(component);
        }
        
        /**
         * 广播物品违规警告（带位置）
         *
         * @param player   玩家对象（可为null）
         * @param item     物品的本地化键名
         * @param location 事件发生位置
         */
        public static void broadcastItemWarning(Player player, String item, Object target) {
                String playerName = player != null ? player.getName() : "系统";
                String cooldownKey = "item:" + playerName + ":" + item;
                
                if (isOnCooldown(cooldownKey, WARNING_COOLDOWN)) {
                        return;
                }

                Component component = MINI_MESSAGE.deserialize(ITEM_WARNING,
                                TagResolver.builder()
                                                .resolver(TagResolver.resolver("player",
                                                                Tag.inserting(Component.text(playerName))))
                                                .resolver(TagResolver.resolver("item",
                                                                Tag.inserting(Component.translatable(item))))
                                                .build())
                                .append(MINI_MESSAGE.deserialize("<dark_gray> | </dark_gray>"))
                                .append(createTeleportComponent(target));

                Bukkit.getServer().broadcast(component);
        }

        /**
         * 广播方块违规警告（带位置）
         *
         * @param player   玩家对象（可为null）
         * @param block    方块名称
         * @param location 事件发生位置
         */
        public static void broadcastBlockWarning(Player player, String block, Object target) {
                String playerName = player != null ? player.getName() : "系统";
                String cooldownKey = "block:" + playerName + ":" + block;
                
                if (isOnCooldown(cooldownKey, WARNING_COOLDOWN)) {
                        return;
                }

                Component component = MINI_MESSAGE.deserialize(BLOCK_WARNING,
                                TagResolver.builder()
                                                .resolver(TagResolver.resolver("player",
                                                                Tag.inserting(Component.text(playerName))))
                                                .resolver(TagResolver.resolver("block",
                                                                Tag.inserting(Component.translatable(block))))
                                                .build())
                                .append(MINI_MESSAGE.deserialize("<dark_gray> | </dark_gray>"))
                                .append(createTeleportComponent(target));

                Bukkit.getServer().broadcast(component);
        }
        

        /**
         * 广播实体违规警告（带位置）
         *
         * @param player   玩家对象（可为null）
         * @param entity   实体的本地化键名
         * @param location 事件发生位置
         */
        public static void broadcastEntityWarning(Player player, String entity, Object target) {
                String playerName = player != null ? player.getName() : "系统";
                String cooldownKey = "entity:" + playerName + ":" + entity;
                
                if (isOnCooldown(cooldownKey, WARNING_COOLDOWN)) {
                        return;
                }

                Component component = MINI_MESSAGE.deserialize(ENTITY_WARNING,
                                TagResolver.builder()
                                                .resolver(TagResolver.resolver("player",
                                                                Tag.inserting(Component.text(playerName))))
                                                .resolver(TagResolver.resolver("entity",
                                                                Tag.inserting(Component.translatable(entity))))
                                                .build())
                                .append(MINI_MESSAGE.deserialize("<dark_gray> | </dark_gray>"))
                                .append(createTeleportComponent(target));

                Bukkit.getServer().broadcast(component);
        }

        /**
         * 广播命令违规警告（带位置）
         *
         * @param player   玩家对象（可为null）
         * @param command  命令内容
         * @param location 事件发生位置
         */
        public static void broadcastCommandWarning(Player player, String command, Object target) {
                String playerName = player != null ? player.getName() : "系统";
                String cooldownKey = "command:" + playerName + ":" + command;
                
                if (isOnCooldown(cooldownKey, WARNING_COOLDOWN)) {
                        return;
                }

                Component component = MINI_MESSAGE.deserialize(COMMAND_WARNING,
                                TagResolver.builder()
                                                .resolver(TagResolver.resolver("player",
                                                                Tag.inserting(Component.text(playerName))))
                                                .resolver(TagResolver.resolver("command",
                                                                Tag.inserting(Component.text(command))))
                                                .build())
                                .append(MINI_MESSAGE.deserialize("<dark_gray> | </dark_gray>"))
                                .append(createTeleportComponent(target));

                Bukkit.getServer().broadcast(component);
        }
}