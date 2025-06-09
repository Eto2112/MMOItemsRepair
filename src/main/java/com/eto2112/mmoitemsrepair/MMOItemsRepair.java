package com.eto2112.mmoitemsrepair;

import com.eto2112.mmoitemsrepair.commands.MainCommand;
import com.eto2112.mmoitemsrepair.commands.RepairCommand;
import com.eto2112.mmoitemsrepair.listeners.MenuListener;
import com.eto2112.mmoitemsrepair.managers.ConfigManager;
import com.eto2112.mmoitemsrepair.managers.LanguageManager;
import com.eto2112.mmoitemsrepair.managers.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MMOItemsRepair extends JavaPlugin {

    private static MMOItemsRepair instance;
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private MenuManager menuManager;

    @Override
    public void onEnable() {
        instance = this;

        // Check for MMOItems dependency
        if (!checkDependencies()) {
            getLogger().severe("MMOItems not found! Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize managers
        initializeManagers();

        // Register commands
        registerCommands();

        // Register listeners
        registerListeners();

        getLogger().info("MMOItemsRepair v" + getDescription().getVersion() + " has been enabled!");
        getLogger().info("Repair system loaded successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MMOItemsRepair has been disabled!");
    }

    private boolean checkDependencies() {
        return Bukkit.getPluginManager().getPlugin("MMOItems") != null;
    }

    private void initializeManagers() {
        configManager = new ConfigManager(this);
        languageManager = new LanguageManager(this);
        menuManager = new MenuManager(this);

        configManager.loadConfigs();
        languageManager.loadLanguage();
        menuManager.loadMenu();
    }

    private void registerCommands() {
        getCommand("repair").setExecutor(new RepairCommand(this));
        getCommand("mmoitemsrepair").setExecutor(new MainCommand(this));
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new MenuListener(this), this);
    }

    public void reload() {
        configManager.loadConfigs();
        languageManager.loadLanguage();
        menuManager.loadMenu();
    }

    // Getters
    public static MMOItemsRepair getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }
}