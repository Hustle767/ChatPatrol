package com.jamplifier;

import org.bukkit.plugin.java.JavaPlugin;

public class ChatPatrol extends JavaPlugin {

    @Override
    public void onEnable() {
        // Save default config if it doesn't exist
        saveDefaultConfig();

        // Register events and commands
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        this.getCommand("chatpatrol").setExecutor(new ChatPatrolCommand(this));

        getLogger().info("ChatPatrol 1.3 Plugin Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ChatPatrol 1.3 Plugin Disabled!");
    }

    // Method to reload config
    public void reloadChatPatrolConfig() {
        reloadConfig();
        getLogger().info("ChatPatrol Config Reloaded!");
    }
}
