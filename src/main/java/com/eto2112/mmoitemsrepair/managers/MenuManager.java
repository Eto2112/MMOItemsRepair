package com.eto2112.mmoitemsrepair.managers;

import com.eto2112.mmoitemsrepair.MMOItemsRepair;
import com.eto2112.mmoitemsrepair.models.RepairMenu;
import com.eto2112.mmoitemsrepair.utils.MMOItemsUtil;
import com.eto2112.mmoitemsrepair.utils.MessageUtil;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MenuManager {

    private final MMOItemsRepair plugin;
    private RepairMenu repairMenuConfig;

    public MenuManager(MMOItemsRepair plugin) {
        this.plugin = plugin;
    }

    public void loadMenu() {
        loadRepairMenuConfig();
    }

    private void loadRepairMenuConfig() {
        ConfigManager config = plugin.getConfigManager();

        String title = MessageUtil.colorize(config.getGuiTitle());
        int size = config.getGuiSize();
        int itemToRepairSlot = config.getItemToRepairSlot();
        int repairMaterialsSlot = config.getRepairMaterialsSlot();
        int repairedPreviewSlot = config.getRepairedPreviewSlot();
        int repairButtonSlot = config.getRepairButtonSlot();

        repairMenuConfig = new RepairMenu(title, size, itemToRepairSlot, repairMaterialsSlot,
                repairedPreviewSlot, repairButtonSlot, new HashMap<>());
    }

    public Inventory createRepairMenu(Player player, ItemStack itemToRepair) {
        if (repairMenuConfig == null) {
            return null;
        }

        Inventory menu = Bukkit.createInventory(null, repairMenuConfig.getSize(),
                repairMenuConfig.getTitle());

        // Fill with background if decoration is enabled
        if (plugin.getConfigManager().isDecorationEnabled()) {
            fillBackground(menu);
        }

        // Set the item to repair
        if (itemToRepair != null && MMOItemsUtil.hasDurability(itemToRepair)) {
            setItemToRepair(menu, itemToRepair);
            setRepairMaterials(menu, player, itemToRepair);
            setRepairedPreview(menu, itemToRepair);
            setRepairButton(menu, player, itemToRepair);
        } else {
            setErrorItem(menu);
        }

        return menu;
    }

    private void fillBackground(Inventory menu) {
        String materialName = plugin.getConfigManager().getBackgroundMaterial();
        String displayName = plugin.getConfigManager().getBackgroundName();

        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.GRAY_STAINED_GLASS_PANE;
        }

        ItemStack backgroundItem = new ItemStack(material);
        ItemMeta meta = backgroundItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtil.colorize(displayName));
            backgroundItem.setItemMeta(meta);
        }

        // Fill all slots except special ones
        for (int i = 0; i < menu.getSize(); i++) {
            if (i != repairMenuConfig.getItemToRepairSlot() &&
                    i != repairMenuConfig.getRepairMaterialsSlot() &&
                    i != repairMenuConfig.getRepairedPreviewSlot() &&
                    i != repairMenuConfig.getRepairButtonSlot()) {
                menu.setItem(i, backgroundItem);
            }
        }
    }

    private void setItemToRepair(Inventory menu, ItemStack item) {
        ItemStack displayItem = item.clone();
        ItemMeta meta = displayItem.getItemMeta();

        if (meta != null) {
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

            double current = MMOItemsUtil.getCurrentDurability(item);
            double max = MMOItemsUtil.getMaxDurability(item);
            double missing = max - current;
            double percentage = MMOItemsUtil.getDurabilityPercentage(item);

            lore.add("");
            lore.add(plugin.getLanguageManager().getMessage("repair.durability-info",
                    "{current}", MessageUtil.formatNumber(current),
                    "{max}", MessageUtil.formatNumber(max),
                    "{percentage}", MessageUtil.formatPercentage(percentage)));
            lore.add(plugin.getLanguageManager().getMessage("menu.item-to-repair.lore",
                    "{missing}", MessageUtil.formatNumber(missing)));

            meta.setLore(lore);
            displayItem.setItemMeta(meta);
        }

        menu.setItem(repairMenuConfig.getItemToRepairSlot(), displayItem);
    }

    private void setRepairMaterials(Inventory menu, Player player, ItemStack item) {
        String materialId = plugin.getConfigManager().getRepairMaterialId();
        int baseMaterialsNeeded = MMOItemsUtil.calculateRepairMaterials(item,
                plugin.getConfigManager().getDurabilityPerMaterial());

        // Apply tier-based cost multiplier
        String tier = MMOItemsUtil.getItemTier(item);
        double tierMultiplier = 1.0;
        if (tier != null) {
            tierMultiplier = plugin.getConfigManager().getCostMultiplier(tier);
        }

        // Debug logging
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("REPAIR COST DEBUG:");
            plugin.getLogger().info("  Base materials needed: " + baseMaterialsNeeded);
            plugin.getLogger().info("  Detected tier: " + (tier != null ? tier : "NULL"));
            plugin.getLogger().info("  Tier multiplier: " + tierMultiplier);
        }

        int finalMaterialsNeeded = (int) Math.ceil(baseMaterialsNeeded * tierMultiplier);

        // More debug logging
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("  Final materials needed: " + finalMaterialsNeeded);
        }

        // Try to get the repair material as an MMOItem
        String[] parts = materialId.split("\\.");
        ItemStack materialDisplay = null;

        if (parts.length == 2) {
            MMOItem mmoMaterial = MMOItemsUtil.getMMOItem(parts[0], parts[1]);
            if (mmoMaterial != null) {
                materialDisplay = mmoMaterial.newBuilder().build();
            }
        }

        // Fallback to a default material if MMOItem not found
        if (materialDisplay == null) {
            materialDisplay = new ItemStack(Material.DIAMOND, 1);
        }

        materialDisplay.setAmount(Math.min(finalMaterialsNeeded, 64));
        ItemMeta meta = materialDisplay.getItemMeta();

        if (meta != null) {
            int playerAmount = countRepairMaterials(player, materialId);
            String status = playerAmount >= finalMaterialsNeeded ?
                    plugin.getLanguageManager().getMessage("status.sufficient") :
                    plugin.getLanguageManager().getMessage("status.insufficient");

            List<String> lore = new ArrayList<>();
            lore.add("§7Repair Cost: §e" + finalMaterialsNeeded + "x " +
                    (parts.length > 1 ? parts[1] : "Repair Material"));

            // Show tier information if applicable
            if (tier != null && tierMultiplier > 1.0) {
                lore.add("§7Tier: §e" + tier + " §7(§c" + String.format("%.1f", tierMultiplier) + "x§7 cost)");
                lore.add("§7Base: §f" + baseMaterialsNeeded + " §7→ Final: §f" + finalMaterialsNeeded);
            } else {
                lore.add("§7Base Cost (no tier bonus)");
            }

            lore.add("");
            lore.add("§7You have: §f" + playerAmount + "x");
            lore.add("§7Status: " + status);

            meta.setLore(lore);
            materialDisplay.setItemMeta(meta);
        }

        menu.setItem(repairMenuConfig.getRepairMaterialsSlot(), materialDisplay);
    }

    private void setRepairedPreview(Inventory menu, ItemStack item) {
        if (!plugin.getConfigManager().showPreview()) {
            return;
        }

        ItemStack previewItem = MMOItemsUtil.repairItem(item.clone());
        ItemMeta meta = previewItem.getItemMeta();

        if (meta != null) {
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

            double max = MMOItemsUtil.getMaxDurability(item);

            lore.add("");
            lore.add(plugin.getLanguageManager().getMessage("menu.repaired-preview.name"));
            lore.add(plugin.getLanguageManager().getMessage("repair.durability-info",
                    "{current}", MessageUtil.formatNumber(max),
                    "{max}", MessageUtil.formatNumber(max),
                    "{percentage}", "100.0%"));

            meta.setLore(lore);
            previewItem.setItemMeta(meta);
        }

        menu.setItem(repairMenuConfig.getRepairedPreviewSlot(), previewItem);
    }

    private void setRepairButton(Inventory menu, Player player, ItemStack item) {
        ItemStack repairButton = new ItemStack(Material.ANVIL);
        ItemMeta meta = repairButton.getItemMeta();

        if (meta != null) {
            String materialId = plugin.getConfigManager().getRepairMaterialId();
            int baseMaterialsNeeded = MMOItemsUtil.calculateRepairMaterials(item,
                    plugin.getConfigManager().getDurabilityPerMaterial());

            // Apply tier-based cost multiplier
            String tier = MMOItemsUtil.getItemTier(item);
            double tierMultiplier = 1.0;
            if (tier != null) {
                tierMultiplier = plugin.getConfigManager().getCostMultiplier(tier);
            }

            int finalMaterialsNeeded = (int) Math.ceil(baseMaterialsNeeded * tierMultiplier);
            int playerAmount = countRepairMaterials(player, materialId);
            double successRate = plugin.getConfigManager().getSuccessRate();

            String[] parts = materialId.split("\\.");
            String materialName = parts.length > 1 ? parts[1] : "Repair Material";

            meta.setDisplayName(plugin.getLanguageManager().getMessage("menu.repair-button.name"));

            List<String> lore = new ArrayList<>();
            lore.add("§7Repair Cost: §e" + finalMaterialsNeeded + "x " + materialName);

            // Show tier multiplier if applicable
            if (tier != null && tierMultiplier > 1.0) {
                lore.add("§7Item Tier: §e" + tier + " §7(§c+" + Math.round((tierMultiplier - 1.0) * 100) + "%§7 cost)");
            }

            lore.add("§7Success Rate: §a" + Math.round(successRate * 100) + "%");
            lore.add("");

            if (playerAmount >= finalMaterialsNeeded) {
                lore.add("§a✓ You have: §f" + playerAmount + "x " + materialName);
                lore.add("");
                lore.add("§e§lCLICK TO REPAIR!");
            } else {
                lore.add("§c✗ Insufficient materials!");
                lore.add("§7You have: §c" + playerAmount + "§7/§e" + finalMaterialsNeeded);
                lore.add("§7Need: §c" + (finalMaterialsNeeded - playerAmount) + " more");
            }

            meta.setLore(lore);
            repairButton.setItemMeta(meta);
        }

        menu.setItem(repairMenuConfig.getRepairButtonSlot(), repairButton);
    }

    private void setErrorItem(Inventory menu) {
        ItemStack errorItem = new ItemStack(Material.BARRIER);
        ItemMeta meta = errorItem.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(plugin.getLanguageManager().getMessage("menu.error-item.name"));
            meta.setLore(Arrays.asList(
                    plugin.getLanguageManager().getMessage("repair.not-mmoitem"),
                    plugin.getLanguageManager().getMessage("repair.no-durability"),
                    plugin.getLanguageManager().getMessage("repair.no-damage")
            ));
            errorItem.setItemMeta(meta);
        }

        menu.setItem(repairMenuConfig.getItemToRepairSlot(), errorItem);
    }

    private int countRepairMaterials(Player player, String materialId) {
        String[] parts = materialId.split("\\.");
        if (parts.length != 2) {
            return 0;
        }

        String type = parts[0];
        String id = parts[1];
        int count = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && MMOItemsUtil.isMMOItemOfType(item, type, id)) {
                count += item.getAmount();
            }
        }

        return count;
    }

    public boolean isRepairMenu(Inventory inventory) {
        if (repairMenuConfig == null) {
            return false;
        }

        // Use InventoryView to get title in 1.21.4
        if (inventory.getViewers().isEmpty()) {
            return false;
        }

        try {
            String inventoryTitle = inventory.getViewers().get(0).getOpenInventory().getTitle();
            return inventory.getSize() == repairMenuConfig.getSize() &&
                    ChatColor.stripColor(inventoryTitle)
                            .equals(ChatColor.stripColor(repairMenuConfig.getTitle()));
        } catch (Exception e) {
            // Fallback comparison if title retrieval fails
            return inventory.getSize() == repairMenuConfig.getSize();
        }
    }

    public boolean isRepairButton(int slot) {
        return repairMenuConfig != null && slot == repairMenuConfig.getRepairButtonSlot();
    }

    public RepairMenu getRepairMenuConfig() {
        return repairMenuConfig;
    }

    public int getItemToRepairSlot() {
        return repairMenuConfig != null ? repairMenuConfig.getItemToRepairSlot() : -1;
    }

    public int getRepairMaterialsSlot() {
        return repairMenuConfig != null ? repairMenuConfig.getRepairMaterialsSlot() : -1;
    }

    public int getRepairedPreviewSlot() {
        return repairMenuConfig != null ? repairMenuConfig.getRepairedPreviewSlot() : -1;
    }
}