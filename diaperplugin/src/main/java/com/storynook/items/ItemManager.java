package com.storynook.items;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class ItemManager {
    private JavaPlugin plugin;
    public static ItemStack diaperpail;
    public static ItemStack diaper;
    // public static ItemStack stinkydiaper;
    // public static ItemStack wetdiaper;
    // public static ItemStack wetpullup;
    // public static ItemStack wetthickdiaper;
    public static ItemStack underwear;
    public static ItemStack thickdiaper;
    public static ItemStack pullup;
    public static ItemStack cleanPants;
    public static ItemStack wetPants;
    public static ItemStack dirtyPants;
    public static ItemStack wetAndDirtyPants;
    // public static ItemStack lax;
    // public static ItemStack dur;
    public static ItemStack toilet;
    public static ItemStack DiaperStuffer;
    public static ItemStack Tape;
    // public static ItemStack Washer;
    public ItemManager(JavaPlugin plugin){this.plugin = plugin;}
    public static void init(JavaPlugin plugin){
        //Create the non-craftable Items.
        // createWetDiaper();
        // createStinkyDiaper();
        // createWetPullup();
        // createWetThickDiaper();
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
                        "Useage: " + ChatColor.GREEN + Math.max(fullness, wetness),
                        "Time Worn: " + ChatColor.YELLOW + Cleantime(timeworn)
                );
        meta.setLore(lore);
        item.setItemMeta(meta);
        // stinkydiaper = item;
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
                    "Useage: " + ChatColor.GREEN + Math.max(fullness, wetness),
                    "Time Worn: " + ChatColor.YELLOW + Cleantime(timeworn)
                );
        meta.setLore(lore);
        item.setItemMeta(meta);
        // wetdiaper = item;
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
                    "Useage: " + ChatColor.GREEN + Math.max(fullness, wetness),
                    "Time Worn: " + ChatColor.YELLOW + Cleantime(timeworn)
                );
        meta.setLore(lore);
        item.setItemMeta(meta);
        // wetpullup = item;
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
                    "Useage: " + ChatColor.GREEN + Math.max(fullness, wetness),
                    "Time Worn: " + ChatColor.YELLOW + Cleantime(timeworn)
                );
        meta.setLore(lore);
        item.setItemMeta(meta);
        // wetthickdiaper = item;
        return item;
    }
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

    // public void createWasherRecipe() {
    //     // Define the custom item
    //     ItemStack item = new ItemStack(Material.FURNACE);
    //     ItemMeta meta = item.getItemMeta();
    //     meta.setDisplayName(ChatColor.WHITE + "Washing Machine");
    //     meta.setCustomModelData(626014);// Custom Model Data for texture
    //     item.setItemMeta(meta);
    //     Washer = item;

    //      // Create the recipe
    //     ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(plugin, "Washer"), Washer);
    //     recipe.addIngredient(Material.FURNACE);
    //     recipe.addIngredient(Material.WATER_BUCKET);

    //     // Register the recipe
    //     Bukkit.addRecipe(recipe);
    // }

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
        recipe.setIngredient('S', Material.SLIME_BALL); 
        recipe.setIngredient('P', Material.PAPER);

        // Register the recipe
        Bukkit.addRecipe(recipe);
    }
    public void createDiaperRecipe() {
        // Define the custom item
        ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Diaper");
        meta.setCustomModelData(626009);
        item.setItemMeta(meta);
        diaper = item;
        
        // Create the recipe
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "Diaper"), diaper);
        recipe.shape("TST", " S ", "PSP"); 
        recipe.setIngredient('S', new RecipeChoice.ExactChoice(ItemManager.DiaperStuffer));
        // recipe.setIngredient('S', new RecipeChoice.MaterialChoice(DiaperStuffer.getType()));
        recipe.setIngredient('P', Material.PAPER);
        recipe.setIngredient('T', new RecipeChoice.ExactChoice(ItemManager.Tape));

        // Register the recipe
        Bukkit.addRecipe(recipe);
    }
    public void createThickDiaperRecipe() {
        // Define the custom item
        ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Thick Diaper");
        meta.setCustomModelData(626001);
        item.setItemMeta(meta);
        thickdiaper = item;

        // Create the recipe
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "ThickDiaper"), thickdiaper);
        recipe.shape("TST", "SSS", "PSP"); 
        recipe.setIngredient('S', new RecipeChoice.ExactChoice(ItemManager.DiaperStuffer));
        recipe.setIngredient('P', Material.PAPER);
        recipe.setIngredient('T', new RecipeChoice.ExactChoice(ItemManager.Tape));

        // Register the recipe
        Bukkit.addRecipe(recipe);
    }
    public void createPullupRecipe() {
        // Define the custom item
        ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Pullup");
        meta.setCustomModelData(626003);
        item.setItemMeta(meta);
        pullup = item;
        
        // Define the custom item
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "Pullup"), pullup);
        recipe.shape("W W", " S ", "PSP"); 
        recipe.setIngredient('S', new RecipeChoice.ExactChoice(ItemManager.DiaperStuffer));
        recipe.setIngredient('W', Material.WHITE_WOOL);
        recipe.setIngredient('P', Material.PAPER);

        // Register the recipe
        Bukkit.addRecipe(recipe);
    }
    public void createUnderwearRecipe() {
        // Define the custom item
        ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Underwear");
        meta.setCustomModelData(626002);
        item.setItemMeta(meta);
        underwear = item;

        // Create the recipe
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "Underwear"), underwear);
        recipe.shape("   ", "WWW", " W ");
        recipe.setIngredient('W', Material.WHITE_WOOL);

        // Register the recipe
        Bukkit.addRecipe(recipe);
    }
    // public void createLaxRecipe() {
    //     // Define the custom item
    //     ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
    //     ItemMeta meta = item.getItemMeta();
    //     meta.setDisplayName("Laxative");
    //     List<String> lore = Arrays.asList(
    //             "Use to spike food items in the crafting table.",
    //             ChatColor.YELLOW + "NOTE:",
    //             "You can only use 1 food item and 1 laxative at a time.",
    //             "Increases the rate the bowels fills."
    //         );
    //     meta.setLore(lore);
    //     meta.setCustomModelData(626012);
    //     item.setItemMeta(meta);
    //     lax = item;

    //     // Create the recipe
    //     ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(plugin, "Laxative"), lax);
    //     recipe.addIngredient(Material.ROTTEN_FLESH);

    //     // Register the recipe
    //     Bukkit.addRecipe(recipe);
    // }
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
