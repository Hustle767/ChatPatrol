package com.jamplifier;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.UUID;

public class ChatPatrolCommand implements CommandExecutor {

    private ChatPatrol plugin;

    public ChatPatrolCommand(ChatPatrol plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.AQUA + "ChatPatrol Plugin is active!");
            return true;
        }

        // Handle "/cp reload" command
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("chatpatrol.admin")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }

            plugin.reloadChatPatrolConfig();
            sender.sendMessage(ChatColor.GREEN + "ChatPatrol config reloaded!");
            return true;
        }

        // Handle "/cp list" command
        if (args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("chatpatrol.admin")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }

            List<String> punished = plugin.getConfig().getStringList("punished");
            if (punished.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "No players have been punished yet.");
                return true;
            }

            sender.sendMessage(ChatColor.GOLD + "Last 10 punished players:");
            for (int i = 0; i < Math.min(punished.size(), 10); i++) {
                UUID uuid = UUID.fromString(punished.get(i));
                String playerName = Bukkit.getOfflinePlayer(uuid).getName(); // Handle offline players

                if (playerName != null) {
                    sender.sendMessage(ChatColor.GOLD + String.valueOf(i + 1) + ". " + playerName);
                } else {
                    sender.sendMessage(ChatColor.GOLD + String.valueOf(i + 1) + ". Unknown Player (UUID: " + uuid + ")");
                }
            }
            return true;
        }

        return false;
    }
}

