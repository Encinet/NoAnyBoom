package org.encinet.noAnyBoom;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.encinet.noAnyBoom.utils.BanUtils;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class AcquisitionListener implements Listener {

    private void removeBannedItems(Inventory inventory) {
        ItemStack[] contents = inventory.getContents();
        for (int i = contents.length - 1; i >= 0; i--) {
            ItemStack item = contents[i];
            if (item != null && BanUtils.isBannedItem(item)) {
                inventory.remove(item);
            }
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        if (newItem != null && BanUtils.isBannedItem(newItem)) {
            player.getInventory().setItem(event.getNewSlot(), null);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        removeBannedItems(event.getPlayer().getInventory());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        removeBannedItems(event.getInventory());
        removeBannedItems(event.getPlayer().getInventory());
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        removeBannedItems(event.getInventory());
        removeBannedItems(event.getPlayer().getInventory());
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        String lower = message.toLowerCase();

        if (lower.contains("/give") || lower.contains("/minecraft:give")) {
            for (String banned : BanUtils.BANNED_MATERIAL_SET) {
                if (message.contains(banned)) {
                    event.setCancelled(true);
                    return;
                }
            }
        } else if (lower.contains("/summon") || lower.contains("/minecraft:summon")) {
            for (String banned : BanUtils.BANNED_ENTITY_SET) {
                if (lower.contains(banned)) {
                    event.setCancelled(true);
                    return;
                }
            }
        } else if (lower.contains("/setblock") || lower.contains("/minecraft:setblock") || lower.contains("/fill")
                || lower.contains("/minecraft:fill") || lower.contains("//")) {
            if (lower.contains("tnt")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // 只处理点击物品操作
        if (event.getAction() != InventoryAction.PICKUP_ALL &&
                event.getAction() != InventoryAction.PICKUP_HALF &&
                event.getAction() != InventoryAction.PICKUP_SOME &&
                event.getAction() != InventoryAction.PICKUP_ONE) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (item != null && BanUtils.isBannedItem(item)) {
            event.setCancelled(true);
            event.setCurrentItem(null);
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (BanUtils.isBannedItem(event.getCurrentItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        ItemStack item = event.getItem().getItemStack();
        if (item != null && BanUtils.isBannedItem(item)) {
            event.setCancelled(true);
            event.getItem().remove();
        }
    }

    @EventHandler
    public void onInventoryCreative(InventoryCreativeEvent event) {
        ItemStack item = event.getCursor();
        if (item != null && BanUtils.isBannedItem(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (item != null && BanUtils.isBannedItem(item)) {
            event.setCancelled(true); // 取消丢弃事件
            event.getPlayer().getInventory().remove(item); // 清除玩家物品栏中的违规物品
        }
    }
}
