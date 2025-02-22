package com.storynook.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
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
    private static Map<UUID, Integer> currentPage = new HashMap<>();
    public static void SoundEffects(Player player, Plugin plugin, int page) {
        UUID playerUUID = player.getUniqueId();
        PlayerStats stats = plugin.getPlayerStats(playerUUID);
        Inventory menu = Bukkit.createInventory(player, 9 * 6, "Sound Effects");

        int start = page * 4;
        int end = page + 4;

        ItemStack Home = new ItemStack(Material.BARRIER); // Custom button
        ItemMeta HomeMeta = Home.getItemMeta();
        if (HomeMeta != null) {
            HomeMeta.setDisplayName("Back to Settings");
            Home.setItemMeta(HomeMeta);   
        }
        menu.setItem(0, Home);

        ItemStack Back = new ItemStack(Material.RED_STAINED_GLASS_PANE); // Custom button
        ItemMeta BackMeta = Back.getItemMeta();
        if (BackMeta != null) {
            if (page > 0) {
                BackMeta.setDisplayName("Previous Page");
            }
            else{
                BackMeta.setDisplayName("Back to Settings");
            }
            Back.setItemMeta(BackMeta);   
            menu.setItem(45, Back);
        }

        int currentSlot = 9; // Starting slot for categories
        List<Map.Entry<String, Map<String, Boolean>>> categoryList = new ArrayList<>(stats.getStoredSounds().entrySet());

        for (int i = start; i < Math.min(end, categoryList.size()); i++) {
            Map.Entry<String, Map<String, Boolean>> categoryEntry = categoryList.get(i);
            String categoryName = categoryEntry.getKey();
            Map<String, Boolean> sounds = categoryEntry.getValue();

            // Create and place category item
            ItemStack categoryItem = new ItemStack(Material.SLIME_BALL);
            ItemMeta categoryMeta = categoryItem.getItemMeta();
            if (categoryMeta != null) {
                categoryMeta.setDisplayName("Category: " + categoryName);
                categoryItem.setItemMeta(categoryMeta);
            }
            menu.setItem(currentSlot, categoryItem);

            // Place sounds under the category
            int soundIndex = 0;
            for (Map.Entry<String, Boolean> soundEntry : sounds.entrySet()) {
                String soundName = soundEntry.getKey();
                boolean isEnabled = soundEntry.getValue();

                ItemStack soundItem = new ItemStack(Material.SLIME_BALL);
                ItemMeta soundMeta = soundItem.getItemMeta();
                if (soundMeta != null) {
                    soundMeta.setDisplayName(soundName);
                    List<String> lore = new ArrayList<>();
                    lore.add("Enabled: " + (isEnabled ? ChatColor.GREEN + "True" : ChatColor.RED + "False"));
                    lore.add(ChatColor.AQUA + "Left Click to Preview");
                    lore.add(ChatColor.YELLOW + "Right Click to Toggle");
                    lore.add("Category: " + categoryName);
                    soundMeta.setLore(lore);
                    soundItem.setItemMeta(soundMeta);
                }

                int soundSlot = currentSlot + 1 + soundIndex;
                if (soundSlot < 54) { // Ensure within inventory bounds
                    menu.setItem(soundSlot, soundItem);
                }
                soundIndex++;
            }

            currentSlot += 9; // Move to next category section
        }
        if (end < categoryList.size()) {
            ItemStack nextPage = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            ItemMeta nextPageMeta = nextPage.getItemMeta();
            if (nextPageMeta != null) {
                nextPageMeta.setDisplayName("Next Page");
                nextPage.setItemMeta(nextPageMeta);
            }
            menu.setItem(53, nextPage); // Next page item slot
        }
        player.openInventory(menu);
        currentPage.put(playerUUID, page);
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
        int currentPageNumber = currentPage.getOrDefault(playerUUID, 0);
        PlayerStats stats = plugin.getPlayerStats(playerUUID);

        if (stats == null) {
            return;
        }
        else if (event.getView().getTitle().equals("Sound Effects")) {
            if (event.getCurrentItem().getType() == Material.RED_STAINED_GLASS_PANE) {
                if (currentPageNumber > 0) {
                    SoundEffects(player, plugin, currentPageNumber - 1);
                }
                else if (currentPageNumber == 0) {
                    SettingsMenu.OpenSettings(player, plugin);
                }
            }else if (event.getCurrentItem().getType() == Material.GREEN_STAINED_GLASS_PANE) {
                SoundEffects(player, plugin, currentPageNumber + 1);
            } else if (event.getCurrentItem().getType() == Material.BARRIER) {
                SettingsMenu.OpenSettings(player, plugin);
            }
            if (event.getAction() == InventoryAction.PICKUP_HALF && event.getCurrentItem().getType() == Material.SLIME_BALL) {
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                if (meta != null && meta.getDisplayName() != null) {
                    String displayName = meta.getDisplayName();
                    // Skip category headers
                    if (displayName.startsWith("Category: ")) {
                        return;
                    }
                    String soundName = displayName;
                    String categoryName = "";
                    List<String> lore = meta.getLore();
                    if (lore != null) {
                        for (String line : lore) {
                            if (line.startsWith("Category: ")) {
                                String[] parts = line.split(": ");
                                if (parts.length > 1) {
                                    categoryName = parts[1];
                                    break;
                                }
                            }
                        }
                    }
                    if (!categoryName.isEmpty()) {
                        stats.toggleSound(categoryName, soundName);
                        SoundEffects(player, plugin, currentPage.getOrDefault(playerUUID, 0)); // Reload the menu
                    }
                }
            }
            else if (event.getAction() == InventoryAction.PICKUP_ALL && event.getCurrentItem().getType() == Material.SLIME_BALL){
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                if (meta != null && meta.getDisplayName() != null) {
                    String displayName = meta.getDisplayName();
                    // Skip category headers
                    if (displayName.startsWith("Category: ")) {
                        return;
                    }
                    String soundName = displayName;
                    PlaySound(player, soundName);
                }
            }
        }
    }
    private void PlaySound(Player player, String soundName) {
        player.playSound(player.getLocation(), "minecraft:" + soundName, SoundCategory.PLAYERS,1.0f, 1.0f);
    }
}
