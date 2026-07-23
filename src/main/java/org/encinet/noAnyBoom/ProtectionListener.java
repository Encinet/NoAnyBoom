package org.encinet.noAnyBoom;

import org.bukkit.ExplosionResult;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Bed;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.tag.DamageTypeTags;
import org.encinet.noAnyBoom.utils.ProtectionRules;

public class ProtectionListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        EntityType type = event.getEntityType();
        if (!ProtectionRules.isSpawnBlocked(type)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        event.setFire(false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (destroysBlocks(event.getExplosionResult())) {
            event.blockList().clear();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!event.isCancelled() && destroysBlocks(event.getExplosionResult())) {
            event.blockList().clear();
        }

        restoreExplodedSource(event.getExplodedBlockState());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getCause() == BlockIgniteEvent.IgniteCause.EXPLOSION) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        EntityDamageEvent.DamageCause cause = event.getCause();
        DamageType damageType = event.getDamageSource().getDamageType();
        if (cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                || isExplosionDamage(damageType)) {
            event.setDamage(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if (event.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION) {
            event.setCancelled(true);
        }
    }

    private boolean destroysBlocks(ExplosionResult result) {
        return result == ExplosionResult.DESTROY || result == ExplosionResult.DESTROY_WITH_DECAY;
    }

    private boolean isExplosionDamage(DamageType damageType) {
        return (DamageTypeTags.IS_EXPLOSION != null && DamageTypeTags.IS_EXPLOSION.isTagged(damageType))
                || damageType.equals(DamageType.EXPLOSION)
                || damageType.equals(DamageType.PLAYER_EXPLOSION)
                || damageType.equals(DamageType.BAD_RESPAWN_POINT)
                || damageType.equals(DamageType.FIREWORKS)
                || damageType.equals(DamageType.WIND_CHARGE);
    }

    private void restoreExplodedSource(BlockState source) {
        Block sourceBlock = source.getBlock();
        if (!sourceBlock.getType().isAir()) {
            return;
        }

        Material material = source.getType();
        if (material == Material.RESPAWN_ANCHOR) {
            source.update(true, false);
            return;
        }

        if (!Tag.BEDS.isTagged(material) || !(source.getBlockData() instanceof Bed bed)) {
            return;
        }

        Block otherHalf = sourceBlock.getRelative(
                bed.getPart() == Bed.Part.HEAD ? bed.getFacing().getOppositeFace() : bed.getFacing()
        );
        if (!otherHalf.getType().isAir()) {
            return;
        }

        bed.setOccupied(false);
        Bed otherData = (Bed) bed.clone();
        otherData.setPart(bed.getPart() == Bed.Part.HEAD ? Bed.Part.FOOT : Bed.Part.HEAD);

        source.setBlockData(bed);
        source.update(true, false);
        otherHalf.setBlockData(otherData, false);
    }
}
