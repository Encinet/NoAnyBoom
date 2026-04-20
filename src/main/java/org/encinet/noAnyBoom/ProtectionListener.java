package org.encinet.noAnyBoom;

import org.bukkit.ExplosionResult;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Arrays;
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
import org.encinet.noAnyBoom.utils.ViolationTracker;
import org.encinet.noAnyBoom.utils.WarningUtils;

public class ProtectionListener implements Listener {

    private static final String[] DANGEROUS_COMMAND_PREFIXES = {
            "/give", "/summon", "/setblock", "/fill",
            "/minecraft:give", "/minecraft:summon", "/minecraft:setblock", "/minecraft:fill"
    };

    // ExplosionListener
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        EntityType type = event.getEntityType();
        if (BanUtils.isBannedEntity(type)) {
            event.setCancelled(true);
            Player damager = event.getEntity() instanceof Player ? (Player) event.getEntity() : null;
            WarningUtils.broadcast("exploded", "Environment", type.name(), event.getEntity().getLocation());
            ScanUtils.scanAndHandleBannedBlocks(event.getEntity().getLocation(), 5, damager);
            if (damager != null) {
                ViolationTracker.record(damager);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntitySpawn(EntitySpawnEvent event) {
        EntityType type = event.getEntityType();
        if (BanUtils.isBannedEntity(type)) {
            event.setCancelled(true);
            Player cause = event.getEntity() instanceof Player ? (Player) event.getEntity() : null;
            WarningUtils.broadcast("spawned", cause != null ? cause : "Environment", type.name(), event.getEntity().getLocation());
            ScanUtils.scanAndHandleBannedBlocks(event.getEntity().getLocation(), 5, cause);
            if (cause != null) {
                ViolationTracker.record(cause);
            }
        }
    }

    // FireListener
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBurn(BlockBurnEvent event) {
        if (BanUtils.isBannedMaterial(event.getBlock().getType())) {
            event.setCancelled(true);
            WarningUtils.broadcast("burned", "Environment", event.getBlock().getType().name(), event.getBlock().getLocation());
        }
    }

    // UsageListener
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Material material = event.getBlockPlaced().getType();
        if (BanUtils.isBannedMaterial(material)) {
            event.setCancelled(true);
            recordViolation(player, "placed", material.name(), player.getLocation());
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
                recordViolation(player, "placed", entityType.name(), player.getLocation());
                removeBannedItemsFromPlayer(player);
            } else {
                WarningUtils.broadcast("placed", "Environment", entityType.name(), event.getEntity().getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockExplode(BlockExplodeEvent event) {
        ExplosionResult explosionResult = event.getExplosionResult();
        if (event.getBlock().getType() == Material.AIR && (explosionResult == ExplosionResult.KEEP || explosionResult == ExplosionResult.TRIGGER_BLOCK)) return;

        Player cause = null;
        if (event.getBlock().getType() == Material.TNT) {
            for (Entity e : event.getBlock().getWorld().getNearbyEntities(event.getBlock().getLocation(), 2, 2, 2)) {
                if (e instanceof Player) {
                    cause = (Player) e;
                    break;
                }
            }
        }

        event.setCancelled(true);
        WarningUtils.broadcast("exploded", cause != null ? cause : "Environment", event.getBlock().getType().name(), event.getBlock().getLocation());
        ScanUtils.scanAndHandleBannedBlocks(event.getBlock().getLocation(), 5, cause);
        if (cause != null) {
            ViolationTracker.record(cause);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTNTPrime(TNTPrimeEvent event) {
        event.setCancelled(true);
        if (event.getPrimingEntity() instanceof Player player) {
            recordViolation(player, "primed", "TNT", event.getBlock().getLocation());
            ScanUtils.scanAndHandleBannedBlocks(event.getBlock().getLocation(), 5, player);
        } else {
            WarningUtils.broadcast("primed", event.getCause().name(), "TNT", event.getBlock().getLocation());
            ScanUtils.scanAndHandleBannedBlocks(event.getBlock().getLocation(), 5, null);
        }
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
            recordViolation(player, "held", newItem.getType().name(), player.getLocation());
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
        if (!isDangerousCommand(message)) {
            return;
        }

        for (String banned : BanUtils.BANNED_MATERIAL_SET) {
            if (message.contains(banned.toLowerCase())) {
                event.setCancelled(true);
                recordViolation(event.getPlayer(), "used command", event.getMessage(), event.getPlayer().getLocation());
                return;
            }
        }
        for (String banned : BanUtils.BANNED_ENTITY_SET) {
            if (message.contains(banned.toLowerCase())) {
                event.setCancelled(true);
                recordViolation(event.getPlayer(), "used command", event.getMessage(), event.getPlayer().getLocation());
                return;
            }
        }
    }

    private boolean isDangerousCommand(String command) {
        if (command.startsWith("//")) return true;
        for (String prefix : DANGEROUS_COMMAND_PREFIXES) {
            if (command.startsWith(prefix)) return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (BanUtils.isBannedItem(item)) {
            event.setCancelled(true);
            event.setCurrentItem(null);
            if (event.getWhoClicked() instanceof Player player) {
                recordViolation(player, "clicked", item.getType().name(), player.getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCraftItem(CraftItemEvent event) {
        ItemStack result = event.getRecipe().getResult();
        if (BanUtils.isBannedItem(result)) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                recordViolation(player, "crafted", result.getType().name(), player.getLocation());
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
                recordViolation(player, "picked up", item.getType().name(), player.getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryCreative(InventoryCreativeEvent event) {
        ItemStack item = event.getCursor();
        if (BanUtils.isBannedItem(item)) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                recordViolation(player, "used creative", item.getType().name(), player.getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (BanUtils.isBannedItem(item)) {
            event.setCancelled(true);
            event.getPlayer().getInventory().remove(item);
            recordViolation(event.getPlayer(), "dropped", item.getType().name(), event.getPlayer().getLocation());
        }
    }

    // DestructionPreventionListener
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Material blockType = event.getBlock().getType();
        if (BanUtils.isBannedMaterial(blockType)) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            recordViolation(event.getPlayer(), "broke", blockType.name(), event.getBlock().getLocation());
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
            WarningUtils.broadcast("damaged", damager != null ? damager : "Environment", entityType.name(), event.getEntity().getLocation());
            if (damager != null) {
                ViolationTracker.record(damager);
            }
        }
    }

    // New Listeners
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockDispense(BlockDispenseEvent event) {
        if (BanUtils.isBannedItem(event.getItem())) {
            event.setCancelled(true);
            Player dispenserPlayer = null;
            for (Entity e : event.getBlock().getWorld().getNearbyEntities(event.getBlock().getLocation(), 3, 3, 3)) {
                if (e instanceof Player) {
                    dispenserPlayer = (Player) e;
                    break;
                }
            }
            WarningUtils.broadcast("dispenser tried to dispense", dispenserPlayer != null ? dispenserPlayer : "Dispenser", event.getItem().getType().name(), event.getBlock().getLocation());
            if (dispenserPlayer != null) {
                ViolationTracker.record(dispenserPlayer);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (BanUtils.isBannedItem(item)) {
            event.setCancelled(true);
            recordViolation(event.getPlayer(), "interacted with", item.getType().name(), event.getPlayer().getLocation());
        }
    }

    private void removeBannedItemsFromPlayer(Player player) {
        player.getInventory().setItemInMainHand(null);
        player.getInventory().setItemInOffHand(null);
    }

    private void recordViolation(Player player, String action, String target, Location loc) {
        WarningUtils.broadcast(action, player, target, loc);
        ViolationTracker.record(player);
    }
}
