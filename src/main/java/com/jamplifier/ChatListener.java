package com.jamplifier;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import net.md_5.bungee.api.ChatColor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
        String originalMessage = event.getMessage(); // Keep the original message
        String message = originalMessage.toLowerCase(); // Lowercase version for word filter

        // Check for blacklisted words
        if (plugin.getConfig().getBoolean("enable-word-filter")) {
            checkForBlacklistedWords(event, player, message);
        }

        // Check for spam
        if (plugin.getConfig().getBoolean("enable-spam-filter")) {
            checkForSpam(event, player, player.getUniqueId());
        }

        // Check for caps filter
        if (plugin.getConfig().getBoolean("enable-caps-filter")) {
            checkForExcessiveCaps(event, player, originalMessage);
        }
    }

    
    private void checkForBlacklistedWords(AsyncPlayerChatEvent event, Player player, String message) {
        // Get blacklisted words from the config
        List<String> blacklistedWords = plugin.getConfig().getStringList("blacklisted-words");

        for (String word : blacklistedWords) {
            if (message.contains(word)) {
                event.setCancelled(true); // Cancel message

                // Log punishment to file
                logPunishment(player, "Blacklisted Word", message);

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

            // Log punishment to file
            logPunishment(player, "Spam", event.getMessage());

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
    private void checkForExcessiveCaps(AsyncPlayerChatEvent event, Player player, String message) {
        int maxCapsAllowed = plugin.getConfig().getInt("caps-threshold"); // Get the caps threshold from the config
        long capsCount = message.chars().filter(Character::isUpperCase).count(); // Count uppercase letters in the message

        // Check if the caps count exceeds the threshold
        if (capsCount > maxCapsAllowed) {
            String modifiedMessage = message.toLowerCase(); // Convert the message to lowercase
            event.setMessage(modifiedMessage); // Update the event's message with the modified version

            // Check if notifications are enabled
            if (plugin.getConfig().getBoolean("caps-notification", true)) {
                // Notify the player about the modification with a default message
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(ChatColor.YELLOW + "Your message contained too many capital letters and was modified to lowercase.");
                });
            }
        }
    }

    private void logPunishment(Player player, String reason, String message) {
        File punishmentsFile = new File(plugin.getDataFolder(), "punishments.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(punishmentsFile, true))) {
            writer.write("Player: " + player.getName() + " | Reason: " + reason + " | Message: " + message);
            writer.newLine();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to write to punishments.txt: " + e.getMessage());
        }
    }

}

