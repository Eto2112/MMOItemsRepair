package com.eto2112.mmoitemsrepair.managers;

import com.eto2112.mmoitemsrepair.MMOItemsRepair;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ConfigManager {

    private final MMOItemsRepair plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(MMOItemsRepair plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        createConfigFile();
        loadConfig();
    }

    private void createConfigFile() {
        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        if (!configFile.exists()) {
            try {
                // Try to save resource, if not available, create manually
                if (plugin.getResource("config.yml") != null) {
                    plugin.saveResource("config.yml", false);
                } else {
                    createDefaultConfig();
                }
            } catch (Exception e) {
                createDefaultConfig();
            }
        }
    }

    private void createDefaultConfig() {
        try {
            configFile.createNewFile();
            FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(configFile);
            setDefaultConfigValues(defaultConfig);
            defaultConfig.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create config.yml", e);
        }
    }

    private void setDefaultConfigValues(FileConfiguration config) {
        config.set("version", "1.0");

        // Repair settings
        config.set("repair.material-id", "material.REPAIR_STONE");
        config.set("repair.durability-per-material", 100);
        config.set("repair.strict-material-check", true);
        config.set("repair.success-rate", 1.0);
        config.set("repair.show-preview", true);
        config.set("repair.close-after-repair", true);

        // Cost multipliers based on actual MMOItems tiers
        config.set("repair.cost-multipliers.COMMON", 1.0);
        config.set("repair.cost-multipliers.UNCOMMON", 1.2);
        config.set("repair.cost-multipliers.RARE", 1.5);
        config.set("repair.cost-multipliers.ULTRA_RARE", 2.0);
        config.set("repair.cost-multipliers.EPIC", 2.5);
        config.set("repair.cost-multipliers.ULTRA_EPIC", 3.0);
        config.set("repair.cost-multipliers.LEGENDARY", 4.0);
        config.set("repair.cost-multipliers.MYTHICAL", 5.0);
        config.set("repair.cost-multipliers.UNIQUE", 6.0);

        // GUI settings
        config.set("gui.title", "&8⚒ Repair Station ⚒");
        config.set("gui.size", 27);
        config.set("gui.slots.item-to-repair", 10);
        config.set("gui.slots.repair-materials", 12);
        config.set("gui.slots.repaired-preview", 16);
        config.set("gui.slots.repair-button", 14);

        // Decoration
        config.set("gui.decoration.enabled", true);
        config.set("gui.decoration.background.material", "gray_stained_glass_pane");
        config.set("gui.decoration.background.name", "&f");

        config.set("gui.decoration.repair-button.material", "anvil");
        config.set("gui.decoration.repair-button.name", "&a⚒ REPAIR ITEM ⚒");
        config.set("gui.decoration.repair-button.lore", java.util.Arrays.asList(
                "&7Click to repair your item",
                "&7Cost: {materials} repair materials",
                "",
                "&eClick to repair!"
        ));

        config.set("gui.decoration.error.material", "barrier");
        config.set("gui.decoration.error.name", "&cCannot Repair This Item");
        config.set("gui.decoration.error.lore", java.util.Arrays.asList(
                "&7This item cannot be repaired",
                "&7Reasons:",
                "&c• Not an MMOItem with durability",
                "&c• Item is already at full durability"
        ));

        // Debug settings
        config.set("debug.enabled", false);
        config.set("debug.log-repairs", true);
        config.set("debug.log-material-checks", false);
    }

    private void loadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void reloadConfig() {
        loadConfig();
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config.yml", e);
        }
    }

    // Getters
    public FileConfiguration getConfig() {
        return config;
    }

    // Repair settings
    public String getRepairMaterialId() {
        return config.getString("repair.material-id", "material.REPAIR_STONE");
    }

    public int getDurabilityPerMaterial() {
        return config.getInt("repair.durability-per-material", 100);
    }

    public boolean isStrictMaterialCheck() {
        return config.getBoolean("repair.strict-material-check", true);
    }

    public double getSuccessRate() {
        return config.getDouble("repair.success-rate", 1.0);
    }

    public boolean showPreview() {
        return config.getBoolean("repair.show-preview", true);
    }

    public boolean closeAfterRepair() {
        return config.getBoolean("repair.close-after-repair", true);
    }

    // Cost multipliers
    public double getCostMultiplier(String tier) {
        if (tier == null) {
            return 1.0;
        }

        // Normalize tier name to uppercase and handle variations
        String normalizedTier = tier.toUpperCase().trim();

        // Check for exact matches first
        double multiplier = config.getDouble("repair.cost-multipliers." + normalizedTier, -1.0);
        if (multiplier > 0) {
            return multiplier;
        }

        // Handle common variations
        switch (normalizedTier) {
            case "LEGENDARY":
            case "LEGEND":
                return config.getDouble("repair.cost-multipliers.LEGENDARY", 4.0);
            case "MYTHICAL":
            case "MYTHIC":
                return config.getDouble("repair.cost-multipliers.MYTHICAL", 5.0);
            case "UNIQUE":
                return config.getDouble("repair.cost-multipliers.UNIQUE", 6.0);
            case "ULTRA_EPIC":
            case "ULTRA EPIC":
                return config.getDouble("repair.cost-multipliers.ULTRA_EPIC", 3.0);
            case "EPIC":
                return config.getDouble("repair.cost-multipliers.EPIC", 2.5);
            case "ULTRA_RARE":
            case "ULTRA RARE":
                return config.getDouble("repair.cost-multipliers.ULTRA_RARE", 2.0);
            case "RARE":
                return config.getDouble("repair.cost-multipliers.RARE", 1.5);
            case "UNCOMMON":
                return config.getDouble("repair.cost-multipliers.UNCOMMON", 1.2);
            case "COMMON":
                return config.getDouble("repair.cost-multipliers.COMMON", 1.0);
            default:
                return 1.0; // Default multiplier
        }
    }

    // GUI settings
    public String getGuiTitle() {
        return config.getString("gui.title", "&8⚒ Repair Station ⚒");
    }

    public int getGuiSize() {
        return config.getInt("gui.size", 27);
    }

    public int getItemToRepairSlot() {
        return config.getInt("gui.slots.item-to-repair", 10);
    }

    public int getRepairMaterialsSlot() {
        return config.getInt("gui.slots.repair-materials", 12);
    }

    public int getRepairedPreviewSlot() {
        return config.getInt("gui.slots.repaired-preview", 16);
    }

    public int getRepairButtonSlot() {
        return config.getInt("gui.slots.repair-button", 14);
    }

    // Decoration settings
    public boolean isDecorationEnabled() {
        return config.getBoolean("gui.decoration.enabled", true);
    }

    public String getBackgroundMaterial() {
        return config.getString("gui.decoration.background.material", "gray_stained_glass_pane");
    }

    public String getBackgroundName() {
        return config.getString("gui.decoration.background.name", "&f");
    }

    // Debug settings
    public boolean isDebugEnabled() {
        return config.getBoolean("debug.enabled", false);
    }

    public boolean shouldLogRepairs() {
        return config.getBoolean("debug.log-repairs", true);
    }

    public boolean shouldLogMaterialChecks() {
        return config.getBoolean("debug.log-material-checks", false);
    }
}