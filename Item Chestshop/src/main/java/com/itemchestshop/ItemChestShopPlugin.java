package com.itemchestshop;

import com.itemchestshop.listeners.SignListener;
import com.itemchestshop.listeners.ChestShopListener;
import com.itemchestshop.commands.ItemChestShopCommand;
import com.itemchestshop.managers.ChestShopManager;
import com.itemchestshop.utils.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemChestShopPlugin extends JavaPlugin {
    
    private static ItemChestShopPlugin instance;
    private ChestShopManager chestShopManager;
    private ConfigManager configManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize managers
        configManager = new ConfigManager(this);
        chestShopManager = new ChestShopManager(this);
        
        // Register listeners
        this.getServer().getPluginManager().registerEvents(new SignListener(this), this);
        this.getServer().getPluginManager().registerEvents(new ChestShopListener(this), this);
        
        // Register commands
        this.getCommand("itemchestshop").setExecutor(new ItemChestShopCommand(this));
        
        this.getLogger().info("ItemChestShop has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Save all shops before disabling
        if (chestShopManager != null) {
            chestShopManager.saveShops();
            getLogger().info("Saved all chest shops to persistent storage");
        }
        
        getLogger().info("ItemChestShop has been disabled!");
    }
    
    public static ItemChestShopPlugin getInstance() {
        return instance;
    }
    
    public ChestShopManager getChestShopManager() {
        return chestShopManager;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
}