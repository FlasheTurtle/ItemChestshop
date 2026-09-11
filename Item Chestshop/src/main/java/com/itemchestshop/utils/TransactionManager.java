package com.itemchestshop.utils;

import com.itemchestshop.models.ChestShop;

import org.bukkit.Material;
import org.bukkit.block.Block;

import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TransactionManager {
    
    /**
     * Processes a transaction between a player and a chest shop
     * @param player The player making the purchase
     * @param chestShop The chest shop
     * @return TransactionResult indicating success or failure
     */
    public static TransactionResult processTransaction(Player player, ChestShop chestShop) {
        // Check if player is trying to buy from their own shop
        if (chestShop.getOwner().equals(player.getUniqueId())) {
            return new TransactionResult(false, "cannot-buy-own-shop");
        }
        
        // Check if player has enough items to pay
        if (!playerHasItems(player, chestShop.getWantItem())) {
            return new TransactionResult(false, "insufficient-items");
        }
        
        // Check if shop has enough stock
        if (!chestShop.hasStock()) {
            return new TransactionResult(false, "insufficient-stock");
        }
        
        // Check if shop can accept payment
        if (!chestShop.canAcceptPayment()) {
            return new TransactionResult(false, "shop-full");
        }
        
        // Get the chest inventory
        Block chestBlock = chestShop.getChestLocation().getBlock();
        if (chestBlock.getType() != Material.CHEST && chestBlock.getType() != Material.TRAPPED_CHEST && chestBlock.getType() != Material.BARREL) {
            return new TransactionResult(false, "no-container-found");
        }

        Container container = (Container) chestBlock.getState();
        Inventory inventory = container.getInventory();
        
        // Perform the transaction
        try {
            // Remove payment from player
            removeItemsFromPlayer(player, chestShop.getWantItem());
            
            // Add payment to chest
            addItemsToInventory(inventory, chestShop.getWantItem());
            
            // Remove items from chest
            removeItemsFromInventory(inventory, chestShop.getGiveItem());
            
            // Give items to player
            addItemsToPlayer(player, chestShop.getGiveItem());
            
            return new TransactionResult(true, "transaction-success");
            
        } catch (Exception e) {
            // If something goes wrong, try to revert (this is a simple approach)
            return new TransactionResult(false, "transaction-failed");
        }
    }
    
    /**
     * Checks if a player has enough of the specified items
     * @param player The player to check
     * @param requiredItem The required item and amount
     * @return true if player has enough items
     */
    private static boolean playerHasItems(Player player, ItemStack requiredItem) {
        return player.getInventory().containsAtLeast(requiredItem, requiredItem.getAmount());
    }
    
    /**
     * Removes items from a player's inventory
     * @param player The player
     * @param items The items to remove
     */
    private static void removeItemsFromPlayer(Player player, ItemStack items) {
        Inventory inventory = player.getInventory();
        int amountToRemove = items.getAmount();

        for(int i = 0; i < inventory.getSize() && amountToRemove > 0; ++i) {
            ItemStack slot = inventory.getItem(i);
            if (slot != null && slot.isSimilar(items)) {
                int slotAmount = slot.getAmount();
                if (slotAmount <= amountToRemove) {
                    inventory.setItem(i, (ItemStack)null);
                    amountToRemove -= slotAmount;
                } else {
                    slot.setAmount(slotAmount - amountToRemove);
                    amountToRemove = 0;
                }
            }
        }

    }
    /**
     * Adds items to a player's inventory
     * @param player The player
     * @param items The items to add
     */
    private static void addItemsToPlayer(Player player, ItemStack items) {
        Inventory inventory = player.getInventory();
        ItemStack toAdd = items.clone();

        int i;
        ItemStack slot;
        int maxStack;
        int amountToAdd;
        for(i = 0; i < inventory.getSize() && toAdd.getAmount() > 0; ++i) {
            slot = inventory.getItem(i);
            if (slot != null && slot.isSimilar(toAdd)) {
                maxStack = slot.getMaxStackSize();
                amountToAdd = slot.getAmount();
                int canAdd = Math.min(maxStack - amountToAdd, toAdd.getAmount());
                if (canAdd > 0) {
                    slot.setAmount(amountToAdd + canAdd);
                    toAdd.setAmount(toAdd.getAmount() - canAdd);
                }
            }
        }

        for(i = 0; i < inventory.getSize() && toAdd.getAmount() > 0; ++i) {
            slot = inventory.getItem(i);
            if (slot == null || slot.getType() == Material.AIR) {
                maxStack = toAdd.getMaxStackSize();
                amountToAdd = Math.min(maxStack, toAdd.getAmount());
                ItemStack newStack = toAdd.clone();
                newStack.setAmount(amountToAdd);
                inventory.setItem(i, newStack);
                toAdd.setAmount(toAdd.getAmount() - amountToAdd);
            }
        }

        if (toAdd.getAmount() > 0) {
            player.getWorld().dropItemNaturally(player.getLocation(), toAdd);
        }

    }
    
    /**
     * Adds items to an inventory
     * @param inventory The inventory
     * @param items The items to add
     */
    private static void addItemsToInventory(Inventory inventory, ItemStack items) {
        ItemStack toAdd = items.clone();

        int i;
        ItemStack slot;
        int maxStack;
        int amountToAdd;
        for(i = 0; i < inventory.getSize() && toAdd.getAmount() > 0; ++i) {
            slot = inventory.getItem(i);
            if (slot != null && slot.isSimilar(toAdd)) {
                maxStack = slot.getMaxStackSize();
                amountToAdd = slot.getAmount();
                int canAdd = Math.min(maxStack - amountToAdd, toAdd.getAmount());
                if (canAdd > 0) {
                    slot.setAmount(amountToAdd + canAdd);
                    toAdd.setAmount(toAdd.getAmount() - canAdd);
                }
            }
        }

        for(i = 0; i < inventory.getSize() && toAdd.getAmount() > 0; ++i) {
            slot = inventory.getItem(i);
            if (slot == null || slot.getType() == Material.AIR) {
                maxStack = toAdd.getMaxStackSize();
                amountToAdd = Math.min(maxStack, toAdd.getAmount());
                ItemStack newStack = toAdd.clone();
                newStack.setAmount(amountToAdd);
                inventory.setItem(i, newStack);
                toAdd.setAmount(toAdd.getAmount() - amountToAdd);
            }
        }

    }
    
    /**
     * Removes items from an inventory
     * @param inventory The inventory
     * @param items The items to remove
     */
    private static void removeItemsFromInventory(Inventory inventory, ItemStack items) {
        int amountToRemove = items.getAmount();
        
        for (int i = 0; i < inventory.getSize() && amountToRemove > 0; i++) {
            ItemStack slot = inventory.getItem(i);
            if (slot != null && slot.isSimilar(items)) {
                int slotAmount = slot.getAmount();
                if (slotAmount <= amountToRemove) {
                    inventory.setItem(i, null);
                    amountToRemove -= slotAmount;
                } else {
                    slot.setAmount(slotAmount - amountToRemove);
                    amountToRemove = 0;
                }
            }
        }
    }
    
    /**
     * Result of a transaction
     */
    public static class TransactionResult {
        private final boolean success;
        private final String messageKey;
        
        public TransactionResult(boolean success, String messageKey) {
            this.success = success;
            this.messageKey = messageKey;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessageKey() {
            return messageKey;
        }
    }
}