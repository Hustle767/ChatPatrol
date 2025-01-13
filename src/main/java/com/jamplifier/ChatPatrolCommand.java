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

        return false;
    }
}

