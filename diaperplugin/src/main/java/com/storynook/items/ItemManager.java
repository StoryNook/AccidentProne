package com.storynook.items;


import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class ItemManager {
    private static JavaPlugin plugin;
    public static ItemStack diaperpail;
    public static ItemStack lax;
    // public static ItemStack dur;
    public static ItemStack toilet;
    public static ItemStack Washer;
    public ItemManager(JavaPlugin plugin){this.plugin = plugin;}

    //Recipes for crafting custom items
    public void createToiletRecipe() {
        // Define the custom item
        ItemStack item = new ItemStack(Material.CAULDRON);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Toilet");
        meta.setCustomModelData(626006); // Custom Model Data for texture
        item.setItemMeta(meta);
        toilet = item;

        // Create the recipe
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "Toilet"), toilet);
        recipe.shape(" T ", " C ", "   "); // Crafting grid: T = Trapdoor, C = Cauldron
        recipe.setIngredient('T', Material.IRON_TRAPDOOR); 
        recipe.setIngredient('C', Material.CAULDRON);

        // Register the recipe
        Bukkit.addRecipe(recipe);
    }
    // public void createDiaperPailRecipe() {
    //     // Define the custom item
    //     ItemStack item = new ItemStack(Material.BARREL, 1);
    //     ItemMeta meta = item.getItemMeta();
    //     meta.setDisplayName("Diaper Pail");
    //     item.setItemMeta(meta);
    //     diaperpail = item;

    //     // Create the recipe
    //     ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "DiaperPail"), diaperpail);
    //     recipe.shape(" T ", " B ", "   "); // Crafting grid: T = Trapdoor, C = Cauldron
    //     recipe.setIngredient('T', Material.IRON_TRAPDOOR); 
    //     recipe.setIngredient('B', Material.BARREL);

    //     // Register the recipe
    //     Bukkit.addRecipe(recipe);
    // }



    public void createWasherRecipe() {
        // Define the custom item
        ItemStack item = new ItemStack(Material.FURNACE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + "Washing Machine");
        meta.setCustomModelData(626014);// Custom Model Data for texture
        item.setItemMeta(meta);
        Washer = item;

            // Create the recipe
        ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(plugin, "Washer"), Washer);
        recipe.addIngredient(Material.FURNACE);
        recipe.addIngredient(Material.WATER_BUCKET);

        // Register the recipe
        Bukkit.addRecipe(recipe);
    }

    public void createLaxRecipe() {
        // Define the custom item
        ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Laxative");
        // List<String> lore = Arrays.asList(
        //         "Use to spike food items in the crafting table.",
        //         ChatColor.YELLOW + "NOTE:",
        //         "You can only use 1 food item and 1 laxative at a time.",
        //         "Increases the rate the bowels fills."
        //     );
        // meta.setLore(lore);
        meta.setCustomModelData(626012);
        item.setItemMeta(meta);
        lax = item;

        // Create the recipe
        ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(plugin, "Laxative"), lax);
        recipe.addIngredient(Material.ROTTEN_FLESH);

        // Register the recipe
        Bukkit.addRecipe(recipe);
    }
    
    public void createlaxedItem(){
        List<Material> foodMaterials = Arrays.asList(
            Material.SUSPICIOUS_STEW, Material.APPLE, Material.BAKED_POTATO,  Material.CAKE, Material.COOKED_BEEF, Material.COOKED_CHICKEN, 
            Material.COOKED_COD, Material.COOKED_MUTTON, Material.COOKED_PORKCHOP, Material.COOKED_RABBIT, Material.COOKED_SALMON,
            Material.GOLDEN_CARROT, Material.COOKIE, Material.GOLDEN_APPLE, Material.PUMPKIN_PIE,  Material.RABBIT_STEW,  Material.MUSHROOM_STEW,
            Material.BREAD, Material.BEETROOT_SOUP, Material.ENCHANTED_GOLDEN_APPLE
        );

        RecipeChoice.MaterialChoice foodChoice = new RecipeChoice.MaterialChoice(foodMaterials);

        ShapelessRecipe recipe = new ShapelessRecipe(
        new NamespacedKey(plugin, "LaxedItem"), createLaxedItem(foodMaterials.get(0)));
        

        // Create a custom recipe choice to check for slimeball with specific model data
        recipe.addIngredient(foodChoice);
        recipe.addIngredient(new RecipeChoice.ExactChoice(lax));
        ItemStack result = new ItemStack(foodMaterials.get(0)); // Use any food material as placeholder
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "laxative_effect"), PersistentDataType.BYTE, (byte) 1);
            result.setItemMeta(meta);
        }
        Bukkit.addRecipe(recipe);
    }
    
    public static ItemStack createLaxedItem(Material Food) {
        ItemStack item = new ItemStack(Food, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "laxative_effect"), PersistentDataType.BYTE, (byte) 1);
        }
        item.setItemMeta(meta);
        return item;
    }
    // public void createLDurRecipe() {
    //     // Define the custom item
    //     ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
    //     ItemMeta meta = item.getItemMeta();
    //     meta.setDisplayName("Diuretic");
    //     meta.setCustomModelData(626013);
    //     item.setItemMeta(meta);
    //     dur = item;

    //     // Create the recipe
    //     ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(plugin, "Diuretic"), dur);

    //     recipe.addIngredient('W', Material.MELON_SLICE);

    //     // Register the recipe
    //     Bukkit.addRecipe(recipe);
    // }
}
