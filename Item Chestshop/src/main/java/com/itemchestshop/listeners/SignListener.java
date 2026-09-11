package com.itemchestshop.listeners;

import com.itemchestshop.ItemChestShopPlugin;
import com.itemchestshop.managers.ChestShopManager;
import com.itemchestshop.models.ChestShop;
import com.itemchestshop.utils.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

public class SignListener implements Listener {

    private final ItemChestShopPlugin plugin;
    private final ChestShopManager chestShopManager;
    private final ConfigManager configManager;

    public SignListener(ItemChestShopPlugin plugin) {
        this.plugin = plugin;
        this.chestShopManager = plugin.getChestShopManager();
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String[] lines = event.getLines();

        // Check if this is a chest shop sign
        if (!isChestShopSign(lines)) {
            return;
        }

        // Check permissions
        if (!player.hasPermission("itemchestshop.create")) {
            player.sendMessage(Component.text(configManager.getMessage("no-permission-create"), NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        // Check if player has reached shop limit
        int maxShops = configManager.getMaxShopsPerPlayer();
        if (maxShops > 0 && chestShopManager.getShopCount(player.getUniqueId()) >= maxShops) {
            player.sendMessage(Component.text("You have reached the maximum number of shops (" + maxShops + ")!", NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        // Validate the shop format
        if (!chestShopManager.isValidShopFormat(lines)) {
            player.sendMessage(Component.text(configManager.getMessage("invalid-format"), NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        // Check if there's a chest below the sign
        Location signLocation = event.getBlock().getLocation();
        Location chestLocation = findChestBelowSign(signLocation);
        if (chestLocation == null) {
            player.sendMessage(Component.text(configManager.getMessage("no-chest-found"), NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        // Set the first line to the player's name if it's empty or matches their name
        if (lines[0].isEmpty() || lines[0].equalsIgnoreCase(player.getName())) {
            event.setLine(0, player.getName());
        } else {
            // If the first line doesn't match the player's name, cancel
            player.sendMessage(Component.text("The first line must be your username or empty!", NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        // Create the chest shop after the sign is placed
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            ChestShop chestShop = chestShopManager.createChestShop(
                    signLocation,
                    player.getUniqueId(),
                    player.getName(),
                    event.getLines()
            );

            if (chestShop != null) {
                player.sendMessage(Component.text(configManager.getMessage("shop-created"), NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("Failed to create chest shop!", NamedTextColor.RED));
            }
        }, 1L);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        // Check if a sign is being broken
        if (isSign(block.getType())) {
            Location signLocation = block.getLocation();
            ChestShop chestShop = chestShopManager.getChestShop(signLocation);

            if (chestShop != null) {
                // Check if player is the owner or has admin permission
                if (!chestShop.getOwner().equals(player.getUniqueId()) &&
                        !player.hasPermission("itemchestshop.admin")) {
                    player.sendMessage(Component.text("You can only break your own shop signs!", NamedTextColor.RED));
                    event.setCancelled(true);
                    return;
                }

                // Remove the chest shop
                chestShopManager.removeChestShop(signLocation);
                player.sendMessage(Component.text(configManager.getMessage("shop-removed"), NamedTextColor.GREEN));
            }
        }
    }

    /**
     * Checks if the sign lines represent a chest shop
     * @param lines The sign lines
     * @return true if it's a chest shop sign
     */
    private boolean isChestShopSign(String[] lines) {
        if (lines.length < 3) {
            return false;
        }

        // Check if line 1 starts with "Want:" and line 2 starts with "Give:"
        String wantLine = lines[1].toLowerCase();
        String giveLine = lines[2].toLowerCase();

        return wantLine.startsWith("want:") && giveLine.startsWith("give:");
    }

    /**
     * Finds a chest/barrel below the sign
     * @param signLocation The location of the sign
     * @return Location of the chest or null if not found
     */
    private Location findChestBelowSign(Location signLocation) {
        // Check the block directly below the sign
        Location belowSign = signLocation.clone().subtract(0, 1, 0);
        Block belowBlock = belowSign.getBlock();

        if (belowBlock.getType() == Material.CHEST || belowBlock.getType() == Material.TRAPPED_CHEST || belowBlock.getType() == Material.BARREL) {
            return belowSign;
        }

        // Check one more block below
        Location twoBelow = signLocation.clone().subtract(0, 2, 0);
        Block twoBelowBlock = twoBelow.getBlock();

        if (twoBelowBlock.getType() == Material.CHEST || twoBelowBlock.getType() == Material.TRAPPED_CHEST || twoBelowBlock.getType() == Material.BARREL) {
            return twoBelow;
        }

        return null;
    }

    /**
     * Checks if a material is a sign
     * @param material The material to check
     * @return true if it's a sign
     */
    private boolean isSign(Material material) {
        return material.name().contains("SIGN");
    }
}