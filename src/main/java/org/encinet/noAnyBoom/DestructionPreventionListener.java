package org.encinet.noAnyBoom;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.encinet.noAnyBoom.utils.BanUtils;
import org.encinet.noAnyBoom.utils.WarningUtils;

public class DestructionPreventionListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Material blockType = event.getBlock().getType();
        if (BanUtils.isBannedMaterial(blockType)) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            WarningUtils.broadcastBlockWarning(event.getPlayer(), blockType.name(), event.getPlayer());
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        EntityType entityType = event.getEntity().getType();
        if (BanUtils.isBannedEntity(entityType)) {
            event.setCancelled(true);
            event.getEntity().remove();
            
            // 尝试获取造成伤害的玩家
            Player damager = null;
            if (event.getEntity() instanceof Player) {
                damager = (Player) event.getEntity();
            }
            WarningUtils.broadcastEntityWarning(damager, entityType.name(), damager != null ? damager : event.getEntity().getLocation());
        }
    }
}
