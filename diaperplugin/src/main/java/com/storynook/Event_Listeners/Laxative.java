package com.storynook.Event_Listeners;


import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.storynook.PlayerStats;
import com.storynook.Plugin;

public class Laxative implements Listener{
    private final Plugin plugin;
    public Laxative(Plugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.getPersistentDataContainer().has(
                new NamespacedKey(plugin, "laxative_effect"),
                PersistentDataType.BYTE)) {
                // Apply laxative effect
                PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());
                if(stats.getMessing()){
                    if (stats.getEffectDuration() == 0) {
                        stats.setBowelFillRate(stats.getBowelFillRate() * ((Math.random() * 7) + 3));
                    }
                    stats.increaseEffectDuration(30);
                }
            }
        }
    }

    private boolean isLaxative(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.hasCustomModelData() && meta.getCustomModelData() == 626012;
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        ItemStack[] matrix = inventory.getMatrix();

        if (event.getRecipe() == null || event.getInventory() == null) return;
        
        ItemStack result = event.getRecipe().getResult();
        ItemMeta resultmeta = result.getItemMeta();
        if (resultmeta.getPersistentDataContainer().has(
            new NamespacedKey(plugin, "laxative_effect"),
            PersistentDataType.BYTE)) {
    
            int laxativeCount = 0;
            Material foodType = null;
            int foodCount = 0;

            for (ItemStack item : matrix) {
                if (item == null) continue;

                if (isLaxative(item)) {
                    laxativeCount += item.getAmount();
                } else if (item.getType().isEdible()) {
                    if (foodType == null) {
                        foodType = item.getType();
                    } else if (!item.getType().equals(foodType)) {
                        // Multiple different food items - fail crafting
                        inventory.setResult(null);
                        return;
                    }
                    foodCount += item.getAmount();;
                } else{
                    inventory.setResult(null);
                    return;
                }
            }
            if (foodType != null && laxativeCount >= 1 && foodCount >= 1) {

                result = new ItemStack(foodType);
                ItemMeta meta = result.getItemMeta();
                if (meta != null) {
                    NamespacedKey craftedByKey = new NamespacedKey(plugin, "crafted_by");
                    String crafterName = ((Player) inventory.getViewers().get(0)).getName();
                    Bukkit.getLogger().info("Setting crafted_by to: " + crafterName);
                    meta.getPersistentDataContainer().set(
                        craftedByKey,
                        PersistentDataType.STRING,
                        crafterName
                    );
                    meta.getPersistentDataContainer().set(
                        new NamespacedKey(plugin, "laxative_effect"),
                        PersistentDataType.BYTE,
                        (byte) 1
                    );
                }
                meta.setLore(Collections.singletonList(ChatColor.RED + "Has Laxative"));
                result.setItemMeta(meta);
                inventory.setResult(result);
            } else {
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            Inventory inventory = event.getInventory();

            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null) {
                    ItemMeta meta = item.getItemMeta();
                    NamespacedKey craftedByKey = new NamespacedKey(plugin, "crafted_by");

                    if (meta.getPersistentDataContainer().has(craftedByKey, PersistentDataType.STRING)) {
                        updateLoreForPlayer(item, player);
                        inventory.setItem(i, item); // Update the item in the inventory
                    }
                }
            }
        }
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            Inventory inventory = event.getInventory();

            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null) {
                    ItemMeta meta = item.getItemMeta();
                    NamespacedKey craftedByKey = new NamespacedKey(plugin, "crafted_by");

                    if (meta.getPersistentDataContainer().has(craftedByKey, PersistentDataType.STRING)) {
                        updateLoreForPlayer(item, player);
                        inventory.setItem(i, item); // Update the item in the inventory
                    }
                }
            }
        }
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent  event) {
        if (!(event.getEntity() instanceof Player)) {
            return; // Ensure that the entity is a player
        }
    
        Player player = (Player) event.getEntity();
        ItemStack item = event.getItem().getItemStack();
        if (event.getItem() != null) {
            ItemMeta meta = item.getItemMeta();
            NamespacedKey craftedByKey = new NamespacedKey(plugin, "crafted_by");

            if (meta.getPersistentDataContainer().has(craftedByKey, PersistentDataType.STRING)) {
                updateLoreForPlayer(item, player);
            }
        }
    }

    // @EventHandler
    // public void onInventoryClick(InventoryClickEvent event) {
    //     if (event.isShiftClick()) {
    //         // Add same logic check to avoid duplications
    //         ItemStack currentItem = event.getCurrentItem();
    //         if (currentItem != null && currentItem.hasItemMeta()) {
    //             ItemMeta meta = currentItem.getItemMeta();
    //             if (meta != null && meta.getPersistentDataContainer().has(
    //                 new NamespacedKey(plugin, "laxative_effect"),
    //                 PersistentDataType.BYTE
    //             )) {
    //                 // log or debug; manage how items are handled further.
    //             }
    //         }
    //     }
    //     if (!(event.getWhoClicked() instanceof Player)) return;

    //     Player player = (Player) event.getWhoClicked();
    //     ItemStack item = event.getCurrentItem();

    //     if (item != null && item.hasItemMeta()) {
    //         ItemMeta meta = item.getItemMeta();
    //         if (meta != null && meta.getPersistentDataContainer().has(
    //             new NamespacedKey(plugin, "laxative_effect"),
    //             PersistentDataType.BYTE
    //         )) {
    //             // Add custom lore visible only to this player
    //             updateLoreForPlayer(item, player);
    //         }
    //     }
    // }

    private void updateLoreForPlayer(ItemStack item, Player player) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        NamespacedKey laxativeKey = new NamespacedKey(plugin, "laxative_effect");
        NamespacedKey craftedByKey = new NamespacedKey(plugin, "crafted_by");

        if (meta.getPersistentDataContainer().has(laxativeKey, PersistentDataType.BYTE) &&
                meta.getPersistentDataContainer().has(craftedByKey, PersistentDataType.STRING)) {

            String crafterName = meta.getPersistentDataContainer()
                    .get(craftedByKey, PersistentDataType.STRING);
            Bukkit.getLogger().info("Crafter name: " + crafterName); // Add this line
            Bukkit.getLogger().info("Current player name: " + player.getName());

            if (crafterName != null && crafterName.equals(player.getName())) {
                meta.setLore(Collections.singletonList(ChatColor.RED + "Has Laxative"));
            } else {
                meta.setLore(null);
            }

            item.setItemMeta(meta);
        }
    }

    // @EventHandler
    // public void onPotionEffect(EntityPotionEffectEvent event) {
    //     if (event.getEntity() instanceof Player) {
    //         Player player = (Player) event.getEntity();
    //         PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());

    //         if (stats != null && event.getNewEffect() != null) {
    //             PotionEffectType effectType = event.getNewEffect().getType();
                
    //             if (effectType.equals(PotionEffectType.SPEED)) {
    //                 int amplifier = event.getNewEffect().getAmplifier();
    //                 stats.setBladderFillRate(1 + (int) (0.2 * (amplifier + 1))); // Increase Bladder and bowel fill rate because of speed potion
    //                 stats.setBowelFillRate(1 + (int) (0.2 * (amplifier + 1)));
    //             }
    //         }
    //     }
    // }
}
