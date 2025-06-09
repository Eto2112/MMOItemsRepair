package com.eto2112.mmoitemsrepair.commands;

import com.eto2112.mmoitemsrepair.MMOItemsRepair;
import com.eto2112.mmoitemsrepair.utils.MMOItemsUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RepairCommand implements CommandExecutor, TabCompleter {

    private final MMOItemsRepair plugin;

    public RepairCommand(MMOItemsRepair plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("player-only"));
            return true;
        }

        Player player = (Player) sender;

        // Check permission
        if (!player.hasPermission("mmoitemsrepair.use")) {
            player.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("no-permission"));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if holding a valid MMOItem with durability
        if (item == null || item.getType().isAir()) {
            player.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("repair.no-item"));
            return true;
        }

        if (!MMOItemsUtil.isMMOItem(item)) {
            player.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("repair.not-mmoitem"));
            return true;
        }

        if (!MMOItemsUtil.hasDurability(item)) {
            player.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("repair.no-durability"));
            return true;
        }

        // Check if item needs repair
        if (!MMOItemsUtil.needsRepair(item)) {
            player.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("repair.no-damage"));
            return true;
        }

        // Open repair menu
        Inventory menu = plugin.getMenuManager().createRepairMenu(player, item);
        if (menu != null) {
            player.openInventory(menu);
        } else {
            player.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("repair.menu-error"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>(); // No tab completion needed for this command
    }
}