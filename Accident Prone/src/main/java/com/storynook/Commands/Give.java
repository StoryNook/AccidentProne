package com.storynook.Commands;
import com.storynook.items.underwear;
import com.storynook.items.ItemManager;
import com.storynook.items.cribs;
import com.storynook.items.pants;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class Give {
    public static void GiveItem(Player player, String item, String variation, int quantity) {
        ArrayList<ItemStack> itemsToAdd = new ArrayList<>();
        switch (item.toLowerCase()) {
            case "underwear":
                for (int i = 0; i < quantity; i++) {
                    itemsToAdd.add(underwear.Underwear());
                }
                break;
            case "pullup":
                for (int i = 0; i < quantity; i++) {
                    itemsToAdd.add(underwear.Pullup());
                }
                break;
            case "diaper":
                for (int i = 0; i < quantity; i++) {
                    itemsToAdd.add(underwear.Diaper());
                }
                break;
            case "thick_diaper":
                for (int i = 0; i < quantity; i++) {
                    itemsToAdd.add(underwear.ThickDiaper());
                }
                break;
            case "laxative":
                for (int i = 0; i < quantity; i++) {
                    itemsToAdd.add(ItemManager.Laxative());
                }
                break;
            case "diaper_pail":
                for (int i = 0; i < quantity; i++) {
                    itemsToAdd.add(ItemManager.Diaperpail());
                }
                break;
            case "washer":
                for (int i = 0; i < quantity; i++) {
                    itemsToAdd.add(ItemManager.Washer());
                }
                break;
            case "toilet":
                for (int i = 0; i < quantity; i++) {
                    itemsToAdd.add(ItemManager.Toilet());
                }
                break;
            case "crib":
                Material cribmaterial = Material.getMaterial(variation + "_SLAB");
                for (int i = 0; i < quantity; i++) {
                    itemsToAdd.add(cribs.createCrib(cribmaterial));
                }
                break;
            case "pants":
                Material pantswool = Material.getMaterial(variation + "_WOOL");
                for (int i = 0; i < quantity; i++) {
                    itemsToAdd.add(pants.createPants(pantswool));
                }
                break;
            default:
                // Add other items as needed
                return;
        }
        Inventory inventory = player.getInventory();
        int availableSpace = 0;

        for (ItemStack stack : inventory) {
            if (stack == null || stack.getType() == Material.AIR) {
                availableSpace++;
            }
        }

        // Calculate how many items can be added to the inventory
        int totalItemsToAdd = quantity;
        int itemsToDrop = Math.max(0, quantity - (availableSpace * 64));

        // Add as many as possible to the inventory
        for (ItemStack stack : itemsToAdd) {
            if (inventory.firstEmpty() != -1 && quantity > 0) {
                int addQuantity = Math.min(64, quantity);
                ItemStack newStack = stack.clone();
                newStack.setAmount(addQuantity);
                inventory.addItem(newStack);
                quantity -= addQuantity;
            } else {
                // Drop remaining items
                for (int i = 0; i < quantity; i++) {
                    Location dropLocation = player.getLocation().add(0, 1, 0);
                    Player targetPlayer = player;
                    targetPlayer.getWorld().dropItem(dropLocation, stack.clone());
                }
                break;
            }
        }
    }
}