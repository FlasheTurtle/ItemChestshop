package com.itemchestshop.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemParser {

    /**
     * Parses an item string containing a material name and amount
     * into an ItemStack.
     * Supports both "Diamond 64" and "64 Diamond" formats.
     * @param itemString The string to parse
     * @return ItemStack or null if parsing fails
     */
    public static ItemStack parseItem(String itemString, ConfigManager configManager) {
        if (itemString == null || itemString.trim().isEmpty()) {
            return null;
        }

        String[] parts = itemString.trim().split("\\s+");
        if (parts.length != 2) {
            return null;
        }

        // Determine which part is the amount and which is the material name.
        int amount;
        String materialName;

        try {
            // Try the first part as the amount.
            // If successful, the second part must be the material name.
            amount = Integer.parseInt(parts[0]);
            materialName = parts[1].toUpperCase();
        } catch (NumberFormatException e) {
            // The first part was not a number, so treat it as the material name
            // and try the second part as the amount.
            materialName = parts[0].toUpperCase();


            try{
                amount = Integer.parseInt(parts[1]);
            } catch(NumberFormatException e2){
                // Neither part was a valid number
                return null;
            }

        }

        // Make sure the amount is within the valid Minecraft stack size.
        if (amount <= 0 || amount > 64) {
            return null;
        }

        // Get the material, make sure it is not null or air
        Material material = getMaterialFromString(materialName, configManager);;
        if (material == null || material == Material.AIR) {
            return null;
        }


        return new ItemStack(material, amount);
    }

    /**
     * Gets a Material from a string, handling configured aliases and common variations.
     *
     * @param materialName The material name to parse
     * @param configManager Provides configured item aliases
     * @return Material or null if not found
     */
    private static Material getMaterialFromString(String materialName, ConfigManager configManager){
        if (materialName != null && !materialName.trim().isEmpty()) {
            String input = materialName.trim().toUpperCase().replace(" ", "_");

            // Check config aliases first
            String alias = configManager.getItemAlias(input);

            if (alias != null) {
                try {
                    return Material.valueOf(alias.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Invalid configured alias; continue with normal parsing.
                }
            }

            try {
                return Material.valueOf(input);
            } catch (IllegalArgumentException var9) {
                String[] variations = new String[]{input, input + "_BLOCK", input + "_ITEM", input + "_ORE", input + "_INGOT", "RAW_" + input, input + "_SLAB", input + "_STAIRS", input + "_FENCE", input + "_DOOR", input + "_TRAPDOOR", input + "_BUTTON", input + "_PRESSURE_PLATE", input + "_WALL", "STRIPPED_" + input, input + "_WOOD", input + "_LOG", input + "_PLANKS", input + "_LEAVES", input + "_SAPLING"};
                String[] var3 = variations;
                int var4 = variations.length;
                int var5 = 0;

                while(var5 < var4) {
                    String variation = var3[var5];

                    try {
                        return Material.valueOf(variation);
                    } catch (IllegalArgumentException var8) {
                        ++var5;
                    }
                }

                Material[] var11 = Material.values();
                var5 = var11.length;

                Material material;
                int var12;
                for(var12 = 0; var12 < var5; ++var12) {
                    material = var11[var12];
                    if (material.isItem() && material.name().contains(input)) {
                        return material;
                    }
                }

                var11 = Material.values();
                var5 = var11.length;

                for(var12 = 0; var12 < var5; ++var12) {
                    material = var11[var12];
                    if (material.isItem() && input.contains(material.name())) {
                        return material;
                    }
                }

                return null;
            }
        } else {
            return null;
        }
    }



    /**
     * Converts an ItemStack to a readable string format
     * @param item The ItemStack to convert
     * @return String representation (e.g., "Diamond 1")
     */
    public static String itemToString(ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            String materialName = item.getType().name().toLowerCase();
            materialName = capitalizeWords(materialName.replace("_", " "));
            return materialName + " " + item.getAmount();
        } else {
            return "Air 0";
        }
    }

    /**
     * Capitalizes the first letter of each word
     * @param str The string to capitalize
     * @return Capitalized string
     */
    private static String capitalizeWords(String str) {
        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();

        for(int i = 0; i < words.length; ++i) {
            if (i > 0) {
                result.append(" ");
            }
            if (words[i].length() > 0) {
                result.append(Character.toUpperCase(words[i].charAt(0)));
                if (words[i].length() > 1) {
                    result.append(words[i].substring(1).toLowerCase());
                }
            }
        }

        return result.toString();
    }
}