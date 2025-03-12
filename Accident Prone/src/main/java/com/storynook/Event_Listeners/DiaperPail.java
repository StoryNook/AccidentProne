package com.storynook.Event_Listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.storynook.items.underwear;

public class DiaperPail implements Listener {
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        Inventory inventory = event.getInventory();
        if (event.getView().getTitle().equals("Diaper Pail")) {
            boolean isValid = true;
            for (ItemStack item : inventory.getContents()) {
                // Check if the item is null or air (invalid)
                if (item == null || item.getType() == Material.AIR) {
                    isValid = false;
                    break;
                }
                
                // Check if the item is a valid custom item using your criteria
                if (!isUsed(item)) {
                    isValid = false;
                    break;
                }
                
                // Ensure the stack size is correct (e.g., up to 64)
                if (item.getAmount() < 64) {
                    isValid = false;
                    break;
                }
            }
            if (isValid){
                for (int i = 0; i < inventory.getSize(); i++) {
                    inventory.setItem(i, null);
                }
                // Add 9 new diaper items
                for (int i = 0; i < 9; i++) {
                    ItemStack diaper = underwear.ThickDiaper();
                    if (diaper != null) {
                        inventory.setItem(i, diaper);
                    }
                }
            }
        }
    }

    private boolean isUsed (ItemStack item){
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasCustomModelData()) {
                return meta.getCustomModelData() == 626005 || 
                meta.getCustomModelData() == 626004 ||
                meta.getCustomModelData() == 626011 ||  
                meta.getCustomModelData() == 626011;
            }
        }
        return false;
    }
}
