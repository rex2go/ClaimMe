package com.rex2go.claimme.listener;

import com.rex2go.claimme.ClaimMe;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

public abstract class AbstractListener implements Listener {

    protected ClaimMe plugin;

    public AbstractListener() {
        plugin = ClaimMe.getInstance();
        PluginManager pluginManager = plugin.getServer().getPluginManager();

        pluginManager.registerEvents(this, plugin);
    }
}