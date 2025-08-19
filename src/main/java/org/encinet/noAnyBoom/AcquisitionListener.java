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
import org.encinet.noAnyBoom.utils.WarningUtils;
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
            WarningUtils.broadcastItemWarning(player, newItem.getType().name(), player.getLocation());
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
        for (String banned : BanUtils.BANNED_MATERIAL_SET) {
            if (message.contains(banned)) {
                event.setCancelled(true);
                WarningUtils.broadcastCommandWarning(event.getPlayer(), message, event.getPlayer().getLocation());
                return;
            }
        }
        for (String banned : BanUtils.BANNED_ENTITY_SET) {
            if (message.contains(banned)) {
                event.setCancelled(true);
                WarningUtils.broadcastCommandWarning(event.getPlayer(), message, event.getPlayer().getLocation());
                return;
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

            if (event.getWhoClicked() instanceof Player) {
                Player player = (Player) event.getWhoClicked();
                WarningUtils.broadcastItemWarning(player, item.getType().name(), event.getWhoClicked().getLocation());
            }
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        ItemStack result = event.getCurrentItem();
        if (result != null && BanUtils.isBannedItem(result)) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            WarningUtils.broadcastItemWarning(player, result.getType().name(), event.getWhoClicked().getLocation());
        }
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        ItemStack item = event.getItem().getItemStack();
        if (item != null && BanUtils.isBannedItem(item)) {
            event.setCancelled(true);
            event.getItem().remove();
            WarningUtils.broadcastItemWarning(player, item.getType().name(), event.getItem().getLocation());
        }
    }

    @EventHandler
    public void onInventoryCreative(InventoryCreativeEvent event) {
        ItemStack item = event.getCursor();
        if (item != null && BanUtils.isBannedItem(item)) {
            event.setCancelled(true);

            if (event.getWhoClicked() instanceof Player) {
                Player player = (Player) event.getWhoClicked();
                WarningUtils.broadcastItemWarning(player, item.getType().name(), event.getWhoClicked().getLocation());
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (item != null && BanUtils.isBannedItem(item)) {
            event.setCancelled(true); // 取消丢弃事件
            event.getPlayer().getInventory().remove(item); // 清除玩家物品栏中的违规物品
            WarningUtils.broadcastItemWarning(event.getPlayer(), item.getType().name(), event.getItemDrop().getLocation());
        }
    }
}
