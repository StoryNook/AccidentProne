package com.storynook.items;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class cribs {
    private JavaPlugin plugin;
    public static ItemStack Crib;
    public cribs(JavaPlugin plugin){this.plugin = plugin;}
    public static void init(JavaPlugin plugin){}

    public static ItemStack createCrib(Material woodtype) {
        ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("Crib");
        meta.setCustomModelData(getCribModelNumber(woodtype));
        item.setItemMeta(meta);
        Crib = item;
        
        return item;
    }

    public static int getCribModelNumber (Material woodtype)
    {
        switch (woodtype) {
            case DARK_OAK_SLAB:
                return 627000;
            default:
                return 0;
        }
    }

    public void createCribRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "Crib"), createCrib(Material.DARK_OAK_SLAB));
        recipe.shape("WWW", "SSS", "WWW");
        List<Material> woodMaterials = Arrays.asList(
            Material.BIRCH_SLAB, Material.SPRUCE_SLAB, Material.OAK_SLAB, Material.ACACIA_SLAB, Material.JUNGLE_SLAB, 
            Material.QUARTZ_SLAB, Material.WARPED_SLAB, Material.DARK_OAK_SLAB, Material.MANGROVE_SLAB, Material.PETRIFIED_OAK_SLAB
        );

        recipe.setIngredient('W', new RecipeChoice.MaterialChoice(woodMaterials));
        recipe.setIngredient('S', Material.STICK);
        Bukkit.addRecipe(recipe);
    }
}
