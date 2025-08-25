package org.encinet.noAnyBoom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.encinet.noAnyBoom.utils.BanUtils;
import org.encinet.noAnyBoom.utils.ScanUtils;
import org.encinet.noAnyBoom.utils.WarningUtils;

public class UsageListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Material material = event.getBlockPlaced().getType();
        if (BanUtils.isBannedMaterial(material)) {
            event.setCancelled(true);
            WarningUtils.broadcastBlockWarning(player, material.name(), player);

            ItemStack main = player.getInventory().getItemInMainHand();
            ItemStack off = player.getInventory().getItemInOffHand();
            if (main != null && BanUtils.isBannedMaterial(main.getType())) {
                player.getInventory().setItemInMainHand(null);
            }
            if (off != null && BanUtils.isBannedMaterial(off.getType())) {
                player.getInventory().setItemInOffHand(null);
            }
        }
    }

    @EventHandler
    public void onEntityPlace(EntityPlaceEvent event) {
        Player player = event.getPlayer();
        EntityType entityType = event.getEntity().getType();
        if (BanUtils.isBannedEntity(entityType)) {
            event.setCancelled(true);
            WarningUtils.broadcastEntityWarning(player, entityType.name(), player);

            ItemStack main = player.getInventory().getItemInMainHand();
            ItemStack off = player.getInventory().getItemInOffHand();
            if (main != null && BanUtils.isBannedMaterial(main.getType())) {
                player.getInventory().setItemInMainHand(null);
            }
            if (off != null && BanUtils.isBannedMaterial(off.getType())) {
                player.getInventory().setItemInOffHand(null);
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.setCancelled(true);
        WarningUtils.broadcastBlockWarning(null, event.getBlock().getType().name(), event.getBlock());
        // 扫描周围半径5格内的违禁方块
        ScanUtils.scanAndHandleBannedBlocks(event.getBlock().getLocation(), 5, null);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.setCancelled(true);
        Location entityLocation = event.getEntity().getLocation();
        WarningUtils.broadcastEntityWarning(null, event.getEntityType().name(), event.getEntity());

        // 扫描周围半径5格内的违禁方块
        ScanUtils.scanAndHandleBannedBlocks(entityLocation, 5, null);
    }
}
