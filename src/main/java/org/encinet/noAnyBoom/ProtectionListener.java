package org.encinet.noAnyBoom;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.encinet.noAnyBoom.utils.BanUtils;
import org.encinet.noAnyBoom.utils.ScanUtils;
import org.encinet.noAnyBoom.utils.WarningUtils;

public class ProtectionListener implements Listener {

    // ExplosionListener
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        EntityType type = event.getEntityType();
        if (BanUtils.isBannedEntity(type)) {
            event.setCancelled(true);
            WarningUtils.broadcast("exploded", null, type.name(), event.getEntity().getLocation());
            ScanUtils.scanAndHandleBannedBlocks(event.getEntity().getLocation(), 5, null);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntitySpawn(EntitySpawnEvent event) {
        EntityType type = event.getEntityType();
        if (BanUtils.isBannedEntity(type)) {
            event.setCancelled(true);
            WarningUtils.broadcast("spawned", null, type.name(), event.getEntity().getLocation());
            ScanUtils.scanAndHandleBannedBlocks(event.getEntity().getLocation(), 5, null);
        }
    }

    // FireListener
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
        WarningUtils.broadcast("burned", null, event.getBlock().getType().name(), event.getBlock().getLocation());
    }

    // UsageListener
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Material material = event.getBlockPlaced().getType();
        if (BanUtils.isBannedMaterial(material)) {
            event.setCancelled(true);
            WarningUtils.broadcast("placed", player, material.name(), player.getLocation());
            removeBannedItemsFromPlayer(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityPlace(EntityPlaceEvent event) {
        Player player = event.getPlayer();
        EntityType entityType = event.getEntity().getType();
        if (BanUtils.isBannedEntity(entityType)) {
            event.setCancelled(true);
            if (player != null) {
                WarningUtils.broadcast("placed", player, entityType.name(), player.getLocation());
                removeBannedItemsFromPlayer(player);
            } else {
                WarningUtils.broadcast("placed", null, entityType.name(), event.getEntity().getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.setCancelled(true);
        WarningUtils.broadcast("exploded", null, event.getBlock().getType().name(), event.getBlock().getLocation());
        ScanUtils.scanAndHandleBannedBlocks(event.getBlock().getLocation(), 5, null);
    }

    // AcquisitionListener
    private void removeBannedItems(Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (BanUtils.isBannedItem(item)) {
                inventory.setItem(i, null);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        if (BanUtils.isBannedItem(newItem)) {
            player.getInventory().setItem(event.getNewSlot(), null);
            WarningUtils.broadcast("held", player, newItem.getType().name(), player.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        removeBannedItems(event.getPlayer().getInventory());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        removeBannedItems(event.getInventory());
        removeBannedItems(event.getPlayer().getInventory());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        removeBannedItems(event.getInventory());
        removeBannedItems(event.getPlayer().getInventory());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase();
        for (String banned : BanUtils.BANNED_MATERIAL_SET) {
            if (message.contains(banned.toLowerCase())) {
                event.setCancelled(true);
                WarningUtils.broadcast("used command", event.getPlayer(), event.getMessage(), event.getPlayer().getLocation());
                return;
            }
        }
        for (String banned : BanUtils.BANNED_ENTITY_SET) {
            if (message.contains(banned.toLowerCase())) {
                event.setCancelled(true);
                WarningUtils.broadcast("used command", event.getPlayer(), event.getMessage(), event.getPlayer().getLocation());
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (BanUtils.isBannedItem(item)) {
            event.setCancelled(true);
            event.setCurrentItem(null);
            if (event.getWhoClicked() instanceof Player player) {
                WarningUtils.broadcast("clicked", player, item.getType().name(), player.getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCraftItem(CraftItemEvent event) {
        ItemStack result = event.getRecipe().getResult();
        if (BanUtils.isBannedItem(result)) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                WarningUtils.broadcast("crafted", player, result.getType().name(), player.getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack item = event.getItem().getItemStack();
            if (BanUtils.isBannedItem(item)) {
                event.setCancelled(true);
                event.getItem().remove();
                WarningUtils.broadcast("picked up", player, item.getType().name(), player.getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryCreative(InventoryCreativeEvent event) {
        ItemStack item = event.getCursor();
        if (BanUtils.isBannedItem(item)) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                WarningUtils.broadcast("used creative", player, item.getType().name(), player.getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (BanUtils.isBannedItem(item)) {
            event.setCancelled(true);
            event.getPlayer().getInventory().remove(item);
            WarningUtils.broadcast("dropped", event.getPlayer(), item.getType().name(), event.getPlayer().getLocation());
        }
    }

    // DestructionPreventionListener
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Material blockType = event.getBlock().getType();
        if (BanUtils.isBannedMaterial(blockType)) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            WarningUtils.broadcast("broke", event.getPlayer(), blockType.name(), event.getBlock().getLocation());
            ScanUtils.scanAndHandleBannedBlocks(event.getBlock().getLocation(), 5, event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        EntityType entityType = event.getEntity().getType();
        if (BanUtils.isBannedEntity(entityType)) {
            event.setCancelled(true);
            event.getEntity().remove();
            Player damager = null;
            if (event instanceof EntityDamageByEntityEvent damageEvent) {
                if (damageEvent.getDamager() instanceof Player) {
                    damager = (Player) damageEvent.getDamager();
                }
            }
            WarningUtils.broadcast("damaged", damager, entityType.name(), event.getEntity().getLocation());
        }
    }

    // New Listeners
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockDispense(BlockDispenseEvent event) {
        if (BanUtils.isBannedItem(event.getItem())) {
            event.setCancelled(true);
            WarningUtils.broadcast("dispensed", null, event.getItem().getType().name(), event.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (BanUtils.isBannedItem(item)) {
            event.setCancelled(true);
            WarningUtils.broadcast("interacted with", event.getPlayer(), item.getType().name(), event.getPlayer().getLocation());
        }
    }

    private void removeBannedItemsFromPlayer(Player player) {
        player.getInventory().setItemInMainHand(null);
        player.getInventory().setItemInOffHand(null);
    }
}
