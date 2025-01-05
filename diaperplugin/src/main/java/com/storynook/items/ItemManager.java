package com.storynook.items;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class ItemManager {
    private final JavaPlugin plugin;
    public static ItemStack diaperpail;
    public static ItemStack diaper;
    public static ItemStack stinkydiaper;
    public static ItemStack wetdiaper;
    public static ItemStack wetpullup;
    public static ItemStack wetthickdiaper;
    public static ItemStack underwear;
    public static ItemStack thickdiaper;
    public static ItemStack pullup;
    // public static ItemStack lax;
    // public static ItemStack dur;
    public static ItemStack toilet;
    public static ItemStack DiaperStuffer;
    public static ItemStack Tape;
    // public static ItemStack Washer;

    public static void init(){
        //Create the non-craftable Items.
        createWetDiaper();
        createStinkyDiaper();
        createWetPullup();
        createWetThickDiaper();
    }
    public ItemManager(JavaPlugin plugin){this.plugin = plugin;}

    public static void createStinkyDiaper(){
        // Define the custom item
        ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Stinky Diaper");
        meta.setCustomModelData(626004);
        item.setItemMeta(meta);
        stinkydiaper = item;
    }
    public static void createWetDiaper(){
        // Define the custom item
        ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Wet Diaper");
        meta.setCustomModelData(626005);
        item.setItemMeta(meta);
        wetdiaper = item;
    }
    public static void createWetPullup(){
        // Define the custom item
        ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Wet Pullup");
        meta.setCustomModelData(626010);
        item.setItemMeta(meta);
        wetpullup = item;
    }
    public static void createWetThickDiaper(){
        // Define the custom item
        ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Wet Thick Diaper");
        meta.setCustomModelData(626011);
        item.setItemMeta(meta);
        wetthickdiaper = item;
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
    
}
