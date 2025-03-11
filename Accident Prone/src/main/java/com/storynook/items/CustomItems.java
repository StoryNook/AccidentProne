package com.storynook.items;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomItems {

    public static boolean VailidUnderwear (ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) {
            return false;
        }

        int customModelData = meta.getCustomModelData();
        List<Integer> CustomItemIDs = Arrays.asList(626001, 626002, 626003, 626009);

        return CustomItemIDs.contains(customModelData);
    }

    public static boolean isCustomItem(ItemStack item) {
        if (item == null) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasCustomModelData()) {
            return false;
        }
        int customModelData = item.getItemMeta().getCustomModelData();
        List<Integer> CustomItemIDs = Arrays.asList(626001, 626002, 626003, 626004, 626005, 626009, 626011, 626010);
        
        return CustomItemIDs.contains(customModelData);
    }

    public static boolean isCustomPants(ItemStack item) {
        if (item == null || item.getType() != Material.LEATHER_LEGGINGS) {
            return false;
        }
        if (!item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if(meta.hasCustomModelData() && (
                meta.getCustomModelData() == 626015 ||  // Pants
                meta.getCustomModelData() == 626016 ||  // Pants Wet
                meta.getCustomModelData() == 626017 ||  // Pants Dirt
                meta.getCustomModelData() == 626018     // Pants Wet & Dirty
        )) return true;
        return false;
    }
    public static boolean isDiaper(ItemStack item) {
        if (item == null || item.getType() != Material.LEATHER_LEGGINGS) {
            return false;
        }
        if (!item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta.hasCustomModelData()) {
            int modelData = meta.getCustomModelData();
            Set<Integer> diaperModels = new HashSet<>(Arrays.asList(626001, 626002, 626003, 626009,
                    626022, 626023, 626024, 626025, 626026, 626027, 626028, 626029,
                    626030, 626031, 626032, 626033));
            return diaperModels.contains(modelData);
        }
        return false;
    }

    public static boolean isValidSmeltingItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) {
            return false;
        }

        int modelData = meta.getCustomModelData();

        // Check if the item is a slimeball with model data between 626001 and 626005
        if (item.getType() == Material.SLIME_BALL && (modelData == 626019 || 
        modelData == 626020 || 
        modelData == 626021)) {
            return true;
        }

        // Check if the item is leather leggings with any custom model data
        return item.getType() == Material.LEATHER_LEGGINGS;
    }
}
