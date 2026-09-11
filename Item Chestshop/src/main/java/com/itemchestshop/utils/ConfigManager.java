package com.itemchestshop.utils;

import com.itemchestshop.ItemChestShopPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    
    private final ItemChestShopPlugin plugin;
    private FileConfiguration config;
    
    public ConfigManager(ItemChestShopPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    public String getMessage(String key) {
        return config.getString("messages." + key, "Message not found: " + key);
    }
    
    public boolean isDebugEnabled() {
        return config.getBoolean("debug", false);
    }
    
    public String getShopSignFormat() {
        return config.getString("shop-sign-format", "[SHOP]");
    }
    
    public int getMaxShopsPerPlayer() {
        return config.getInt("max-shops-per-player", 10);
    }

    public String getItemAlias(String itemName) {
        if (itemName == null || itemName.trim().isEmpty()) {
            return null;
        }

        return config.getString("item_aliases." + itemName.trim().toLowerCase());
    }
}