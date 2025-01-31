package com.storynook.Event_Listeners;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.storynook.items.pants;

import org.bukkit.Color;

public class PantsCrafting implements Listener{
    private JavaPlugin plugin;
    public PantsCrafting(JavaPlugin plugin){this.plugin = plugin;}
    
    public class CraftingListener implements Listener {

        private boolean isCustomPants(ItemStack item) {
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
            if (isCustomPants(item)) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onPrepareAnvil(PrepareAnvilEvent event) {
            ItemStack firstItem = event.getInventory().getItem(0);
            ItemStack secondItem = event.getInventory().getItem(1);

            if ((firstItem != null && isCustomPants(firstItem)) || 
                (secondItem != null && isCustomPants(secondItem))) {
                event.setResult(null); // Block the result output
            }
        }





    //     @EventHandler
    //     public void EquipLeggings(PlayerInteractEvent event) {
    //         Player player = event.getPlayer();
    //         PlayerInventory inventory = player.getInventory();
    //         ItemStack itemInHand = player.getInventory().getItemInMainHand();

    //         if (itemInHand != null) {
    //             if (isPants(itemInHand)) { 

    //                 LeatherArmorMeta meta = (LeatherArmorMeta) player.getInventory().getLeggings().getItemMeta();
    //                 if(meta != null && meta.hasCustomModelData()){
    //                     inventory.setLeggings(null);
    //                     AttributeModifier modifier = new AttributeModifier(event.getPlayer().getUniqueId(), "generic.armor", 0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.LEGS);
    //                     meta.addAttributeModifier(Attribute.GENERIC_ARMOR, modifier);
    //                     itemInHand.setItemMeta(meta);
    //                     inventory.setLeggings(itemInHand);
    //                     inventory.removeItem(itemInHand);

    //                 }
    //             }
    //         }
    //     }

    //     @EventHandler
    //     public void onLeggingsClick(InventoryClickEvent event) {
    //         if (!(event.getWhoClicked() instanceof Player)) return;

    //         Player player = (Player) event.getWhoClicked();
    //         InventoryType.SlotType slotType = event.getSlotType();
    //         ItemStack currentItem = event.getCurrentItem();  // Item in the slot
    //         ItemStack cursorItem = event.getCursor();        // Item on the cursor

    //         // 1. Handle normal clicks on the leggings slot
    //         if (slotType == InventoryType.SlotType.ARMOR && event.getSlot() == 36) {
    //             // If the player is wearing the diaper leggings
    //             if (isDiaper(currentItem)) {
    //                 // If they're trying to equip ANY other leggings
    //                 if (cursorItem != null && isPants(cursorItem) && !isDiaper(cursorItem)) {
    //                     player.getInventory().setLeggings(null);  // Remove the diaper
    //                 }
    //             }
    //         }

    //         // 2. Handle SHIFT-CLICK equipping leggings
    //         if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
    //             ItemStack shiftItem = event.getCurrentItem();

    //             if (shiftItem != null && isPants(shiftItem) && !isDiaper(shiftItem)) {
    //                 ItemStack equippedLeggings = player.getInventory().getLeggings();

    //                 // If the player is wearing diaper leggings, remove them
    //                 if (isDiaper(equippedLeggings)) {
    //                     player.getInventory().setLeggings(null);
    //                 }
    //             }
    //         }

    //         // 3. Handle number key hotbar swapping
    //         if (event.getClick() == ClickType.NUMBER_KEY) {
    //             int hotbarButton = event.getHotbarButton();
    //             ItemStack hotbarItem = player.getInventory().getItem(hotbarButton);

    //             if (hotbarItem != null && isPants(hotbarItem) && !isDiaper(hotbarItem)) {
    //                 ItemStack equippedLeggings = player.getInventory().getLeggings();

    //                 if (isDiaper(equippedLeggings)) {
    //                     player.getInventory().setLeggings(null);
    //                 }
    //             }
    //         }
    //     }

    //     private boolean isDiaper(ItemStack item) {
    //         if (item == null || item.getType() != Material.LEATHER_LEGGINGS) {
    //             return false;
    //         }
    //         if (!item.hasItemMeta()) {
    //             return false;
    //         }
    //         ItemMeta meta = item.getItemMeta();
    //         if(meta.hasCustomModelData() && (
    //                meta.getCustomModelData() == 626002 ||  // Underwear
    //                meta.getCustomModelData() == 626003 ||  // Pullup
    //                meta.getCustomModelData() == 626009 ||  // Diaper
    //                meta.getCustomModelData() == 626001     // Thick Diaper
    //         )) return true;
    //         return false;
    //     }

    //     private boolean isPants(ItemStack item) {
    //         if (item == null) return false;
    //         ItemMeta meta = item.getItemMeta();
    //         if (!meta.hasCustomModelData()) return false;
    //         return meta.getCustomModelData() == 626015 ||
    //             meta.getCustomModelData() == 626016;
    //     }
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
}
