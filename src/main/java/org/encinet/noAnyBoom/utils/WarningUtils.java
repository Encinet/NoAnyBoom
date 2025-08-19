package org.encinet.noAnyBoom.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WarningUtils {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    
    // 使用 MiniMessage 格式的警告模板（美化颜色）
    private static final String ITEM_WARNING = "<gradient:red:dark_red>[<bold>⚠</bold> 警告]</gradient> <white>玩家 <yellow><player></yellow> 试图使用违禁物品 <yellow><item></yellow>";
    private static final String BLOCK_WARNING = "<gradient:red:dark_red>[<bold>⚠</bold> 警告]</gradient> <white>玩家 <yellow><player></yellow> 试图放置违禁方块 <yellow><block></yellow>";
    private static final String ENTITY_WARNING = "<gradient:red:dark_red>[<bold>⚠</bold> 警告]</gradient> <white>玩家 <yellow><player></yellow> 试图放置违禁实体 <yellow><entity></yellow>";
    private static final String COMMAND_WARNING = "<gradient:red:dark_red>[<bold>⚠</bold> 警告]</gradient> <white>玩家 <yellow><player></yellow> 使用了违禁命令 <yellow><command></yellow>";

    // 创建可点击的坐标组件（优化格式）
    private static Component createLocationComponent(Location location) {
        if (location == null) {
            return MINI_MESSAGE.deserialize("<gray>未知位置</gray>");
        }
        
        String worldName = location.getWorld() != null ? location.getWorld().getName() : "未知世界";
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        String tpCommand = String.format("/tp @s %d %d %d", x, y, z);
        String coords = String.format("Location %s (%d,%d,%d)", worldName, x, y, z);
        
        // 使用 MiniMessage 创建带点击和悬停事件的组件
        return MINI_MESSAGE.deserialize(
            "<gradient:gold:yellow><hover:show_text:'<gray>点击传送到该位置</gray>'><click:run_command:'" + tpCommand + "'>" + coords + "</click></hover></gradient>"
        );
    }

    /**
     * 广播物品违规警告（带位置）
     * @param player 玩家对象（可为null）
     * @param item 物品名称
     * @param location 事件发生位置
     */
    public static void broadcastItemWarning(Player player, String item, Location location) {
        String playerName = player != null ? player.getName() : "系统";
        
        Component component = MINI_MESSAGE.deserialize(ITEM_WARNING, 
            TagResolver.builder()
                .resolver(TagResolver.resolver("player", Tag.inserting(Component.text(playerName))))
                .resolver(TagResolver.resolver("item", Tag.inserting(Component.text(item))))
                .build()
        ).append(Component.space()).append(createLocationComponent(location));
        
        Bukkit.getServer().broadcast(component);
    }

    /**
     * 广播方块违规警告（带位置）
     * @param player 玩家对象（可为null）
     * @param block 方块名称
     * @param location 事件发生位置
     */
    public static void broadcastBlockWarning(Player player, String block, Location location) {
        String playerName = player != null ? player.getName() : "系统";
        
        Component component = MINI_MESSAGE.deserialize(BLOCK_WARNING, 
            TagResolver.builder()
                .resolver(TagResolver.resolver("player", Tag.inserting(Component.text(playerName))))
                .resolver(TagResolver.resolver("block", Tag.inserting(Component.text(block))))
                .build()
        ).append(Component.space()).append(createLocationComponent(location));
        
        Bukkit.getServer().broadcast(component);
    }

    /**
     * 广播实体违规警告（带位置）
     * @param player 玩家对象（可为null）
     * @param entity 实体名称
     * @param location 事件发生位置
     */
    public static void broadcastEntityWarning(Player player, String entity, Location location) {
        String playerName = player != null ? player.getName() : "系统";
        
        Component component = MINI_MESSAGE.deserialize(ENTITY_WARNING, 
            TagResolver.builder()
                .resolver(TagResolver.resolver("player", Tag.inserting(Component.text(playerName))))
                .resolver(TagResolver.resolver("entity", Tag.inserting(Component.text(entity))))
                .build()
        ).append(Component.space()).append(createLocationComponent(location));
        
        Bukkit.getServer().broadcast(component);
    }

    /**
     * 广播命令违规警告（带位置）
     * @param player 玩家对象（可为null）
     * @param command 命令内容
     * @param location 事件发生位置
     */
    public static void broadcastCommandWarning(Player player, String command, Location location) {
        String playerName = player != null ? player.getName() : "系统";
        
        Component component = MINI_MESSAGE.deserialize(COMMAND_WARNING, 
            TagResolver.builder()
                .resolver(TagResolver.resolver("player", Tag.inserting(Component.text(playerName))))
                .resolver(TagResolver.resolver("command", Tag.inserting(Component.text(command))))
                .build()
        ).append(Component.space()).append(createLocationComponent(location));
        
        Bukkit.getServer().broadcast(component);
    }
}