package org.encinet.noAnyBoom.utils;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class BanUtils {
    public static final Set<String> BANNED_MATERIAL_SET = Set.of("tnt", "tnt_minecart", "end_crystal",
            "witch_spawn_egg");
    public static final Set<String> BANNED_ENTITY_SET = Set.of(
            "tnt",
            "tnt_minecart",
            "end_crystal",
            "fireball",
            "small_fireball",
            "wither",
            "wither_skull",
            "ender_dragon",
            "dragon_fireball");

    public static boolean isBannedItem(ItemStack item) {
        if (item == null) {
            return false;
        }
        Material type = item.getType();
        return isBannedMaterial(type);
    }

    public static boolean isBannedMaterial(Material material) {
        return material == Material.TNT ||
                material == Material.TNT_MINECART ||
                material == Material.END_CRYSTAL ||
                material == Material.WITCH_SPAWN_EGG;
    }

    public static boolean isBannedEntity(EntityType entityType) {
        return entityType == EntityType.PRIMED_TNT ||
                entityType == EntityType.MINECART_TNT ||
                entityType == EntityType.ENDER_CRYSTAL ||
                entityType == EntityType.FIREBALL ||
                entityType == EntityType.SMALL_FIREBALL ||
                entityType == EntityType.WITHER ||
                entityType == EntityType.WITHER_SKULL ||
                entityType == EntityType.ENDER_DRAGON ||
                entityType == EntityType.DRAGON_FIREBALL;
    }
}
