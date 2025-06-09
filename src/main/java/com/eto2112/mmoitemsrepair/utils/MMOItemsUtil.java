package com.eto2112.mmoitemsrepair.utils;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MMOItemsUtil {

    /**
     * Check if an ItemStack is an MMOItem
     */
    public static boolean isMMOItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        return MMOItems.getType(item) != null;
    }

    /**
     * Get LiveMMOItem from ItemStack
     */
    public static LiveMMOItem getLiveMMOItem(ItemStack item) {
        if (!isMMOItem(item)) {
            return null;
        }
        try {
            return new LiveMMOItem(item);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check if an MMOItem has durability system
     */
    public static boolean hasDurability(ItemStack item) {
        LiveMMOItem mmoItem = getLiveMMOItem(item);
        if (mmoItem == null) {
            return false;
        }
        try {
            return mmoItem.hasData(MMOItems.plugin.getStats().get("DURABILITY")) &&
                    mmoItem.hasData(MMOItems.plugin.getStats().get("MAX_DURABILITY"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get current durability of an MMOItem
     */
    public static double getCurrentDurability(ItemStack item) {
        LiveMMOItem mmoItem = getLiveMMOItem(item);
        if (mmoItem == null || !hasDurability(item)) {
            return 0;
        }
        try {
            Object durabilityData = mmoItem.getData(MMOItems.plugin.getStats().get("DURABILITY"));
            if (durabilityData != null) {
                if (durabilityData instanceof Number) {
                    return ((Number) durabilityData).doubleValue();
                }
                try {
                    return (Double) durabilityData.getClass().getMethod("getValue").invoke(durabilityData);
                } catch (Exception e) {
                    return 0;
                }
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Get maximum durability of an MMOItem
     */
    public static double getMaxDurability(ItemStack item) {
        LiveMMOItem mmoItem = getLiveMMOItem(item);
        if (mmoItem == null || !hasDurability(item)) {
            return 0;
        }
        try {
            Object maxDurabilityData = mmoItem.getData(MMOItems.plugin.getStats().get("MAX_DURABILITY"));
            if (maxDurabilityData != null) {
                if (maxDurabilityData instanceof Number) {
                    return ((Number) maxDurabilityData).doubleValue();
                }
                try {
                    return (Double) maxDurabilityData.getClass().getMethod("getValue").invoke(maxDurabilityData);
                } catch (Exception e) {
                    return 100;
                }
            }
            return 100;
        } catch (Exception e) {
            return 100;
        }
    }

    /**
     * Repair an MMOItem using MMOItems command system (most reliable method)
     */
    public static ItemStack repairItem(ItemStack item) {
        if (!isMMOItem(item) || !hasDurability(item)) {
            return item;
        }

        try {
            // Get item details
            Type itemType = MMOItems.getType(item);
            String itemId = MMOItems.getID(item);

            if (itemType == null || itemId == null) {
                return item;
            }

            // Get the base MMOItem template
            MMOItem template = MMOItems.plugin.getMMOItem(itemType, itemId);
            if (template == null) {
                return item;
            }

            // Create a new item with full durability
            ItemStack repairedItem = template.newBuilder().build();

            // Copy any additional data from the original item
            LiveMMOItem originalLive = getLiveMMOItem(item);
            LiveMMOItem repairedLive = getLiveMMOItem(repairedItem);

            if (originalLive != null && repairedLive != null) {
                // Copy non-durability stats that might have been modified
                try {
                    // Copy enchantments, gems, etc. but keep full durability
                    copyNonDurabilityStats(originalLive, repairedLive);
                    return repairedLive.newBuilder().build();
                } catch (Exception e) {
                    // If copying fails, return the basic repaired item
                    return repairedItem;
                }
            }

            return repairedItem;
        } catch (Exception e) {
            return item;
        }
    }

    /**
     * Copy non-durability stats from one item to another
     */
    private static void copyNonDurabilityStats(LiveMMOItem from, LiveMMOItem to) {
        try {
            // Get all stats except durability ones
            for (net.Indyuce.mmoitems.stat.type.ItemStat stat : MMOItems.plugin.getStats().getAll()) {
                String statId = stat.getId();

                // Skip durability-related stats
                if (statId.equals("DURABILITY") || statId.equals("MAX_DURABILITY")) {
                    continue;
                }

                // Copy other stats if they exist
                if (from.hasData(stat)) {
                    Object data = from.getData(stat);
                    if (data != null) {
                        to.setData(stat, (StatData) data);
                    }
                }
            }
        } catch (Exception e) {
            // If copying fails, just ignore - the item will still be repaired
        }
    }

    /**
     * Check if an item needs repair (has less than max durability)
     */
    public static boolean needsRepair(ItemStack item) {
        if (!hasDurability(item)) {
            return false;
        }
        double current = getCurrentDurability(item);
        double max = getMaxDurability(item);
        return current < max && current >= 0;
    }

    /**
     * Calculate how many repair materials are needed (with tier-based cost multiplier)
     */
    public static int calculateRepairMaterials(ItemStack item, int durabilityPerMaterial) {
        if (!needsRepair(item)) {
            return 0;
        }

        double currentDurability = getCurrentDurability(item);
        double maxDurability = getMaxDurability(item);
        double missingDurability = maxDurability - currentDurability;

        // Get base materials needed
        int baseMaterials = (int) Math.ceil(missingDurability / durabilityPerMaterial);

        // Apply tier-based multiplier if available
        String tier = getItemTier(item);
        if (tier != null) {
            // This will be used by the plugin to apply cost multipliers
            // The actual multiplier application happens in the menu/command logic
        }

        return baseMaterials;
    }

    /**
     * Calculate repair materials with tier multiplier applied
     */
    public static int calculateRepairMaterialsWithTier(ItemStack item, int durabilityPerMaterial, double tierMultiplier) {
        int baseMaterials = calculateRepairMaterials(item, durabilityPerMaterial);
        return (int) Math.ceil(baseMaterials * tierMultiplier);
    }

    /**
     * Get the tier of an MMOItem - Fixed for MMOItems 6.10.1
     */
    public static String getItemTier(ItemStack item) {
        if (!isMMOItem(item)) {
            return null;
        }

        try {
            // Method 1: Try to get tier from item's NBT/lore directly
            if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
                List<String> lore = item.getItemMeta().getLore();
                for (String line : lore) {
                    String stripped = ChatColor.stripColor(line).toUpperCase();

                    // Check against known tiers
                    if (stripped.contains("MYTHICAL")) return "MYTHICAL";
                    if (stripped.contains("LEGENDARY")) return "LEGENDARY";
                    if (stripped.contains("ULTRA EPIC")) return "ULTRA_EPIC";
                    if (stripped.contains("EPIC")) return "EPIC";
                    if (stripped.contains("ULTRA RARE")) return "ULTRA_RARE";
                    if (stripped.contains("RARE")) return "RARE";
                    if (stripped.contains("UNCOMMON")) return "UNCOMMON";
                    if (stripped.contains("COMMON")) return "COMMON";
                    if (stripped.contains("UNIQUE")) return "UNIQUE";
                }
            }

            // Method 2: Try LiveMMOItem approach
            LiveMMOItem mmoItem = getLiveMMOItem(item);
            if (mmoItem != null) {
                // Try different ways to get tier data
                try {
                    net.Indyuce.mmoitems.stat.type.ItemStat tierStat = MMOItems.plugin.getStats().get("TIER");
                    if (tierStat != null && mmoItem.hasData(tierStat)) {
                        Object tierData = mmoItem.getData(tierStat);
                        if (tierData != null) {
                            String tierString = tierData.toString();
                            if (tierString != null && !tierString.isEmpty()) {
                                return tierString.toUpperCase();
                            }
                        }
                    }
                } catch (Exception e) {
                    // Continue to next method
                }

                // Method 3: Try to get from MMOItem template
                try {
                    Type itemType = MMOItems.getType(item);
                    String itemId = MMOItems.getID(item);
                    if (itemType != null && itemId != null) {
                        MMOItem template = MMOItems.plugin.getMMOItem(itemType, itemId);
                        if (template != null) {
                            net.Indyuce.mmoitems.stat.type.ItemStat tierStat = MMOItems.plugin.getStats().get("TIER");
                            if (tierStat != null && template.hasData(tierStat)) {
                                Object tierData = template.getData(tierStat);
                                if (tierData != null) {
                                    String tierString = tierData.toString();
                                    if (tierString != null && !tierString.isEmpty()) {
                                        return tierString.toUpperCase();
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // Continue to next method
                }
            }

            // Method 4: Debug - try to find tier in any way possible
            try {
                String debug = item.toString();
                for (String tier : new String[]{"MYTHICAL", "LEGENDARY", "ULTRA_EPIC", "EPIC", "ULTRA_RARE", "RARE", "UNCOMMON", "COMMON", "UNIQUE"}) {
                    if (debug.toUpperCase().contains(tier)) {
                        return tier;
                    }
                }
            } catch (Exception e) {
                // Ignore
            }

        } catch (Exception e) {
            // Return null if tier cannot be determined
        }

        return null; // No tier found
    }

    /**
     * Debug method to help identify why tier detection isn't working
     */
    public static void debugItemTier(ItemStack item, org.bukkit.command.CommandSender sender) {
        if (!isMMOItem(item)) {
            sender.sendMessage("§cNot an MMOItem!");
            return;
        }

        sender.sendMessage("§6=== TIER DEBUG INFO ===");

        // Check lore
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            sender.sendMessage("§eLore lines:");
            List<String> lore = item.getItemMeta().getLore();
            for (int i = 0; i < lore.size(); i++) {
                String line = lore.get(i);
                String stripped = ChatColor.stripColor(line);
                sender.sendMessage("§7[" + i + "] §f" + line);
                sender.sendMessage("§7    Stripped: §f" + stripped);
            }
        } else {
            sender.sendMessage("§cItem has no lore!");
        }

        // Check MMOItem data
        LiveMMOItem mmoItem = getLiveMMOItem(item);
        if (mmoItem != null) {
            sender.sendMessage("§eLiveMMOItem found!");

            try {
                net.Indyuce.mmoitems.stat.type.ItemStat tierStat = MMOItems.plugin.getStats().get("TIER");
                if (tierStat != null) {
                    sender.sendMessage("§eTIER stat exists!");
                    if (mmoItem.hasData(tierStat)) {
                        Object tierData = mmoItem.getData(tierStat);
                        sender.sendMessage("§eTier data: §f" + tierData);
                        sender.sendMessage("§eTier data class: §f" + tierData.getClass().getName());
                        sender.sendMessage("§eTier toString: §f" + tierData.toString());
                    } else {
                        sender.sendMessage("§cItem has no TIER data!");
                    }
                } else {
                    sender.sendMessage("§cTIER stat is null!");
                }
            } catch (Exception e) {
                sender.sendMessage("§cError accessing tier: " + e.getMessage());
            }
        }

        // Check template
        try {
            Type itemType = MMOItems.getType(item);
            String itemId = MMOItems.getID(item);
            sender.sendMessage("§eItem Type: §f" + (itemType != null ? itemType.getId() : "null"));
            sender.sendMessage("§eItem ID: §f" + (itemId != null ? itemId : "null"));

            if (itemType != null && itemId != null) {
                MMOItem template = MMOItems.plugin.getMMOItem(itemType, itemId);
                if (template != null) {
                    sender.sendMessage("§eTemplate found!");
                    net.Indyuce.mmoitems.stat.type.ItemStat tierStat = MMOItems.plugin.getStats().get("TIER");
                    if (tierStat != null && template.hasData(tierStat)) {
                        Object tierData = template.getData(tierStat);
                        sender.sendMessage("§eTemplate tier: §f" + tierData);
                    }
                }
            }
        } catch (Exception e) {
            sender.sendMessage("§cError checking template: " + e.getMessage());
        }

        String detectedTier = getItemTier(item);
        sender.sendMessage("§6Final detected tier: §f" + (detectedTier != null ? detectedTier : "§cNULL"));
    }

    /**
     * Get MMOItem by type and id
     */
    public static MMOItem getMMOItem(String type, String id) {
        try {
            Type itemType = MMOItems.plugin.getTypes().get(type);
            if (itemType == null) {
                return null;
            }
            return MMOItems.plugin.getMMOItem(itemType, id);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check if an MMOItem matches specific type and id
     */
    public static boolean isMMOItemOfType(ItemStack item, String type, String id) {
        if (!isMMOItem(item)) {
            return false;
        }

        try {
            Type itemType = MMOItems.getType(item);
            String itemId = MMOItems.getID(item);

            return itemType != null && itemId != null &&
                    itemType.getId().equalsIgnoreCase(type) &&
                    itemId.equalsIgnoreCase(id);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get durability percentage (0.0 to 1.0)
     */
    public static double getDurabilityPercentage(ItemStack item) {
        if (!hasDurability(item)) {
            return 1.0;
        }

        double current = getCurrentDurability(item);
        double max = getMaxDurability(item);

        if (max <= 0) return 1.0;
        return Math.max(0.0, Math.min(1.0, current / max));
    }

    /**
     * Alternative repair method using commands (fallback)
     */
    public static ItemStack repairItemViaCommand(ItemStack item, Player player) {
        if (!isMMOItem(item) || !hasDurability(item) || player == null) {
            return item;
        }

        try {
            // Get item slot in player's inventory
            int slot = player.getInventory().getHeldItemSlot();

            // Use MMOItems repair command
            String command = "mmoitems repair " + player.getName() + " " + slot;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

            // Return the item from player's hand (should be repaired now)
            return player.getInventory().getItemInMainHand();
        } catch (Exception e) {
            return item;
        }
    }
}