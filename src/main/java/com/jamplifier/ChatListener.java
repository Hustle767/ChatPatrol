package com.jamplifier;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

public class ChatListener implements Listener {

    private final ChatPatrol plugin;

    public ChatListener(ChatPatrol plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().toLowerCase();

        // Get blacklisted words from the config
        List<String> blacklistedWords = plugin.getConfig().getStringList("blacklisted-words");

        for (String word : blacklistedWords) {
            if (message.contains(word)) {
                event.setCancelled(true); // Prevent message from being sent

                // Log the player's UUID to the punished list in config
                List<String> punished = plugin.getConfig().getStringList("punished");
                punished.add(player.getUniqueId().toString());
                plugin.getConfig().set("punished", punished); // Save the updated punished list
                plugin.saveConfig();

                // Schedule the ban command to run on the main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban " + player.getName() + " You were banned for using inappropriate language!");
                });

                return;
            }
        }
    }
}


