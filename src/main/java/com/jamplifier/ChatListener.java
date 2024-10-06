package com.jamplifier;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ChatListener implements Listener {

    private final ChatPatrol plugin;
    private final HashMap<UUID, Long> lastMessageTime = new HashMap<>();
    private final HashMap<UUID, Integer> messageCount = new HashMap<>();

    public ChatListener(ChatPatrol plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().toLowerCase();
        UUID playerUUID = player.getUniqueId();

        // Check for blacklisted words
        checkForBlacklistedWords(event, player, message);

        // Check for spam (if enabled in config)
        if (plugin.getConfig().getBoolean("enable-spam-filter")) {
            checkForSpam(event, player, playerUUID);
        }
    }

    private void checkForBlacklistedWords(AsyncPlayerChatEvent event, Player player, String message) {
        // Get blacklisted words from the config
        List<String> blacklistedWords = plugin.getConfig().getStringList("blacklisted-words");

        for (String word : blacklistedWords) {
            if (message.contains(word)) {
                event.setCancelled(true); // Cancel message

                // Add player's UUID to punished list, avoid duplicates
                List<String> punished = plugin.getConfig().getStringList("punished");
                if (!punished.contains(player.getUniqueId().toString())) {
                    punished.add(player.getUniqueId().toString());
                    plugin.getConfig().set("punished", punished); // Update punished list in config
                    plugin.saveConfig(); // Save config
                }

                // Fetch and execute the blacklisted word punishment command
                String punishmentCommand = plugin.getConfig().getString("punishments.blacklisted-words-command");
                if (punishmentCommand != null && !punishmentCommand.isEmpty()) {
                    String finalCommand = punishmentCommand.replace("{player}", player.getName());
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
                    });
                }
                return; // Exit after processing blacklisted word
            }
        }
    }

    private void checkForSpam(AsyncPlayerChatEvent event, Player player, UUID playerUUID) {
        long currentTime = System.currentTimeMillis();
        int spamThreshold = plugin.getConfig().getInt("spam-threshold");
        int spamTimeWindow = plugin.getConfig().getInt("spam-time-window") * 1000; // Convert seconds to milliseconds

        // Check if the player has spoken recently
        if (lastMessageTime.containsKey(playerUUID) && (currentTime - lastMessageTime.get(playerUUID)) < spamTimeWindow) {
            // Increase message count if within the time window
            messageCount.put(playerUUID, messageCount.getOrDefault(playerUUID, 0) + 1);
        } else {
            // Reset message count if outside the time window
            messageCount.put(playerUUID, 1);
        }

        lastMessageTime.put(playerUUID, currentTime); // Update last message time

        // If message count exceeds threshold, consider it spam
        if (messageCount.get(playerUUID) > spamThreshold) {
            event.setCancelled(true); // Cancel the spam message

            // Fetch and execute the spam punishment command
            String spamCommand = plugin.getConfig().getString("punishments.spam-command");
            if (spamCommand != null && !spamCommand.isEmpty()) {
                String finalCommand = spamCommand.replace("{player}", player.getName());
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
                });
            }

            messageCount.put(playerUUID, 0); // Reset the spam count after punishment
        }
    }
}

