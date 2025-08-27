package org.encinet.noAnyBoom;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.encinet.noAnyBoom.utils.WarningUtils;

public class FireListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
        // 方块燃烧事件没有玩家对象，使用null
        WarningUtils.broadcastBlockWarning(null, event.getBlock().getType().name(), event.getBlock());
    }
}
