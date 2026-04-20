package org.encinet.noAnyBoom;

import org.bukkit.plugin.java.JavaPlugin;

public final class NoAnyBoom extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new ProtectionListener(), this);

        NabCommand.register(getLifecycleManager());

        getLogger().info("NoAnyBoom Enable");
    }
}