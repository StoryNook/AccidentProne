package com.storynook.items;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class pants {
    private JavaPlugin plugin;
    public static ItemStack cleanPants;
    public static ItemStack wetPants;
    public static ItemStack dirtyPants;
    public static ItemStack wetAndDirtyPants;
    public pants(JavaPlugin plugin){this.plugin = plugin;}
    public static void init(JavaPlugin plugin){
        //Create the non-craftable Items.
        // for (Material woolColor : Material.values()) {
        //     if (woolColor.name().endsWith("_WOOL")) {
        //         createPants(woolColor, 0, "Clean Pants");
        //         createPants(woolColor, 20, "Wet Pants");
        //         createPants(woolColor, 40, "Dirty Pants");
        //         createPants(woolColor, 60, "Wet and Dirty Pants");
        //         createCleanPantsRecipe(plugin, woolColor);
        //     }
        // }

        
    }
    private static void createPants(Material woolMaterial, int customModelOffset, String name) {
        ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS, 1);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setDisplayName(name);
        meta.setColor(getColorFromWool(woolMaterial));
        meta.setCustomModelData(626020 + customModelOffset + getColorIndex(woolMaterial));
        item.setItemMeta(meta);
    }

    private static void createCleanPantsRecipe(JavaPlugin plugin, Material woolMaterial) {
        ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setDisplayName(woolMaterial + " Pants");
        meta.setColor(getColorFromWool(woolMaterial));
        meta.setCustomModelData(626020 + getColorIndex(woolMaterial));
        item.setItemMeta(meta);

        // Define the recipe
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, woolMaterial.name().toLowerCase() + "_clean_pants"), item);
        recipe.shape("WWW", "W W", "W W"); // Crafting grid
        recipe.setIngredient('W', woolMaterial);
        plugin.getServer().addRecipe(recipe);
    }

    private static int getColorIndex(Material wool) {
        switch (wool) {
            case WHITE_WOOL: return 1;
            case ORANGE_WOOL: return 2;
            case PURPLE_WOOL: return 3;
            case BLACK_WOOL: return 4;
            case BLUE_WOOL: return 5;
            case YELLOW_WOOL: return 6;
            case RED_WOOL: return 7;
            case MAGENTA_WOOL : return 8;
            // Add more cases as needed for other colors
            default: return -1;
        }
    }

    private static Color getColorFromWool(Material wool) {
        switch (wool) {
            case WHITE_WOOL: return Color.WHITE;
            case ORANGE_WOOL: return Color.ORANGE;
            case PURPLE_WOOL: return Color.PURPLE;
            case BLACK_WOOL: return Color.BLACK;
            case BLUE_WOOL: return Color.BLUE;
            case YELLOW_WOOL: return Color.YELLOW;
            case RED_WOOL: return Color.RED;
            case MAGENTA_WOOL : return Color.fromRGB(255, 190, 73);


            // Add more cases as needed for other colors
            default: return null;
        }
    }

    public void createCleanPantsRecipe() {
        ItemStack item = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setDisplayName("Clean Pants");
        meta.setCustomModelData(626020);
        item.setItemMeta(meta);
        cleanPants = item;

        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "clean_pants"), item);
        recipe.shape("WWW", "W W", "W W"); // Crafting grid: W = Wool
        recipe.setIngredient('W', Material.WHITE_WOOL);
        Bukkit.addRecipe(recipe);
    }
}
