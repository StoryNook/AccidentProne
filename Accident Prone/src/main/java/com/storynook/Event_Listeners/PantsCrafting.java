package com.storynook.Event_Listeners;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.storynook.PlayerStats;
import com.storynook.Plugin;
import com.storynook.items.CustomItemCheck;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;

public class PantsCrafting implements Listener{
    private static Plugin plugin;
    public PantsCrafting(Plugin plugin){this.plugin = plugin;}
        
    public static class CraftingListener implements Listener {

        @EventHandler
        public void onPrepareCraft(PrepareItemCraftEvent event) {
            if (event.getRecipe() == null || event.getInventory() == null) return;

            ItemStack result = event.getRecipe().getResult();
            if (result.getType() != Material.LEATHER_LEGGINGS) return;

            ItemStack[] matrix = event.getInventory().getMatrix();
            Material woolColor = null;

            // Check wool colors in the grid
            for (ItemStack item : matrix) {
                if (item != null && item.getType().toString().endsWith("_WOOL")) {
                    if (woolColor == null) {
                        woolColor = item.getType(); // Set initial color
                    } else if (!woolColor.equals(item.getType())) {
                        event.getInventory().setResult(new ItemStack(Material.AIR)); // Mismatched colors cancel craft
                        return;
                    }
                }
            }

            if (woolColor != null) {

                Color color = getColorFromWool(woolColor);
                if (color == null) {
                    // Cancel crafting if color mapping fails
                    event.getInventory().setResult(new ItemStack(Material.AIR));
                    return;
                }
                // Create leggings matching the wool color
                ItemStack coloredLeggings = new ItemStack(Material.LEATHER_LEGGINGS);
                LeatherArmorMeta meta = (LeatherArmorMeta) coloredLeggings.getItemMeta();
                meta.setDisplayName("Pants");
                meta.setCustomModelData(626015);
                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
                meta.setColor(color);
                coloredLeggings.setItemMeta(meta);
                event.getInventory().setResult(coloredLeggings);
            }
            else {
                event.getInventory().setResult(new ItemStack(Material.AIR)); // Ensure no item is crafted if no wool is found
            }
        }
        @EventHandler
        public void onPrepareEnchant(PrepareItemEnchantEvent event) {
            ItemStack item = event.getItem();
            if (CustomItemCheck.isCustomPants(item)) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onPrepareAnvil(PrepareAnvilEvent event) {
            ItemStack firstItem = event.getInventory().getItem(0);
            ItemStack secondItem = event.getInventory().getItem(1);

            if ((firstItem != null && CustomItemCheck.isCustomPants(firstItem)) || 
                (secondItem != null && CustomItemCheck.isCustomPants(secondItem))) {
                event.setResult(null); // Block the result output
            }
        }
    }
    private static Color getColorFromWool(Material wool) {
        switch (wool) {
            case WHITE_WOOL: return Color.WHITE;
            case LIGHT_GRAY_WOOL: return Color.fromRGB(211, 211, 211);
            case GRAY_WOOL: return Color.GRAY; 
            case BLACK_WOOL: return Color.BLACK;
            case RED_WOOL: return Color.RED;
            case ORANGE_WOOL: return Color.ORANGE;
            case YELLOW_WOOL: return Color.YELLOW;
            case LIME_WOOL: return Color.LIME;
            case GREEN_WOOL: return Color.GREEN;
            case LIGHT_BLUE_WOOL : return Color.fromRGB(173, 216, 230);
            case CYAN_WOOL: return Color.fromRGB(0, 255, 255);
            case BLUE_WOOL: return Color.BLUE;
            case PURPLE_WOOL: return Color.PURPLE;
            case MAGENTA_WOOL : return Color.fromRGB(255, 0, 255);
            case PINK_WOOL: return Color.fromRGB(243, 139, 170);
            case BROWN_WOOL: return Color.fromRGB(131, 84, 50);

            // Add more cases as needed for other colors
            default: return null;
        }
    }
    public static void equipDiaperArmor(Player target, boolean changed, boolean accident) {
        PlayerStats stats = plugin.getPlayerStats(target.getUniqueId());
        PlayerInventory inventory = target.getInventory();
        if (inventory.getLeggings() != null) {
            ItemStack leggings = target.getInventory().getLeggings();
            if (CustomItemCheck.isDiaper(leggings) && !stats.getvisableUnderwear()) {
                    target.getInventory().setLeggings(null);
                    return;
            }
            else if (CustomItemCheck.isDiaper(leggings) && (changed || accident)) {
                target.getInventory().setLeggings(null);
            }
        }
        if (stats.getvisableUnderwear() && inventory.getLeggings() == null) {
            ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);

            LeatherArmorMeta meta = (LeatherArmorMeta) leggings.getItemMeta();

            Color color = Color.fromRGB(Integer.parseInt("F7FFF4", 16));
            meta.setColor(color);
            switch (stats.getUnderwearType()) {
                case 0:
                if (stats.getDiaperWetness() > 0 && stats.getDiaperFullness() == 0) {
                    meta.setCustomModelData(626031);
                }
                else if (stats.getDiaperFullness() > 0 && stats.getDiaperWetness() == 0) {
                    meta.setCustomModelData(626032);
                }
                else if (stats.getDiaperFullness() > 0 && stats.getDiaperWetness() > 0){
                    meta.setCustomModelData(626033);
                }
                else{
                    meta.setCustomModelData(626002);
                }
                    meta.setDisplayName("Underwear");
                    break;
                case 1:
                if (stats.getDiaperWetness() > 0 && stats.getDiaperFullness() == 0) {
                    meta.setCustomModelData(626028);
                }
                else if (stats.getDiaperFullness() > 0 && stats.getDiaperWetness() == 0) {
                    meta.setCustomModelData(626029);
                }
                else if (stats.getDiaperFullness() > 0 && stats.getDiaperWetness() > 0){
                    meta.setCustomModelData(626030);
                }
                else{
                    meta.setCustomModelData(626003);
                }
                    meta.setDisplayName("Pullup");
                    break;
                case 2:
                if (stats.getDiaperWetness() > 0 && stats.getDiaperFullness() == 0) {
                    meta.setCustomModelData(626022);
                }
                else if (stats.getDiaperFullness() > 0 && stats.getDiaperWetness() == 0) {
                    meta.setCustomModelData(626023);
                }
                else if (stats.getDiaperFullness() > 0 && stats.getDiaperWetness() > 0){
                    meta.setCustomModelData(626024);
                }
                else{
                    meta.setCustomModelData(626009);
                }
                meta.setDisplayName("Diaper");
                    break;
                case 3:
                if (stats.getDiaperWetness() > 0 && stats.getDiaperFullness() == 0) {
                    meta.setCustomModelData(626025);
                }
                else if (stats.getDiaperFullness() > 0 && stats.getDiaperWetness() == 0) {
                    meta.setCustomModelData(626026);
                }
                else if (stats.getDiaperFullness() > 0 && stats.getDiaperWetness() > 0){
                    meta.setCustomModelData(626027);
                }
                else{
                    meta.setCustomModelData(626001);
                }
                    meta.setDisplayName("Thick Diaper");
                    break;
                default:
                    break;
            }

            meta.setUnbreakable(true);

            meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
            
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS,ItemFlag.HIDE_UNBREAKABLE,ItemFlag.HIDE_ATTRIBUTES);
            AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "generic.armor", 0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.LEGS);
            meta.addAttributeModifier(Attribute.GENERIC_ARMOR, modifier);

            leggings.setItemMeta(meta);
            inventory.setLeggings(leggings);
        }
    }
                
    @EventHandler
    public void EquipLeggings(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        ItemStack leggings = player.getInventory().getLeggings();

        if (itemInHand != null) {
            if (isLeggings(itemInHand)) { 
                LeatherArmorMeta leggingsmeta = (LeatherArmorMeta) leggings.getItemMeta();
                if(leggingsmeta != null && leggingsmeta.hasCustomModelData()){
                    if (CustomItemCheck.isDiaper(leggings)) {
                        inventory.setLeggings(null);
                    }
                }
            }
            else return;
        }
    }
                    
                
    @EventHandler
    public void onLeggingsBreak(PlayerItemBreakEvent event) {
        ItemStack brokenItem = event.getBrokenItem();
        if (isLeggings(brokenItem)) {
            Player player = event.getPlayer();
            Bukkit.getScheduler().runTaskLater(plugin, () -> equipDiaperArmor(player, false, false), 1L);
        }
    }
                
    @EventHandler
    public void onLeggingsClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        InventoryType.SlotType slotType = event.getSlotType();
        ItemStack currentItem = event.getCurrentItem();  // Item in the slot
        ItemStack cursorItem = event.getCursor();        // Item on the cursor

        // 1. Handle normal clicks on the leggings slot
        if (slotType == InventoryType.SlotType.ARMOR && event.getSlot() == 36) {
            // If the player is wearing the diaper leggings
            if (CustomItemCheck.isDiaper(currentItem)) {
                // If they're trying to equip ANY other leggings
                if (cursorItem != null && isLeggings(cursorItem) && !CustomItemCheck.isDiaper(cursorItem)) {
                    player.getInventory().setLeggings(null);  // Remove the diaper
                }
            }
        }

        // 2. Handle SHIFT-CLICK equipping leggings
        if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
            ItemStack shiftItem = event.getCurrentItem();

            if (shiftItem != null && isLeggings(shiftItem) && !CustomItemCheck.isDiaper(shiftItem)) {
                ItemStack equippedLeggings = player.getInventory().getLeggings();

                // If the player is wearing diaper leggings, remove them
                if (CustomItemCheck.isDiaper(equippedLeggings)) {
                    player.getInventory().setLeggings(null);
                }
            }
        }

        // 3. Handle number key hotbar swapping
        if (event.getClick() == ClickType.NUMBER_KEY) {
            int hotbarButton = event.getHotbarButton();
            ItemStack hotbarItem = player.getInventory().getItem(hotbarButton);

            if (hotbarItem != null && isLeggings(hotbarItem) && !CustomItemCheck.isDiaper(hotbarItem)) {
                ItemStack equippedLeggings = player.getInventory().getLeggings();

                if (CustomItemCheck.isDiaper(equippedLeggings)) {
                    player.getInventory().setLeggings(null);
                }
            }
        }
    }
                
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        ItemStack leggings = player.getInventory().getLeggings();

        // Check if the leggings are empty
        if (leggings == null || leggings.getType().isAir()) {
            equipDiaperArmor(player, false, false);
        }
    }

    private boolean isLeggings(ItemStack item) {
        if (item == null) return false;
        return item.getType() == Material.LEATHER_LEGGINGS ||
                item.getType() == Material.CHAINMAIL_LEGGINGS ||
                item.getType() == Material.IRON_LEGGINGS ||
                item.getType() == Material.GOLDEN_LEGGINGS ||
                item.getType() == Material.DIAMOND_LEGGINGS|| 
                isNetheriteLeggings(item.getType());
    }
    private boolean isNetheriteLeggings(Material material){
        try{
            return material == Material.valueOf("NETHERITE_LEGGINGS");
        } catch (IllegalArgumentException e){
            return false;
        }
    }
}
