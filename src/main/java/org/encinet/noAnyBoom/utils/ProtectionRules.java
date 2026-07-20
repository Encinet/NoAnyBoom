package org.encinet.noAnyBoom.utils;

import org.bukkit.entity.EntityType;

import java.util.EnumSet;
import java.util.Set;

public final class ProtectionRules {

    private static final Set<EntityType> BLOCKED_SPAWNS = EnumSet.of(
            EntityType.TNT,
            EntityType.FIREBALL,
            EntityType.SMALL_FIREBALL,
            EntityType.WITHER,
            EntityType.WITHER_SKULL,
            EntityType.ENDER_DRAGON,
            EntityType.DRAGON_FIREBALL
    );

    private static final Set<EntityType> ALLOWED_NON_DESTRUCTIVE_EXPLOSIONS = EnumSet.of(
            EntityType.WIND_CHARGE,
            EntityType.BREEZE_WIND_CHARGE
    );

    private ProtectionRules() {
    }

    public static boolean isSpawnBlocked(EntityType entityType) {
        return BLOCKED_SPAWNS.contains(entityType);
    }

    public static boolean isExplosionBlocked(EntityType entityType) {
        return !ALLOWED_NON_DESTRUCTIVE_EXPLOSIONS.contains(entityType);
    }
}
