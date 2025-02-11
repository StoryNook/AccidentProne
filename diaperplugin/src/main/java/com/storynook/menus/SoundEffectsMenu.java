package com.storynook.menus;

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
        // UUID playerUUID = player.getUniqueId();
        // PlayerStats stats = plugin.getPlayerStats(playerUUID);
        Inventory menu = Bukkit.createInventory(player, 9, "Sound Effects");

        ItemStack Back = new ItemStack(Material.RED_STAINED_GLASS_PANE); // Custom button
        ItemMeta BackMeta = Back.getItemMeta();
        if (BackMeta != null) {
            BackMeta.setDisplayName("Back to Settings");
            Back.setItemMeta(BackMeta);   
        }
        menu.setItem(0, Back);
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
