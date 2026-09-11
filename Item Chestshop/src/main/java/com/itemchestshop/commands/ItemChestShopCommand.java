package com.itemchestshop.commands;

import com.itemchestshop.ItemChestShopPlugin;
import com.itemchestshop.managers.ChestShopManager;
import com.itemchestshop.models.ChestShop;
import com.itemchestshop.utils.ConfigManager;
import com.itemchestshop.utils.ItemParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemChestShopCommand implements CommandExecutor {
    
    private final ItemChestShopPlugin plugin;
    private final ChestShopManager chestShopManager;
    private final ConfigManager configManager;
    
    public ItemChestShopCommand(ItemChestShopPlugin plugin) {
        this.plugin = plugin;
        this.chestShopManager = plugin.getChestShopManager();
        this.configManager = plugin.getConfigManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help":
                showHelp(sender);
                break;
            case "reload":
                if (!sender.hasPermission("itemchestshop.admin")) {
                    sender.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
                    return true;
                }
                reloadConfig(sender);
                break;
            case "info":
                showInfo(sender);
                break;
            case "stats":
                if (sender instanceof Player) {
                    showPlayerStats(sender, (Player) sender);
                } else {
                    sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
                }
                break;
            case "list":
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Component.text("Invalid page number: " + args[1], NamedTextColor.RED));
                        return true;
                    }
                }
                showShopList(sender, page);
                break;
            case "find":
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /itemchestshop find <item>", NamedTextColor.RED));
                    return true;
                }
                findShops(sender, args[1]);
                break;
            default:
                showHelp(sender);
                break;
        }
        
        return true;
    }
    
    /**
     * Shows help information
     * @param sender The command sender
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== ItemChestShop Commands ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/itemchestshop help", NamedTextColor.YELLOW)
            .append(Component.text(" - Show this help", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/itemchestshop info", NamedTextColor.YELLOW)
            .append(Component.text(" - Show plugin info", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/itemchestshop stats", NamedTextColor.YELLOW)
            .append(Component.text(" - Show your shop statistics", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/itemchestshop list [page]", NamedTextColor.YELLOW)
            .append(Component.text(" - Browse all shops", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("/itemchestshop find <item>", NamedTextColor.YELLOW)
            .append(Component.text(" - Find shops selling an item", NamedTextColor.WHITE)));
        
        if (sender.hasPermission("itemchestshop.admin")) {
            sender.sendMessage(Component.text("/itemchestshop reload", NamedTextColor.YELLOW)
                .append(Component.text(" - Reload configuration", NamedTextColor.WHITE)));
        }
        
        sender.sendMessage(Component.text("", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("To create a shop:", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("1. Place a chest", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("2. Place a sign on or above the chest", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("3. Write on the sign:", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("   Line 1: Your username (or leave empty)", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("   Line 2: Want: [amount] [item]", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("   Line 3: Give: [amount] [item]", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Example:", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("VydeogamzXD", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Want: 64 Bread", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Give: 1 Diamond", NamedTextColor.GRAY));
    }
    
    /**
     * Shows plugin information
     * @param sender The command sender
     */
    private void showInfo(CommandSender sender) {
        sender.sendMessage(Component.text("=== ItemChestShop Plugin Info ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Version: ", NamedTextColor.YELLOW)
            .append(Component.text(plugin.getDescription().getVersion(), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Author: ", NamedTextColor.YELLOW)
            .append(Component.text("VydeogamzXD", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Description: ", NamedTextColor.YELLOW)
            .append(Component.text(plugin.getDescription().getDescription(), NamedTextColor.WHITE)));
        
        int totalShops = chestShopManager.getAllChestShops().size();
        sender.sendMessage(Component.text("Total Shops: ", NamedTextColor.YELLOW)
            .append(Component.text(String.valueOf(totalShops), NamedTextColor.WHITE)));
    }
    
    /**
     * Shows player statistics
     * @param sender The command sender
     * @param player The player
     */
    private void showPlayerStats(CommandSender sender, Player player) {
        List<ChestShop> playerShops = new ArrayList<>();
        for (ChestShop shop : chestShopManager.getAllChestShops().values()) {
            if (shop.getOwner().equals(player.getUniqueId())) {
                playerShops.add(shop);
            }
        }
        
        player.sendMessage(Component.text("=== Your Shop Statistics ===", NamedTextColor.GOLD));
        player.sendMessage(Component.text("Total Shops: ", NamedTextColor.YELLOW)
            .append(Component.text(String.valueOf(playerShops.size()), NamedTextColor.WHITE)));
        
        if (playerShops.isEmpty()) {
            player.sendMessage(Component.text("You don't have any shops yet!", NamedTextColor.GRAY));
            player.sendMessage(Component.text("Use ", NamedTextColor.GRAY)
                .append(Component.text("/itemchestshop help", NamedTextColor.YELLOW))
                .append(Component.text(" to learn how to create one.", NamedTextColor.GRAY)));
            return;
        }
        
        player.sendMessage(Component.text("Your Shops:", NamedTextColor.YELLOW));
        for (int i = 0; i < Math.min(playerShops.size(), 5); i++) {
            ChestShop shop = playerShops.get(i);
            String location = shop.getSignLocation().getWorld().getName() + " " + 
                            shop.getSignLocation().getBlockX() + "," + 
                            shop.getSignLocation().getBlockY() + "," + 
                            shop.getSignLocation().getBlockZ();
            
            player.sendMessage(Component.text("" + (i + 1) + ". ", NamedTextColor.GRAY)
                .append(Component.text(shop.getWantItem() + " → " + shop.getGiveItem(), NamedTextColor.WHITE))
                .append(Component.text(" at " + location, NamedTextColor.GRAY)));
        }
        
        if (playerShops.size() > 5) {
            player.sendMessage(Component.text("... and " + (playerShops.size() - 5) + " more shops", NamedTextColor.GRAY));
        }
    }
    
    /**
     * Reloads the configuration
     * @param sender The command sender
     */
    private void reloadConfig(CommandSender sender) {
        try {
            configManager.loadConfig();
            sender.sendMessage(Component.text("Configuration reloaded successfully!", NamedTextColor.GREEN));
        } catch (Exception e) {
            sender.sendMessage(Component.text("Failed to reload configuration: " + e.getMessage(), NamedTextColor.RED));
        }
    }
    
    /**
     * Shows a paginated list of all shops
     * @param sender The command sender
     * @param page The page number (1-based)
     */
    private void showShopList(CommandSender sender, int page) {
        Map<Location, ChestShop> allShops = chestShopManager.getAllChestShops();
        
        if (allShops.isEmpty()) {
            sender.sendMessage(Component.text("No shops found on this server.", NamedTextColor.YELLOW));
            return;
        }
        
        List<ChestShop> shopList = new ArrayList<>(allShops.values());
        int shopsPerPage = 10;
        int totalPages = (int) Math.ceil((double) shopList.size() / shopsPerPage);
        
        if (page < 1 || page > totalPages) {
            sender.sendMessage(Component.text("Invalid page number. Valid pages: 1-" + totalPages, NamedTextColor.RED));
            return;
        }
        
        int startIndex = (page - 1) * shopsPerPage;
        int endIndex = Math.min(startIndex + shopsPerPage, shopList.size());
        
        sender.sendMessage(Component.text("=== Shop List (Page " + page + "/" + totalPages + ") ===", NamedTextColor.GOLD));
        
        for (int i = startIndex; i < endIndex; i++) {
            ChestShop shop = shopList.get(i);
            Location loc = shop.getSignLocation();
            String ownerName = plugin.getServer().getOfflinePlayer(shop.getOwner()).getName();
            
            // Format shop info
            String wantItem = formatItemName(shop.getWantItem());
            String giveItem = formatItemName(shop.getGiveItem());
            
            sender.sendMessage(Component.text("" + (i + 1) + ". ", NamedTextColor.YELLOW)
                .append(Component.text(ownerName + "'s shop", NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("   Want: ", NamedTextColor.GRAY)
                .append(Component.text("" + shop.getWantItem().getAmount() + "x " + wantItem, NamedTextColor.GREEN)));
            sender.sendMessage(Component.text("   Give: ", NamedTextColor.GRAY)
                .append(Component.text("" + shop.getGiveItem().getAmount() + "x " + giveItem, NamedTextColor.AQUA)));
            sender.sendMessage(Component.text("   Location: " + loc.getWorld().getName() + " (" + 
                loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")", NamedTextColor.GRAY));
        }
        
        if (page < totalPages) {
            sender.sendMessage(Component.text("Use ", NamedTextColor.GRAY)
                .append(Component.text("/itemchestshop list " + (page + 1), NamedTextColor.YELLOW))
                .append(Component.text(" for the next page.", NamedTextColor.GRAY)));
        }
    }
    
    /**
     * Finds shops that sell a specific item
     * @param sender The command sender
     * @param itemName The item to search for
     */
    private void findShops(CommandSender sender, String itemName) {
        ItemStack searchItem = ItemParser.parseItem(itemName, plugin.getConfigManager());
        
        if (searchItem == null) {
            sender.sendMessage(Component.text("Invalid item: " + itemName, NamedTextColor.RED));
            return;
        }
        
        Map<Location, ChestShop> allShops = chestShopManager.getAllChestShops();
        List<ChestShop> matchingShops = new ArrayList<>();
        
        for (ChestShop shop : allShops.values()) {
            if (shop.getGiveItem().getType() == searchItem.getType()) {
                matchingShops.add(shop);
            }
        }
        
        if (matchingShops.isEmpty()) {
            sender.sendMessage(Component.text("No shops found selling " + formatItemName(searchItem) + ".", NamedTextColor.YELLOW));
            return;
        }
        
        sender.sendMessage(Component.text("=== Shops selling " + formatItemName(searchItem) + " ===", NamedTextColor.GOLD));
        
        for (int i = 0; i < Math.min(matchingShops.size(), 15); i++) {
            ChestShop shop = matchingShops.get(i);
            Location loc = shop.getSignLocation();
            String ownerName = plugin.getServer().getOfflinePlayer(shop.getOwner()).getName();
            
            String wantItem = formatItemName(shop.getWantItem());
            
            sender.sendMessage(Component.text("" + (i + 1) + ". ", NamedTextColor.YELLOW)
                .append(Component.text(ownerName + "'s shop", NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("   Wants: ", NamedTextColor.GRAY)
                .append(Component.text("" + shop.getWantItem().getAmount() + "x " + wantItem, NamedTextColor.GREEN)));
            sender.sendMessage(Component.text("   Gives: ", NamedTextColor.GRAY)
                .append(Component.text("" + shop.getGiveItem().getAmount() + "x " + formatItemName(shop.getGiveItem()), NamedTextColor.AQUA)));
            sender.sendMessage(Component.text("   Location: " + loc.getWorld().getName() + " (" + 
                loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")", NamedTextColor.GRAY));
        }
        
        if (matchingShops.size() > 15) {
            sender.sendMessage(Component.text("... and " + (matchingShops.size() - 15) + " more shops.", NamedTextColor.GRAY));
        }
    }
    
    /**
     * Formats an item name for display
     * @param item The item to format
     * @return The formatted item name
     */
    private String formatItemName(ItemStack item) {
        String name = item.getType().name().toLowerCase().replace('_', ' ');
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}