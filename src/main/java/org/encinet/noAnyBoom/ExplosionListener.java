package org.encinet.noAnyBoom;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.encinet.noAnyBoom.utils.BanUtils;
import org.encinet.noAnyBoom.utils.ScanUtils;
import org.encinet.noAnyBoom.utils.WarningUtils;

public class ExplosionListener implements Listener {

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        EntityType type = event.getEntityType();
        if (BanUtils.isBannedEntity(type)) {
            event.setCancelled(true);
            Location entityLocation = event.getEntity().getLocation();
            WarningUtils.broadcastEntityWarning(null, type.name(), event.getEntity());
            
            // 扫描周围半径5格内的违禁方块
            ScanUtils.scanAndHandleBannedBlocks(entityLocation, 5, null);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        EntityType type = event.getEntityType();
        if (BanUtils.isBannedEntity(type)) {
            event.setCancelled(true);
            // 生成事件没有玩家对象，使用控制台名
            WarningUtils.broadcastEntityWarning(null, type.name(), event.getEntity());
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        EntityType type = event.getEntityType();
        if (BanUtils.isBannedEntity(type)) {
            event.setCancelled(true);
            Location entityLocation = event.getEntity().getLocation();
            WarningUtils.broadcastEntityWarning(null, type.name(), event.getEntity());
            
            // 扫描周围半径5格内的违禁方块
            ScanUtils.scanAndHandleBannedBlocks(entityLocation, 5, null);
        }
    }
}
