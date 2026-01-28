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
        scanRecursive(center, radius, bannedBlocksFound);

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
     * 高性能BFS扫描：初始扫描radius范围，发现违禁方块后无限扩散清理
     *
     * @param center            中心位置
     * @param radius            初始扫描半径
     * @param bannedBlocksFound 发现的违禁方块统计
     */
    private static void scanRecursive(Location center, int radius, Map<Material, Integer> bannedBlocksFound) {
        World world = center.getWorld();
        if (world == null) return;

        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();

        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight() - 1;
        int radiusSquared = radius * radius;

        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();

        // 扫描radius球形范围
        for (int y = Math.max(minY, centerY - radius); y <= Math.min(maxY, centerY + radius); y++) {
            int dy = y - centerY;
            int dySquared = dy * dy;

            for (int x = centerX - radius; x <= centerX + radius; x++) {
                int dx = x - centerX;
                int dxSquared = dx * dx;
                int remainingSquared = radiusSquared - dxSquared - dySquared;

                if (remainingSquared < 0) continue;

                int zRange = (int) Math.sqrt(remainingSquared);

                for (int z = centerZ - zRange; z <= centerZ + zRange; z++) {
                    long pos = encodePosition(x, y, z);
                    if (visited.add(pos)) {
                        queue.add(pos);
                    }
                }
            }
        }

        // 扩散半径
        final int SPREAD_RADIUS = 5;
        int[][] directions = generateSphereOffsets(SPREAD_RADIUS);

        // BFS处理：无限扩散
        while (!queue.isEmpty()) {
            long current = queue.poll();
            int[] coords = decodePosition(current);
            int x = coords[0];
            int y = coords[1];
            int z = coords[2];

            Block block = world.getBlockAt(x, y, z);
            Material material = block.getType();

            if (BanUtils.isBannedMaterial(material)) {
                block.setType(Material.AIR);
                bannedBlocksFound.merge(material, 1, Integer::sum);

                // 扩散到周围方块
                for (int[] offset : directions) {
                    int nx = x + offset[0];
                    int ny = y + offset[1];
                    int nz = z + offset[2];

                    // 仅检查世界高度边界
                    if (ny < minY || ny > maxY) continue;

                    long neighborPos = encodePosition(nx, ny, nz);
                    if (visited.add(neighborPos)) {
                        queue.add(neighborPos);
                    }
                }
            }
        }
    }

    /**
     * 生成球形范围内的所有偏移坐标
     */
    private static int[][] generateSphereOffsets(int radius) {
        java.util.List<int[]> offsets = new java.util.ArrayList<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    if (dx * dx + dy * dy + dz * dz <= radius * radius) {
                        offsets.add(new int[]{dx, dy, dz});
                    }
                }
            }
        }

        return offsets.toArray(new int[0][]);
    }

    /**
     * 坐标编码为long
     */
    private static long encodePosition(int x, int y, int z) {
        return ((long) (x + 33554432) & 0x3FFFFFFL) << 38
                | ((long) (y + 2048) & 0xFFFL) << 26
                | ((long) (z + 33554432) & 0x3FFFFFFL);
    }

    /**
     * long解码为坐标
     */
    private static int[] decodePosition(long encoded) {
        int x = (int) ((encoded >> 38) & 0x3FFFFFFL) - 33554432;
        int y = (int) ((encoded >> 26) & 0xFFFL) - 2048;
        int z = (int) (encoded & 0x3FFFFFFL) - 33554432;
        return new int[]{x, y, z};
    }
}