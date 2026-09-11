package com.itemchestshop.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.itemchestshop.ItemChestShopPlugin;
import com.itemchestshop.models.ChestShop;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public class DataManager {
    
    private final ItemChestShopPlugin plugin;
    private final File dataFile;
    private final Gson gson;
    
    public DataManager(ItemChestShopPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "shops.json");
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Location.class, new LocationAdapter())
                .registerTypeAdapter(ItemStack.class, new ItemStackAdapter())
                .setPrettyPrinting()
                .create();
        
        // Create data folder if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
    }
    
    /**
     * Saves all chest shops to the data file
     * @param chestShops Map of chest shops to save
     */
    public void saveShops(Map<Location, ChestShop> chestShops) {
        try {
            Map<String, SerializableChestShop> serializableShops = new HashMap();
            Iterator var3 = chestShops.entrySet().iterator();

            while(var3.hasNext()) {
                Map.Entry<Location, ChestShop> entry = (Map.Entry)var3.next();
                String locationKey = this.locationToString((Location)entry.getKey());
                SerializableChestShop serializableShop = new SerializableChestShop((ChestShop)entry.getValue());
                serializableShops.put(locationKey, serializableShop);
            }

            FileWriter writer = new FileWriter(this.dataFile);

            try {
                this.gson.toJson(serializableShops, writer);
            } catch (Throwable var8) {
                try {
                    writer.close();
                } catch (Throwable var7) {
                    var8.addSuppressed(var7);
                }

                throw var8;
            }

            writer.close();
            Logger var10000 = this.plugin.getLogger();
            int var10001 = chestShops.size();
            var10000.info("Saved " + var10001 + " chest shops to " + this.dataFile.getName());
        } catch (IOException var9) {
            IOException e = var9;
            this.plugin.getLogger().log(Level.SEVERE, "Failed to save chest shops", e);
        }

    }
    
    /**
     * Loads all chest shops from the data file
     * @return Map of loaded chest shops
     */
    public Map<Location, ChestShop> loadShops() {
        Map<Location, ChestShop> chestShops = new HashMap();
        if (!this.dataFile.exists()) {
            this.plugin.getLogger().info("No existing shop data found, starting fresh");
            return chestShops;
        } else {
            try {
                FileReader reader = new FileReader(this.dataFile);

                try {
                    Type type = (new TypeToken<Map<String, SerializableChestShop>>() {
                    }).getType();
                    Map<String, SerializableChestShop> serializableShops = (Map)this.gson.fromJson(reader, type);
                    if (serializableShops != null) {
                        Iterator var5 = serializableShops.entrySet().iterator();

                        while(var5.hasNext()) {
                            Map.Entry<String, SerializableChestShop> entry = (Map.Entry)var5.next();

                            try {
                                Location location = this.stringToLocation((String)entry.getKey());
                                ChestShop chestShop = ((SerializableChestShop)entry.getValue()).toChestShop();
                                if (location != null && chestShop != null) {
                                    chestShops.put(location, chestShop);
                                }
                            } catch (Exception var10) {
                                Exception e = var10;
                                this.plugin.getLogger().log(Level.WARNING, "Failed to load shop at " + (String)entry.getKey(), e);
                            }
                        }
                    }
                } catch (Throwable var11) {
                    try {
                        reader.close();
                    } catch (Throwable var9) {
                        var11.addSuppressed(var9);
                    }

                    throw var11;
                }

                reader.close();
                Logger var10000 = this.plugin.getLogger();
                int var10001 = chestShops.size();
                var10000.info("Loaded " + var10001 + " chest shops from " + this.dataFile.getName());
            } catch (IOException var12) {
                IOException ee = var12;
                this.plugin.getLogger().log(Level.SEVERE, "Failed to load chest shops", ee);
            }

            return chestShops;
        }
    }
    
    /**
     * Converts a Location to a string representation
     */
    private String locationToString(Location location) {
        return location.getWorld().getName() + "," + 
               location.getX() + "," + 
               location.getY() + "," + 
               location.getZ() + "," + 
               location.getYaw() + "," + 
               location.getPitch();
    }
    
    /**
     * Converts a string representation back to a Location
     */
    private Location stringToLocation(String locationString) {
        try {
            String[] parts = locationString.split(",");
            if (parts.length != 6) {
                return null;
            }
            
            World world = Bukkit.getWorld(parts[0]);
            if (world == null) {
                return null;
            }
            
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);
            
            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Serializable version of ChestShop for JSON storage
     */
    private static class SerializableChestShop {
        private final String signLocation;
        private final String chestLocation;
        private final String owner;
        private final String ownerName;
        private final ItemStack wantItem;
        private final ItemStack giveItem;
        
        public SerializableChestShop(ChestShop chestShop) {
            this.signLocation = locationToString(chestShop.getSignLocation());
            this.chestLocation = locationToString(chestShop.getChestLocation());
            this.owner = chestShop.getOwner().toString();
            this.ownerName = chestShop.getOwnerName();
            this.wantItem = chestShop.getWantItem();
            this.giveItem = chestShop.getGiveItem();
        }
        
        public ChestShop toChestShop() {
            Location signLoc = stringToLocation(this.signLocation);
            Location chestLoc = stringToLocation(this.chestLocation);
            UUID ownerUUID = UUID.fromString(this.owner);
            
            if (signLoc == null || chestLoc == null) {
                return null;
            }
            
            return new ChestShop(signLoc, chestLoc, ownerUUID, ownerName, wantItem, giveItem);
        }
        
        private static String locationToString(Location location) {
            return location.getWorld().getName() + "," + 
                   location.getX() + "," + 
                   location.getY() + "," + 
                   location.getZ() + "," + 
                   location.getYaw() + "," + 
                   location.getPitch();
        }
        
        private static Location stringToLocation(String locationString) {
            try {
                String[] parts = locationString.split(",");
                if (parts.length != 6) {
                    return null;
                }
                
                World world = Bukkit.getWorld(parts[0]);
                if (world == null) {
                    return null;
                }
                
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                double z = Double.parseDouble(parts[3]);
                float yaw = Float.parseFloat(parts[4]);
                float pitch = Float.parseFloat(parts[5]);
                
                return new Location(world, x, y, z, yaw, pitch);
            } catch (Exception e) {
                return null;
            }
        }
    }
    
    /**
     * Custom adapter for Location serialization
     */
    private static class LocationAdapter implements JsonSerializer<Location>, JsonDeserializer<Location> {
        @Override
        public JsonElement serialize(Location location, Type type, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("world", location.getWorld().getName());
            obj.addProperty("x", location.getX());
            obj.addProperty("y", location.getY());
            obj.addProperty("z", location.getZ());
            obj.addProperty("yaw", location.getYaw());
            obj.addProperty("pitch", location.getPitch());
            return obj;
        }
        
        @Override
        public Location deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
            JsonObject obj = json.getAsJsonObject();
            World world = Bukkit.getWorld(obj.get("world").getAsString());
            if (world == null) return null;
            
            return new Location(
                world,
                obj.get("x").getAsDouble(),
                obj.get("y").getAsDouble(),
                obj.get("z").getAsDouble(),
                obj.get("yaw").getAsFloat(),
                obj.get("pitch").getAsFloat()
            );
        }
    }
    
    /**
     * Custom adapter for ItemStack serialization
     */
    private static class ItemStackAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {
        private ItemStackAdapter() {
        }

        public JsonElement serialize(ItemStack itemStack, Type type, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", itemStack.getType().name());
            obj.addProperty("amount", itemStack.getAmount());
            if (itemStack.hasItemMeta()) {
                obj.addProperty("meta", itemStack.getItemMeta().toString());
            }

            return obj;
        }

        public ItemStack deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
            JsonObject obj = json.getAsJsonObject();

            try {
                Material material = Material.valueOf(obj.get("type").getAsString());
                int amount = obj.get("amount").getAsInt();
                ItemStack itemStack = new ItemStack(material, amount);
                return itemStack;
            } catch (Exception var8) {
                return null;
            }
        }
    }
}