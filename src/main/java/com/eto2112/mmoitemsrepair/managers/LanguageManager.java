package com.eto2112.mmoitemsrepair.managers;

import com.eto2112.mmoitemsrepair.MMOItemsRepair;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class LanguageManager {

    private final MMOItemsRepair plugin;
    private FileConfiguration language;
    private File languageFile;

    public LanguageManager(MMOItemsRepair plugin) {
        this.plugin = plugin;
    }

    public void loadLanguage() {
        languageFile = new File(plugin.getDataFolder(), "language.yml");

        if (!languageFile.exists()) {
            createDefaultLanguageFile();
        }

        language = YamlConfiguration.loadConfiguration(languageFile);
    }

    private void createDefaultLanguageFile() {
        try {
            languageFile.createNewFile();
            FileConfiguration defaultLang = YamlConfiguration.loadConfiguration(languageFile);
            setDefaultLanguageValues(defaultLang);
            defaultLang.save(languageFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create language.yml", e);
        }
    }

    private void setDefaultLanguageValues(FileConfiguration lang) {
        // General messages
        lang.set("prefix", "&8[&6⚒Repair&8] ");
        lang.set("no-permission", "&cYou don't have permission to use this command!");
        lang.set("player-only", "&cThis command can only be used by players!");
        lang.set("reload-success", "&aConfiguration files reloaded successfully!");
        lang.set("version-info", "&aMMOItemsRepair v{version} by Eto2112");

        // Repair system messages
        lang.set("repair.no-item", "&cYou must be holding an item to repair!");
        lang.set("repair.not-mmoitem", "&cYou must be holding a valid MMOItem!");
        lang.set("repair.no-durability", "&cThis MMOItem doesn't have a durability system!");
        lang.set("repair.no-damage", "&aThis item is already at full durability!");
        lang.set("repair.menu-error", "&cFailed to open repair menu. Please try again.");
        lang.set("repair.success", "&a✓ Item repaired successfully!");
        lang.set("repair.failed", "&cRepair failed! Please try again.");
        lang.set("repair.insufficient-materials", "&cYou don't have enough repair materials!");
        lang.set("repair.materials-removed", "&7Consumed {amount} repair materials.");
        lang.set("repair.materials-needed", "&eRequired: &f{amount}x {material}");
        lang.set("repair.materials-found", "&aFound: &f{amount}x {material}");
        lang.set("repair.repair-cost", "&6Repair Cost: &f{materials} materials");
        lang.set("repair.durability-info", "&7Durability: &c{current}&7/&a{max} &7({percentage}%)");
        lang.set("repair.success-chance", "&7Success Rate: &a{rate}%");
        lang.set("repair.repair-risk", "&cWarning: Repair may fail!");

        // Menu messages
        lang.set("menu.item-to-repair.name", "&e📦 Item to Repair");
        lang.set("menu.item-to-repair.lore", java.util.Arrays.asList(
                "&7Place the item you want to repair here",
                "",
                "&7Current Durability: &c{current}&7/&a{max}",
                "&7Repair Needed: &f{missing} points"
        ));

        lang.set("menu.repair-materials.name", "&b🔧 Repair Materials");
        lang.set("menu.repair-materials.lore", java.util.Arrays.asList(
                "&7Materials needed for repair:",
                "&f• {amount}x {material}",
                "",
                "&7You have: &f{player_amount}x",
                "&7Status: {status}"
        ));

        lang.set("menu.repaired-preview.name", "&a✨ Repair Result");
        lang.set("menu.repaired-preview.lore", java.util.Arrays.asList(
                "&7Item after repair:",
                "",
                "&7Durability: &a{max}&7/&a{max} &7(100%)",
                "&aItem will be fully repaired!"
        ));

        lang.set("menu.repair-button.name", "&a⚒ REPAIR ITEM ⚒");
        lang.set("menu.repair-button.lore", java.util.Arrays.asList(
                "&7Click to repair your item",
                "",
                "&7Cost: &f{materials}x {material}",
                "&7Success Rate: &a{rate}%",
                "",
                "&eClick to repair!"
        ));

        lang.set("menu.error-item.name", "&cCannot Repair");
        lang.set("menu.error-item.lore", java.util.Arrays.asList(
                "&7This item cannot be repaired",
                "",
                "&cPossible reasons:",
                "&c• Not holding an MMOItem",
                "&c• Item has no durability system",
                "&c• Item is already at full durability"
        ));

        // Status messages
        lang.set("status.sufficient", "&a✓ Sufficient");
        lang.set("status.insufficient", "&c✗ Insufficient");
        lang.set("status.not-found", "&c✗ Not Found");

        // Admin messages
        lang.set("admin.debug-enabled", "&aDebug mode enabled");
        lang.set("admin.debug-disabled", "&cDebug mode disabled");
        lang.set("admin.repair-logged", "&7[DEBUG] Repair: {player} repaired {item} using {materials}x {material}");
    }

    public String getMessage(String path) {
        String message = language.getString(path, "&cMessage not found: " + path);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getMessage(String path, String placeholder, String replacement) {
        String message = getMessage(path);
        return message.replace(placeholder, replacement);
    }

    public String getMessage(String path, String... replacements) {
        String message = getMessage(path);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        return message;
    }

    public String getPrefix() {
        return getMessage("prefix");
    }

    public String getMessageWithPrefix(String path) {
        return getPrefix() + getMessage(path);
    }

    public String getMessageWithPrefix(String path, String placeholder, String replacement) {
        return getPrefix() + getMessage(path, placeholder, replacement);
    }

    public String getMessageWithPrefix(String path, String... replacements) {
        return getPrefix() + getMessage(path, replacements);
    }

    public void reloadLanguage() {
        language = YamlConfiguration.loadConfiguration(languageFile);
    }
}