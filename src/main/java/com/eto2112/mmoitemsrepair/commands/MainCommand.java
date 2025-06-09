package com.eto2112.mmoitemsrepair.commands;

import com.eto2112.mmoitemsrepair.MMOItemsRepair;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final MMOItemsRepair plugin;

    public MainCommand(MMOItemsRepair plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
            case "rl":
                return handleReload(sender);
            case "version":
            case "ver":
            case "v":
                return handleVersion(sender);
            case "debug":
                return handleDebug(sender, args);
            case "tierdebug":
                return handleTierDebug(sender);
            case "testmultiplier":
                return handleTestMultiplier(sender);
            case "info":
                return handleInfo(sender);
            case "help":
            case "?":
                sendHelpMessage(sender);
                return true;
            default:
                sendHelpMessage(sender);
                return true;
        }
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("mmoitemsrepair.admin")) {
            sender.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("no-permission"));
            return true;
        }

        long startTime = System.currentTimeMillis();

        try {
            plugin.reload();
            long duration = System.currentTimeMillis() - startTime;

            sender.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("reload-success"));
            sender.sendMessage(plugin.getLanguageManager().getPrefix() + "§7Reload completed in §e" + duration + "ms");

            // Log the reload
            plugin.getLogger().info("Configuration reloaded by " + sender.getName() + " in " + duration + "ms");

        } catch (Exception e) {
            sender.sendMessage(plugin.getLanguageManager().getPrefix() + "§cError reloading plugin: " + e.getMessage());
            plugin.getLogger().severe("Error reloading plugin: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    private boolean handleTestMultiplier(CommandSender sender) {
        if (!sender.hasPermission("mmoitemsrepair.admin")) {
            sender.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("no-permission"));
            return true;
        }

        if (!(sender instanceof org.bukkit.entity.Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("player-only"));
            return true;
        }

        org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
        org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType().isAir()) {
            sender.sendMessage(plugin.getLanguageManager().getPrefix() + "§cYou must be holding an item!");
            return true;
        }

        String tier = com.eto2112.mmoitemsrepair.utils.MMOItemsUtil.getItemTier(item);
        double multiplier = plugin.getConfigManager().getCostMultiplier(tier);

        sender.sendMessage("§6=== MULTIPLIER TEST ===");
        sender.sendMessage("§eTier detected: §f" + (tier != null ? tier : "NULL"));
        sender.sendMessage("§eMultiplier: §f" + multiplier);

        // Test some known tiers
        sender.sendMessage("");
        sender.sendMessage("§eConfig test:");
        sender.sendMessage("§7LEGENDARY: §f" + plugin.getConfigManager().getCostMultiplier("LEGENDARY"));
        sender.sendMessage("§7MYTHICAL: §f" + plugin.getConfigManager().getCostMultiplier("MYTHICAL"));
        sender.sendMessage("§7COMMON: §f" + plugin.getConfigManager().getCostMultiplier("COMMON"));

        return true;
    }

    private boolean handleTierDebug(CommandSender sender) {
        if (!sender.hasPermission("mmoitemsrepair.admin")) {
            sender.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("no-permission"));
            return true;
        }

        if (!(sender instanceof org.bukkit.entity.Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("player-only"));
            return true;
        }

        org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
        org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType().isAir()) {
            sender.sendMessage(plugin.getLanguageManager().getPrefix() + "§cYou must be holding an item!");
            return true;
        }

        // Debug the tier detection
        com.eto2112.mmoitemsrepair.utils.MMOItemsUtil.debugItemTier(item, sender);

        return true;
    }

    private boolean handleVersion(CommandSender sender) {
        String version = plugin.getDescription().getVersion();
        String authors = String.join(", ", plugin.getDescription().getAuthors());

        sender.sendMessage("§6§l=== MMOItemsRepair Version Info ===");
        sender.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("version-info", "{version}", version));
        sender.sendMessage(plugin.getLanguageManager().getPrefix() + "§7Author(s): §e" + authors);
        sender.sendMessage(plugin.getLanguageManager().getPrefix() + "§7Description: §e" + plugin.getDescription().getDescription());
        sender.sendMessage(plugin.getLanguageManager().getPrefix() + "§7Website: §e" + plugin.getDescription().getWebsite());

        // Server info
        sender.sendMessage("");
        sender.sendMessage(plugin.getLanguageManager().getPrefix() + "§7Server Version: §e" +
                plugin.getServer().getBukkitVersion());
        sender.sendMessage(plugin.getLanguageManager().getPrefix() + "§7Java Version: §e" +
                System.getProperty("java.version"));

        // Plugin status
        boolean mmoItemsLoaded = plugin.getServer().getPluginManager().getPlugin("MMOItems") != null;
        sender.sendMessage(plugin.getLanguageManager().getPrefix() + "§7MMOItems: " +
                (mmoItemsLoaded ? "§a✓ Loaded" : "§c✗ Not Found"));

        return true;
    }

    private boolean handleInfo(CommandSender sender) {
        sender.sendMessage("§6§l=== MMOItemsRepair Plugin Info ===");
        sender.sendMessage("§7This plugin provides a repair system for MMOItems with custom materials.");
        sender.sendMessage("");
        sender.sendMessage("§e§lFeatures:");
        sender.sendMessage("§7• Interactive repair GUI with preview");
        sender.sendMessage("§7• Material-based repair system");
        sender.sendMessage("§7• Configurable repair costs and success rates");
        sender.sendMessage("§7• Full durability restoration");
        sender.sendMessage("§7• Admin commands for management");
        sender.sendMessage("");
        sender.sendMessage("§e§lCommands:");
        sender.sendMessage("§7• §e/repair §7- Open repair menu");
        sender.sendMessage("§7• §e/mmoitemsrepair reload §7- Reload configs");
        sender.sendMessage("§7• §e/mmoitemsrepair version §7- Show version");

        return true;
    }

    private boolean handleDebug(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mmoitemsrepair.admin")) {
            sender.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("no-permission"));
            return true;
        }

        if (args.length < 2) {
            boolean currentStatus = plugin.getConfigManager().isDebugEnabled();
            sender.sendMessage(plugin.getLanguageManager().getPrefix() + "§7Debug mode is currently: " +
                    (currentStatus ? "§aEnabled" : "§cDisabled"));
            sender.sendMessage(plugin.getLanguageManager().getPrefix() + "§7Usage: §e/mmoitemsrepair debug <on|off>");
            return true;
        }

        boolean enable = args[1].equalsIgnoreCase("on") ||
                args[1].equalsIgnoreCase("true") ||
                args[1].equalsIgnoreCase("enable");

        // Update config
        plugin.getConfigManager().getConfig().set("debug.enabled", enable);
        plugin.getConfigManager().saveConfig();

        if (enable) {
            sender.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("admin.debug-enabled"));
            plugin.getLogger().info("Debug mode enabled by " + sender.getName());
        } else {
            sender.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("admin.debug-disabled"));
            plugin.getLogger().info("Debug mode disabled by " + sender.getName());
        }

        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§6§l=== MMOItemsRepair Commands ===");
        sender.sendMessage("§e/repair §7- Open repair menu for held MMOItem");
        sender.sendMessage("");
        sender.sendMessage("§e§lAdmin Commands:");
        sender.sendMessage("§e/mmoitemsrepair reload §7- Reload configuration files");
        sender.sendMessage("§e/mmoitemsrepair version §7- Show plugin version and info");
        sender.sendMessage("§e/mmoitemsrepair info §7- Show plugin information");
        sender.sendMessage("§e/mmoitemsrepair debug <on|off> §7- Toggle debug mode");
        sender.sendMessage("§e/mmoitemsrepair tierdebug §7- Debug tier detection for held item");
        sender.sendMessage("§e/mmoitemsrepair help §7- Show this help message");
        sender.sendMessage("");
        sender.sendMessage("§7§lAliases:");
        sender.sendMessage("§7• Repair: §e/repair, /fix, /mmorepair");
        sender.sendMessage("§7• Main: §e/mmoitemsrepair, /mir");
        sender.sendMessage("§7• Version: §e/mmoitemsrepair ver, /mmoitemsrepair v");
        sender.sendMessage("§7• Reload: §e/mmoitemsrepair rl");
        sender.sendMessage("");
        sender.sendMessage("§7§lPermissions:");
        sender.sendMessage("§7• Repair: §emmoitemsrepair.use");
        sender.sendMessage("§7• Admin: §emmoitemsrepair.admin");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("reload", "rl", "version", "ver", "v", "debug", "tierdebug", "info", "help");
            String input = args[0].toLowerCase();

            for (String subcommand : subcommands) {
                if (subcommand.startsWith(input)) {
                    // Check permissions for admin commands
                    if (subcommand.equals("reload") || subcommand.equals("rl") ||
                            subcommand.equals("debug") || subcommand.equals("tierdebug")) {
                        if (sender.hasPermission("mmoitemsrepair.admin")) {
                            completions.add(subcommand);
                        }
                    } else {
                        completions.add(subcommand);
                    }
                }
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("debug"))) {
            if (sender.hasPermission("mmoitemsrepair.admin")) {
                List<String> options = Arrays.asList("on", "off", "true", "false", "enable", "disable");
                String input = args[1].toLowerCase();

                for (String option : options) {
                    if (option.startsWith(input)) {
                        completions.add(option);
                    }
                }
            }
        }

        return completions;
    }
}