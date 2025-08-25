package org.encinet.noAnyBoom.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class ScanUtils {

    /**
     * 扫描指定位置周围半径内的违禁方块，并递归处理
     *
     * @param center 中心位置
     * @param radius 扫描半径
     * @param player 玩家对象（可为null）
     */
    public static void scanAndHandleBannedBlocks(Location center, int radius, Player player) {
        Map<Material, Integer> bannedBlocksFound = new HashMap<>();
        scanRecursive(center, radius, player, bannedBlocksFound);

        // 发送扫描汇总警告
        if (!bannedBlocksFound.isEmpty()) {
            broadcastScanSummary(player, bannedBlocksFound, center);
        }
    }

    /**
     * 广播扫描汇总警告
     *
     * @param player            玩家对象（可为null）
     * @param bannedBlocksFound 发现的违禁方块统计
     * @param center            扫描中心位置
     */
    private static void broadcastScanSummary(Player player, Map<Material, Integer> bannedBlocksFound, Location center) {
        String playerName = player != null ? player.getName() : "系统";

        StringBuilder summary = new StringBuilder();
        summary.append("<gradient:#ff5555:#aa0000>[<bold>⚠</bold> 扫描警告]</gradient> <white>");
        summary.append("玩家 <yellow>").append(playerName).append("</yellow> 附近发现违禁方块: ");

        boolean first = true;
        for (Map.Entry<Material, Integer> entry : bannedBlocksFound.entrySet()) {
            if (!first) {
                summary.append(", ");
            }
            summary.append("<yellow>").append(entry.getKey().name()).append("</yellow>×").append(entry.getValue());
            first = false;
        }

        summary.append("</white>");

        MiniMessage miniMessage = MiniMessage.miniMessage();
        Component component = miniMessage.deserialize(summary.toString())
                .append(miniMessage.deserialize("<dark_gray> | </dark_gray>"))
                .append(createTeleportComponent(center));

        org.bukkit.Bukkit.getServer().broadcast(component);
    }

    /**
     * 创建可点击的传送组件（支持坐标）
     */
    private static Component createTeleportComponent(Location location) {
        if (location == null) {
            return MiniMessage.miniMessage().deserialize("<gray>未知位置</gray>");
        }

        String worldName = location.getWorld() != null ? location.getWorld().getName() : "未知世界";
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        String tpCommand = String.format("/minecraft:tp @s %d %d %d", x, y, z);
        String coords = String.format("坐标 %s (%d,%d,%d)", worldName, x, y, z);

        return MiniMessage.miniMessage().deserialize(
                "<gradient:gold:yellow><hover:show_text:'<white>点击传送到该位置</white>'><click:run_command:'"
                        + tpCommand + "'>" + coords + "</click></hover></gradient>");
    }

    /**
     * 使用优化的BFS扫描并处理违禁方块，避免重复扫描和对象创建
     *
     * @param center            中心位置
     * @param radius            扫描半径
     * @param player            玩家对象（可为null）
     * @param bannedBlocksFound 发现的违禁方块统计
     */
    private static void scanRecursive(Location center, int radius, Player player,
            Map<Material, Integer> bannedBlocksFound) {
        World world = center.getWorld();
        if (world == null)
            return;

        // 使用队列进行BFS和集合记录已访问坐标（使用字符串键避免Location对象开销）
        Queue<Location> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        // 重用Location对象以减少内存分配
        Location tempLoc = center.clone();

        // 初始添加中心点周围的所有方块
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();

        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    tempLoc.setX(x);
                    tempLoc.setY(y);
                    tempLoc.setZ(z);
                    queue.add(tempLoc.clone());

                    // 预标记为已访问以避免重复入队
                    visited.add(x + "," + y + "," + z);
                }
            }
        }

        // BFS处理队列中的位置
        while (!queue.isEmpty()) {
            Location current = queue.poll();
            int currentX = current.getBlockX();
            int currentY = current.getBlockY();
            int currentZ = current.getBlockZ();

            Block block = current.getBlock();
            Material material = block.getType();

            // 检查是否为违禁方块
            if (BanUtils.isBannedMaterial(material)) {
                // 移除违禁方块
                block.setType(Material.AIR);
                // 统计发现的违禁方块
                bannedBlocksFound.put(material, bannedBlocksFound.getOrDefault(material, 0) + 1);

                // 添加周围5格内的方块到队列进行进一步扫描
                for (int dx = -5; dx <= 5; dx++) {
                    for (int dy = -5; dy <= 5; dy++) {
                        for (int dz = -5; dz <= 5; dz++) {
                            int nx = currentX + dx;
                            int ny = currentY + dy;
                            int nz = currentZ + dz;

                            String coordKey = nx + "," + ny + "," + nz;
                            if (!visited.contains(coordKey)) {
                                visited.add(coordKey);
                                Location neighbor = new Location(world, nx, ny, nz);
                                queue.add(neighbor);
                            }
                        }
                    }
                }
            }
        }
    }
}