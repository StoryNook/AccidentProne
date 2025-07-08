package com.storynook.items;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class underwear {
    private JavaPlugin plugin;
    public static ItemStack diaper;
    public static ItemStack underwear;
    public static ItemStack thickdiaper;
    public static ItemStack pullup;
    public static ItemStack DiaperStuffer;
    public static ItemStack Tape;
    public underwear(JavaPlugin plugin){this.plugin = plugin;}

    public void createAllRecipes() {

        createTapeRecipe();
        createDiaperStufferRecipe();
        createDiaperRecipe();
        createPullupRecipe();
        createThickDiaperRecipe();
        createUnderwearRecipe();
        WashedUnderwear();

    }

    public void createDiaperStufferRecipe() {
        // Define the custom item
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + "Diaper Stuffer");
        meta.setCustomModelData(626007);// Custom Model Data for texture
        item.setItemMeta(meta);
        DiaperStuffer = item;

         // Create the recipe
        ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(plugin, "DiaperStuffer"), DiaperStuffer);
        recipe.addIngredient(Material.WHITE_WOOL);

        // Register the recipe
        Bukkit.addRecipe(recipe);
    }
    public void createTapeRecipe() {
        // Define the custom item
        ItemStack item = new ItemStack(Material.SLIME_BALL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Tape");
        meta.setCustomModelData(626008);// Custom Model Data for texture
        item.setItemMeta(meta);
        Tape = item;

        // Create the recipe
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "Tape"), Tape);
        recipe.shape("S S", "P P", "   "); 
        recipe.setIngredient('S', new RecipeChoice.MaterialChoice(Material.SLIME_BALL, Material.HONEY_BOTTLE));
        // recipe.setIngredient('S', Material.SLIME_BALL); 
        recipe.setIngredient('P', Material.PAPER);

        // Register the recipe
        Bukkit.addRecipe(recipe);
    }
    public static ItemStack Diaper(){
        ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Diaper");
        meta.setCustomModelData(626009);
        item.setItemMeta(meta);
        diaper = item;
        return item;
    }

    public void createDiaperRecipe() {
        Diaper();
        // Create the recipe
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "Diaper"), diaper);
        recipe.shape("TST", " S ", "PSP"); 
        recipe.setIngredient('S', new RecipeChoice.ExactChoice(DiaperStuffer));
        // recipe.setIngredient('S', new RecipeChoice.MaterialChoice(DiaperStuffer.getType()));
        recipe.setIngredient('P', Material.PAPER);
        recipe.setIngredient('T', new RecipeChoice.ExactChoice(Tape));

        // Register the recipe
        Bukkit.addRecipe(recipe);
    }
    public static ItemStack ThickDiaper(){
        ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Thick Diaper");
        meta.setCustomModelData(626001);
        item.setItemMeta(meta);
        thickdiaper = item;
        return item;
    }
    
    public void createThickDiaperRecipe() {
        ThickDiaper();
        // Create the recipe
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "ThickDiaper"), thickdiaper);
        recipe.shape("TST", "SSS", "PSP"); 
        recipe.setIngredient('S', new RecipeChoice.ExactChoice(DiaperStuffer));
        recipe.setIngredient('P', Material.PAPER);
        recipe.setIngredient('T', new RecipeChoice.ExactChoice(Tape));

        // Register the recipe
        Bukkit.addRecipe(recipe);
    }
    public static ItemStack Pullup(){
        ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Pullup");
        meta.setCustomModelData(626003);
        item.setItemMeta(meta);
        pullup = item;
        return item;
    }
    
    public void createPullupRecipe() {
        Pullup();
        // Define the custom item
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "Pullup"), pullup);
        recipe.shape("W W", " S ", "PSP"); 
        recipe.setIngredient('S', new RecipeChoice.ExactChoice(DiaperStuffer));
        recipe.setIngredient('W', Material.WHITE_WOOL);
        recipe.setIngredient('P', Material.PAPER);

        // Register the recipe
        Bukkit.addRecipe(recipe);
    }
    public static ItemStack Underwear(){
        ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Underwear");
        meta.setCustomModelData(626002);
        item.setItemMeta(meta);
        underwear = item;
        return item;
    }
    public void createUnderwearRecipe() {
        Underwear();
        // Create the recipe
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "Underwear"), underwear);
        recipe.shape("   ", "WWW", " W ");
        recipe.setIngredient('W', Material.WHITE_WOOL);

        // Register the recipe
        Bukkit.addRecipe(recipe);
    }
    private static StringBuilder Cleantime(int timeworn){
        int totalHours = (int) (timeworn / 20); // Total hours based on the assumption that each tick is approximately 1/20th of a second
        int months = totalHours / (30 * 24);
        int weeks = (totalHours % (30 * 24)) / (7 * 24);
        int days = ((totalHours % (30 * 24)) % (7 * 24)) / 24;
        int hours = ((totalHours % (30 * 24)) % (7 * 24)) % 24;

        // Output the time worn in a readable format
        StringBuilder timeWornDisplay = new StringBuilder();
        if (months > 0) {
            timeWornDisplay.append(months).append(" month").append(months > 1 ? "s" : "").append(" ");
        }
        if (weeks > 0 || (months == 0 && weeks == 0 && days == 0)) { // Include weeks only if not zero or if no months are present
            timeWornDisplay.append(weeks).append(" week").append(weeks > 1 ? "s" : "").append(" ");
        }
        if (days > 0 || (months == 0 && weeks == 0)) { // Include days only if not zero or if no months and no weeks are present
            timeWornDisplay.append(days).append(" day").append(days > 1 ? "s" : "").append(" ");
        }
        if (hours > 0 || (months == 0 && weeks == 0 && days == 0)) { // Include hours only if not zero or if no months, no weeks, and no days are present
            timeWornDisplay.append(hours).append(" hour").append(hours > 1 ? "s" : "").append(" ");
        }
        return timeWornDisplay;
    }
    
    public static ItemStack createStinkyDiaper(Player owner, int wetness, int fullness, int timeworn){
        // Define the custom item
        ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Stinky Diaper");
        meta.setCustomModelData(626004);
        List<String> lore = Arrays.asList(
                    "Owner: " + ChatColor.AQUA + owner.getDisplayName(),
                    "Usage: " + ChatColor.GREEN + Math.max(fullness, wetness),
                    "Time Worn: " + ChatColor.YELLOW + Cleantime(timeworn)
            );
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack createWetDiaper(Player owner, int wetness, int fullness, int timeworn){
        // Define the custom item
        ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Wet Diaper");
        meta.setCustomModelData(626005);
        List<String> lore = Arrays.asList(
                    "Owner: " + ChatColor.AQUA + owner.getDisplayName(),
                    "Usage: " + ChatColor.GREEN + Math.max(fullness, wetness),
                    "Time Worn: " + ChatColor.YELLOW + Cleantime(timeworn)
                );
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack createWetPullup(Player owner, int wetness, int fullness, int timeworn){
        // Define the custom item
        ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Wet Pullup");
        meta.setCustomModelData(626010);
        List<String> lore = Arrays.asList(
                    "Owner: " + ChatColor.AQUA + owner.getDisplayName(),
                    "Usage: " + ChatColor.GREEN + Math.max(fullness, wetness),
                    "Time Worn: " + ChatColor.YELLOW + Cleantime(timeworn)
                );
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack createWetThickDiaper(Player owner, int wetness, int fullness, int timeworn){
        // Define the custom item
        ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Wet Thick Diaper");
        meta.setCustomModelData(626011);
        List<String> lore = Arrays.asList(
                    "Owner: " + ChatColor.AQUA + owner.getDisplayName(),
                    "Usage: " + ChatColor.GREEN + Math.max(fullness, wetness),
                    "Time Worn: " + ChatColor.YELLOW + Cleantime(timeworn)
                );
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack createWetUndies(Player owner, int wetness, int fullness, int timeworn){
        // Define the custom item
        ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Wet Undies");
        meta.setCustomModelData(626019);
        List<String> lore = Arrays.asList(
                    "Owner: " + ChatColor.AQUA + owner.getDisplayName(),
                    "Usage: " + ChatColor.GREEN + Math.max(fullness, wetness),
                    "Time Worn: " + ChatColor.YELLOW + Cleantime(timeworn)
                );
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack createDirtyUndies(Player owner, int wetness, int fullness, int timeworn){
        // Define the custom item
        ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Dirty Undies");
        meta.setCustomModelData(626020);
        List<String> lore = Arrays.asList(
                    "Owner: " + ChatColor.AQUA + owner.getDisplayName(),
                    "Usage: " + ChatColor.GREEN + Math.max(fullness, wetness),
                    "Time Worn: " + ChatColor.YELLOW + Cleantime(timeworn)
                );
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack createWetANDDirtyUndies(Player owner, int wetness, int fullness, int timeworn){
        // Define the custom item
        ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Ruined Undies");
        meta.setCustomModelData(626021);
        List<String> lore = Arrays.asList(
                    "Owner: " + ChatColor.AQUA + owner.getDisplayName(),
                    "Usage: " + ChatColor.GREEN + Math.max(fullness, wetness),
                    "Time Worn: " + ChatColor.YELLOW + Cleantime(timeworn)
                );
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    public void WashedUnderwear(){
        NamespacedKey key = new NamespacedKey(plugin, "WashedUnderwear");
        ItemStack result = new ItemStack(Material.SLIME_BALL);
        FurnaceRecipe recipe = new FurnaceRecipe(key, result, Material.SLIME_BALL, 0.0f, 200);
        Bukkit.addRecipe(recipe);
    }
}
