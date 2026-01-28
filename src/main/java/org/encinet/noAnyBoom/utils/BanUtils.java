package org.encinet.noAnyBoom.utils;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class BanUtils {
    public static final Set<String> BANNED_MATERIAL_SET = Set.of("tnt", "tnt_minecart", "end_crystal");
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
                material == Material.END_CRYSTAL;
    }

    public static boolean isBannedEntity(EntityType entityType) {
        return entityType == EntityType.TNT ||
                entityType == EntityType.TNT_MINECART ||
                entityType == EntityType.END_CRYSTAL ||
                entityType == EntityType.FIREBALL ||
                entityType == EntityType.SMALL_FIREBALL ||
                entityType == EntityType.WITHER ||
                entityType == EntityType.WITHER_SKULL ||
                entityType == EntityType.ENDER_DRAGON ||
                entityType == EntityType.DRAGON_FIREBALL;
    }
}
