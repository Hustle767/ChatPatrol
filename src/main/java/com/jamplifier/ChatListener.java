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

                // Log the player's UUID to the punished list in config, but avoid duplicates
                List<String> punished = plugin.getConfig().getStringList("punished");
                if (!punished.contains(player.getUniqueId().toString())) {
                    punished.add(player.getUniqueId().toString());
                    plugin.getConfig().set("punished", punished); // Save the updated punished list
                    plugin.saveConfig();
                }

                // Fetch the punishment command from the config
                String punishmentCommand = plugin.getConfig().getString("punishment-command");
                
                if (punishmentCommand != null && !punishmentCommand.isEmpty()) {
                    // Replace {player} with the player's name in the command
                    String finalCommand = punishmentCommand.replace("{player}", player.getName());

                    // Schedule the command to run on the main thread
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
                    });
                }
                return;
            }
        }
    }
}


