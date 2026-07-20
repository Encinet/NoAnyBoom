package org.encinet.noAnyBoom;

import org.bukkit.ExplosionResult;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.encinet.noAnyBoom.utils.ProtectionRules;
import org.encinet.noAnyBoom.utils.WarningUtils;

public class ProtectionListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        EntityType type = event.getEntityType();
        if (!ProtectionRules.isSpawnBlocked(type)) {
            return;
        }

        event.setCancelled(true);
        WarningUtils.log("spawn blocked", "Environment", type.name(), event.getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        EntityType type = event.getEntityType();
        if (!ProtectionRules.isExplosionBlocked(type)) {
            return;
        }

        event.setCancelled(true);
        WarningUtils.log("explosion blocked", "Environment", type.name(), event.getEntity().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        EntityType type = event.getEntityType();
        if (!ProtectionRules.isExplosionBlocked(type)) {
            return;
        }

        event.setCancelled(true);
        WarningUtils.log("explosion blocked", "Environment", type.name(), event.getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (isNonDestructiveWindBurst(event)) {
            return;
        }

        event.setCancelled(true);
        Material source = event.getExplodedBlockState().getType();
        WarningUtils.log("explosion blocked", "Environment", source.name(), event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        Material source = event.getSource().getType();
        Material spread = event.getNewState().getType();
        if (!isFire(source) && !isFire(spread)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTNTPrime(TNTPrimeEvent event) {
        event.setCancelled(true);

        if (event.getPrimingEntity() instanceof Player player) {
            WarningUtils.log("priming blocked", player, Material.TNT.name(), event.getBlock().getLocation());
            return;
        }

        WarningUtils.log("priming blocked", event.getCause().name(), Material.TNT.name(), event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.END_CRYSTAL) {
            event.setCancelled(true);
            event.getEntity().remove();
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            event.setCancelled(true);
        }
    }

    private boolean isNonDestructiveWindBurst(BlockExplodeEvent event) {
        ExplosionResult result = event.getExplosionResult();
        return event.getExplodedBlockState().getType() == Material.AIR
                && (result == ExplosionResult.KEEP || result == ExplosionResult.TRIGGER_BLOCK);
    }

    private boolean isFire(Material material) {
        return material == Material.FIRE || material == Material.SOUL_FIRE;
    }
}
