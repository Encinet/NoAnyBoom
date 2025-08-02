package org.encinet.noAnyBoom;

import org.bukkit.plugin.java.JavaPlugin;

public final class NoAnyBoom extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new AcquisitionListener(), this);
        getServer().getPluginManager().registerEvents(new DestructionPreventionListener(), this);
        getServer().getPluginManager().registerEvents(new ExplosionListener(), this);
        getServer().getPluginManager().registerEvents(new UsageListener(), this);
        getLogger().info("NoAnyBoom Enable");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
