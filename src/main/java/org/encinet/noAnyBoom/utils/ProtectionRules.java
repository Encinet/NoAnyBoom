package org.encinet.noAnyBoom.utils;

import org.bukkit.entity.EntityType;

import java.util.EnumSet;
import java.util.Set;

public final class ProtectionRules {

    private static final Set<EntityType> BLOCKED_SPAWNS = EnumSet.of(
            EntityType.WITHER,
            EntityType.ENDER_DRAGON
    );

    private ProtectionRules() {
    }

    public static boolean isSpawnBlocked(EntityType entityType) {
        return BLOCKED_SPAWNS.contains(entityType);
    }
}
