package com.eto2112.mmoitemsrepair.listeners;

import com.eto2112.mmoitemsrepair.MMOItemsRepair;
import com.eto2112.mmoitemsrepair.utils.MMOItemsUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class MenuListener implements Listener {

    private final MMOItemsRepair plugin;
    private final Random random = new Random();

    public MenuListener(MMOItemsRepair plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        // Check if this is a repair menu
        if (!plugin.getMenuManager().isRepairMenu(inventory)) {
            return;
        }

        event.setCancelled(true); // Cancel all clicks in repair menu by default

        int slot = event.getSlot();

        // Handle repair button click
        if (plugin.getMenuManager().isRepairButton(slot)) {
            handleRepairClick(player, inventory);
            return;
        }

        // Prevent clicking on display slots
        if (slot == plugin.getMenuManager().getItemToRepairSlot() ||
                slot == plugin.getMenuManager().getRepairMaterialsSlot() ||
                slot == plugin.getMenuManager().getRepairedPreviewSlot()) {
            // Don't allow clicking these display slots
            return;
        }
    }

    private void handleRepairClick(Player player, Inventory inventory) {
        ItemStack itemToRepair = player.getInventory().getItemInMainHand();

        // Validate item to repair
        if (itemToRepair == null || !MMOItemsUtil.isMMOItem(itemToRepair) || !MMOItemsUtil.hasDurability(itemToRepair)) {
            player.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("repair.not-mmoitem"));
            if (plugin.getConfigManager().closeAfterRepair()) {
                player.closeInventory();
            }
            return;
        }

        // Check if item needs repair
        if (!MMOItemsUtil.needsRepair(itemToRepair)) {
            player.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("repair.no-damage"));
            if (plugin.getConfigManager().closeAfterRepair()) {
                player.closeInventory();
            }
            return;
        }

        // Calculate materials needed with tier multiplier
        int baseMaterialsNeeded = MMOItemsUtil.calculateRepairMaterials(itemToRepair,
                plugin.getConfigManager().getDurabilityPerMaterial());

        // Apply tier-based cost multiplier
        String tier = MMOItemsUtil.getItemTier(itemToRepair);
        double tierMultiplier = 1.0;
        if (tier != null) {
            tierMultiplier = plugin.getConfigManager().getCostMultiplier(tier);
        }

        int finalMaterialsNeeded = (int) Math.ceil(baseMaterialsNeeded * tierMultiplier);

        if (finalMaterialsNeeded <= 0) {
            player.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("repair.no-damage"));
            if (plugin.getConfigManager().closeAfterRepair()) {
                player.closeInventory();
            }
            return;
        }

        // Check if player has enough repair materials
        String materialId = plugin.getConfigManager().getRepairMaterialId();
        if (!hasEnoughRepairMaterials(player, materialId, finalMaterialsNeeded)) {
            player.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("repair.insufficient-materials"));
            return;
        }

        // Check success rate
        double successRate = plugin.getConfigManager().getSuccessRate();
        if (successRate < 1.0 && random.nextDouble() > successRate) {
            // Repair failed
            removeRepairMaterials(player, materialId, finalMaterialsNeeded);
            player.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("repair.failed"));

            // Log failed repair if debug is enabled
            if (plugin.getConfigManager().isDebugEnabled() && plugin.getConfigManager().shouldLogRepairs()) {
                plugin.getLogger().info("REPAIR FAILED - " + player.getName() + " tried to repair " +
                        itemToRepair.getType().name() + " (Tier: " + (tier != null ? tier : "UNKNOWN") +
                        ", Cost: " + finalMaterialsNeeded + "x " + materialId + ")");
            }
            return;
        }

        // Remove repair materials from player inventory
        if (!removeRepairMaterials(player, materialId, finalMaterialsNeeded)) {
            player.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("repair.insufficient-materials"));
            return;
        }

        // Repair the item
        ItemStack repairedItem = MMOItemsUtil.repairItem(itemToRepair);

        // Update player's main hand item
        player.getInventory().setItemInMainHand(repairedItem);

        // Send success message with tier info
        player.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("repair.success"));

        if (tier != null && tierMultiplier > 1.0) {
            player.sendMessage(plugin.getLanguageManager().getPrefix() +
                    "§7Repaired §e" + tier + " §7item (§c" + tierMultiplier + "x§7 cost) using §e" +
                    finalMaterialsNeeded + "§7 materials.");
        } else {
            player.sendMessage(plugin.getLanguageManager().getMessageWithPrefix("repair.materials-removed",
                    "{amount}", String.valueOf(finalMaterialsNeeded)));
        }

        // Log successful repair if debug is enabled
        if (plugin.getConfigManager().isDebugEnabled() && plugin.getConfigManager().shouldLogRepairs()) {
            plugin.getLogger().info("REPAIR SUCCESS - " + player.getName() + " repaired " +
                    itemToRepair.getType().name() + " (Tier: " + (tier != null ? tier : "UNKNOWN") +
                    ", Multiplier: " + tierMultiplier + "x, Final Cost: " + finalMaterialsNeeded + "x " + materialId + ")");
        }

        // Close menu if configured to do so
        if (plugin.getConfigManager().closeAfterRepair()) {
            player.closeInventory();
        } else {
            // Refresh the menu to show updated state
            Inventory newMenu = plugin.getMenuManager().createRepairMenu(player, player.getInventory().getItemInMainHand());
            if (newMenu != null) {
                player.openInventory(newMenu);
            }
        }
    }

    private boolean hasEnoughRepairMaterials(Player player, String materialId, int needed) {
        String[] parts = materialId.split("\\.");
        if (parts.length != 2) {
            return false;
        }

        String type = parts[0];
        String id = parts[1];

        // Check for MMOItem repair materials
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && MMOItemsUtil.isMMOItemOfType(item, type, id)) {
                count += item.getAmount();
                if (count >= needed) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean removeRepairMaterials(Player player, String materialId, int needed) {
        String[] parts = materialId.split("\\.");
        if (parts.length != 2) {
            return false;
        }

        String type = parts[0];
        String id = parts[1];
        int remaining = needed;

        // First pass: count available materials
        int available = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && MMOItemsUtil.isMMOItemOfType(item, type, id)) {
                available += item.getAmount();
            }
        }

        if (available < needed) {
            return false;
        }

        // Second pass: remove materials
        for (int i = 0; i < player.getInventory().getSize() && remaining > 0; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && MMOItemsUtil.isMMOItemOfType(item, type, id)) {
                int amount = item.getAmount();
                if (amount <= remaining) {
                    player.getInventory().setItem(i, null);
                    remaining -= amount;
                } else {
                    item.setAmount(amount - remaining);
                    remaining = 0;
                }
            }
        }

        return remaining == 0;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        // Check if this was a repair menu
        if (plugin.getMenuManager().isRepairMenu(event.getInventory())) {
            // Menu closed, no special handling needed since we don't store items in the menu
        }
    }
}