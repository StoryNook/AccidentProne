package com.storynook.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.storynook.PlayerStats;
import com.storynook.Plugin;


public class SoundEffectsMenu implements Listener{
    private Plugin plugin;
    public SoundEffectsMenu (Plugin plugin) {
        this.plugin = plugin;
    }
    public static void SoundEffects(Player player, Plugin plugin) {
        UUID playerUUID = player.getUniqueId();
        PlayerStats stats = plugin.getPlayerStats(playerUUID);
        stats.printAllStoredSounds();
        Inventory menu = Bukkit.createInventory(player, 9 * 6, "Sound Effects");

        ItemStack Back = new ItemStack(Material.RED_STAINED_GLASS_PANE); // Custom button
        ItemMeta BackMeta = Back.getItemMeta();
        if (BackMeta != null) {
            BackMeta.setDisplayName("Back to Settings");
            Back.setItemMeta(BackMeta);   
        }
        menu.setItem(0, Back);

        int currentSlot = 9; // Start at first slot
        for (Map.Entry<String, Map<String, Boolean>> categoryEntry : stats.StoredSounds.entrySet()) {
            String categoryName = categoryEntry.getKey();
            Map<String, Boolean> sounds = categoryEntry.getValue();
            System.out.println("\nProcessing category: " + categoryName);
            // Create category item
            ItemStack categoryItem = new ItemStack(Material.SLIME_BALL);
            ItemMeta categoryMeta = categoryItem.getItemMeta();
            if (categoryMeta != null) {
                categoryMeta.setDisplayName("Category: "+ categoryName);
                categoryItem.setItemMeta(categoryMeta);
            }
            menu.setItem(currentSlot, categoryItem);

            // Add sounds for this category
            int soundIndex = 0;
            for (Map.Entry<String, Boolean> soundEntry : sounds.entrySet()) {
                String soundName = soundEntry.getKey();
                boolean isEnabled = soundEntry.getValue();
                System.out.println("Processing sound: " + soundName + 
                              " | Enabled: " + isEnabled + 
                              " | Category: " + categoryName);

                ItemStack soundItem = new ItemStack(Material.SLIME_BALL);
                ItemMeta soundMeta = soundItem.getItemMeta();
                if (soundMeta != null) {
                    soundMeta.setDisplayName(soundName);
                    List<String> lore = new ArrayList<>();
                    lore.add("Enabled: " + isEnabled);
                    soundMeta.setLore(lore);
                    soundItem.setItemMeta(soundMeta);
                }
                
                int soundSlot = currentSlot + 1 + soundIndex;
                System.out.println("Attempting to place sound in slot: " + soundSlot +
                              " | Sound Name: " + soundName);
                if (soundSlot < currentSlot + 8) { // Ensure we don't exceed inventory size
                    menu.setItem(soundSlot, soundItem);
                    System.out.println("Sound successfully placed in slot: " + soundSlot);
                }else {
                    System.out.println("Skipping sound placement - slot exceeds inventory size");
                }
                soundIndex++;
            }

            currentSlot += 9; // Move to next category section
        }
        player.openInventory(menu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Sound Effects")) {
            return;
        }

        event.setCancelled(true); // Prevent default behavior

        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        UUID playerUUID = player.getUniqueId();
        PlayerStats stats = plugin.getPlayerStats(playerUUID);

        if (stats == null) {
            return;
        }
        else if (event.getView().getTitle().equals("Sound Effects")) {
            if (event.getCurrentItem().getType() == Material.RED_STAINED_GLASS_PANE) {
                SettingsMenu.OpenSettings(player, plugin);
            }
        }
    }
}
