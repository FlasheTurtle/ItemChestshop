package com.itemchestshop.models;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ChestShop {
    
    private final Location signLocation;
    private final Location chestLocation;
    private final UUID owner;
    private final String ownerName;
    private final ItemStack wantItem;
    private final ItemStack giveItem;
    
    public ChestShop(Location signLocation, Location chestLocation, UUID owner, String ownerName, 
                     ItemStack wantItem, ItemStack giveItem) {
        this.signLocation = signLocation;
        this.chestLocation = chestLocation;
        this.owner = owner;
        this.ownerName = ownerName;
        this.wantItem = wantItem;
        this.giveItem = giveItem;
    }
    
    public Location getSignLocation() {
        return signLocation;
    }
    
    public Location getChestLocation() {
        return chestLocation;
    }
    
    public UUID getOwner() {
        return owner;
    }
    
    public String getOwnerName() {
        return ownerName;
    }
    
    public ItemStack getWantItem() {
        return wantItem;
    }
    
    public ItemStack getGiveItem() {
        return giveItem;
    }
    
    public boolean hasStock() {
        Block chestBlock = chestLocation.getBlock();
        if (chestBlock.getType() != Material.CHEST && chestBlock.getType() != Material.TRAPPED_CHEST && chestBlock.getType() != Material.BARREL) {
            return false;
        }

        Container container = (Container) chestBlock.getState();
        Inventory inventory = container.getInventory();
        
        return inventory.containsAtLeast(giveItem, giveItem.getAmount());
    }
    
    public boolean canAcceptPayment() {
        Block chestBlock = chestLocation.getBlock();
        if (chestBlock.getType() != Material.CHEST && chestBlock.getType() != Material.TRAPPED_CHEST && chestBlock.getType() != Material.BARREL) {
            return false;
        }

        Container container = (Container) chestBlock.getState();
        Inventory inventory = container.getInventory();
        
        // Check if chest has space for the payment items
        ItemStack[] contents = inventory.getContents();
        int availableSlots = 0;
        int neededSlots = 0;
        
        // Count available slots
        for (ItemStack item : contents) {
            if (item == null || item.getType() == Material.AIR) {
                availableSlots++;
            } else if (item.isSimilar(wantItem)) {
                int maxStack = item.getMaxStackSize();
                int currentAmount = item.getAmount();
                int canAdd = maxStack - currentAmount;
                if (canAdd >= wantItem.getAmount()) {
                    return true;
                }
            }
        }
        
        // Calculate needed slots for the payment
        int totalAmount = wantItem.getAmount();
        int maxStack = wantItem.getMaxStackSize();
        neededSlots = (int) Math.ceil((double) totalAmount / maxStack);
        
        return availableSlots >= neededSlots;
    }
}