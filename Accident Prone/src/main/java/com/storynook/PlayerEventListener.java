package com.storynook;

import com.storynook.items.CustomItems;
import com.storynook.items.pants;
import com.storynook.menus.IncontinenceMenu;
import com.storynook.menus.SettingsMenu;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;


public class PlayerEventListener implements Listener {
    private static Plugin plugin;
    HashMap<UUID, HashSet<NamespacedKey>> playerCraftedSpecialItems = new HashMap<>();
        
    public PlayerEventListener(Plugin plugin) {
        this.plugin = plugin;
    }
                //Loads the player stats when the login, and discovers all of the custom crafting recipes
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.loadPlayerStats(event.getPlayer()); //Uses the plugin instance to load player stats
        //Discover all of the custom crafting recipes

        String[] recipes = {
            "Diaper",
            "ThickDiaper", 
            "Pullup",
            "Underwear",
            "Tape",
            "CleanPants",
            "Toilet",
            "DiaperStuffer",
            "Washer",
            "LaxedItem",
            "Crib",
            "DiaperPail"
        };
        
        for (String recipe : recipes) {
            event.getPlayer().discoverRecipe(new NamespacedKey(plugin, recipe));
        }
        // if (event.getPlayer().hasPermission("diaperplugin.debug") || event.getPlayer().isOp()) {
        //     event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "Laxative"));
        // }
        // event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "DiaperPail"));
        // event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "Laxative"));
        // event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "Diuretic"));


        
        // plugin.manageParticleEffects(event.getPlayer());
    }
            
    //Updates stats and world events when the player leaves
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.getVehicle() instanceof ArmorStand) {
            player.leaveVehicle();
            ArmorStand armorStand = (ArmorStand) player.getVehicle();
            armorStand.remove();
        }
        plugin.savePlayerStats(event.getPlayer()); // Uses the plugin instance to save player stats
    }
        
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        // Determine if the player is sprinting or walking normally
        if (player.isSprinting()) {
            UpdateStats.activityMultiplier.put(playerUUID, 1.5); // Increase for sprinting
        } else {
            UpdateStats.activityMultiplier.put(playerUUID, 1.0); // Normal walking or standing still
        }

        // Check for jumping specifically
        if (player.isOnGround() && player.getLocation().getY() > event.getFrom().getY()) {
            // The player location is ascending from the last event call and they were on the ground
            Double currentMultiplier = UpdateStats.activityMultiplier.getOrDefault(playerUUID, 1.0);
            UpdateStats.activityMultiplier.put(playerUUID, currentMultiplier + 0.5);
        }
    }
        
            
    //Handles the Player's consume event so they stay hydrated
    @EventHandler
    public void onPlayerDrink(PlayerItemConsumeEvent event) {
        PlayerStats stats = plugin.getPlayerStats(event.getPlayer().getUniqueId());
        if (stats != null) {
            ItemStack consumedItem = event.getItem();
            if(isHydrating(consumedItem)){
                // Increase hydration when the player drinks
                UpdateStats.HydrationSpike.put(event.getPlayer().getUniqueId(), 10);
                stats.increaseHydration(30); 
            }
        }
    }
    private boolean isHydrating(ItemStack item){
        if (item.getType() == Material.POTION || item.getType() == Material.MELON_SLICE) {
            return true;
        }
        return false;
    }
            

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        ItemStack[] matrix = inventory.getMatrix();

        if (event.getRecipe() == null || event.getInventory() == null) return;

            ItemStack result = event.getRecipe().getResult();
            if (result.getType() != Material.LEATHER_LEGGINGS) return;

            Material woolColor = null;

            // Check wool colors in the grid
            for (ItemStack item : matrix) {
                if (item != null && item.getType() == Material.LEATHER) {
                    return;
                }
                else if (item != null && item.getType().toString().endsWith("_WOOL")) {
                    if (woolColor == null) {
                        woolColor = item.getType(); // Set initial color
                    } else if (!woolColor.equals(item.getType())) {
                        event.getInventory().setResult(new ItemStack(Material.AIR)); // Mismatched colors cancel craft
                        return;
                    }
                }
            }

            if (woolColor != null) {

                Color color = pants.getColorFromWool(woolColor);
                if (color == null) {
                    // Cancel crafting if color mapping fails
                    event.getInventory().setResult(new ItemStack(Material.AIR));
                    return;
                }
                event.getInventory().setResult(pants.createPants(woolColor));
            }
            else {
                event.getInventory().setResult(new ItemStack(Material.AIR)); // Ensure no item is crafted if no wool is found
            }

        for (ItemStack item : matrix) {
            if (item == null) continue;

            if (CustomItems.isCustomItem(item)) {
                inventory.setResult(null); // Only block crafting if it's not intended
            }
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

    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (plugin.isAwaitingInput(playerUUID)) {
            event.setCancelled(true); // Prevents the chat from showing to other players
            String inputType = plugin.getAwaitingInputType(playerUUID);
            String message = event.getMessage().trim();
            try {
                if ("minFill".equals(inputType)) {
                    int newMinFill = Integer.parseInt(message);
                    PlayerStats stats = plugin.getPlayerStats(playerUUID);
                    stats.setMinFill(newMinFill);
                    player.sendMessage(ChatColor.GREEN + "Minimum fill set to: " + newMinFill);
                    plugin.clearAwaitingInput(playerUUID);
                    Bukkit.getScheduler().runTask(plugin, () -> SettingsMenu.OpenSettings(player, plugin));
                }
                else if ("bladderincon".equals(inputType)){
                    int newBladderincon = Integer.parseInt(message);
                    PlayerStats stats = plugin.getPlayerStats(playerUUID);
                    stats.setBladderIncontinence(newBladderincon);
                    player.sendMessage(ChatColor.GREEN + "Bladder Incontinence set to: " + newBladderincon);
                    plugin.clearAwaitingInput(playerUUID);
                    Bukkit.getScheduler().runTask(plugin, () -> IncontinenceMenu.IncontinenceSettings(player, plugin));
                }else if ("bowelincon".equals(inputType)){
                    int newBowelincon = Integer.parseInt(message);
                    PlayerStats stats = plugin.getPlayerStats(playerUUID);
                    stats.setBowelIncontinence(newBowelincon);
                    player.sendMessage(ChatColor.GREEN + "Bowel Incontinence set to: " + newBowelincon);
                    plugin.clearAwaitingInput(playerUUID);
                    Bukkit.getScheduler().runTask(plugin, () ->  IncontinenceMenu.IncontinenceSettings(player, plugin));
                }
            } catch (NumberFormatException e) {
                plugin.clearAwaitingInput(playerUUID);
                player.sendMessage(ChatColor.RED + "Invalid number. Please enter a valid number.");
                if ("minFill".equals(inputType)) {
                    Bukkit.getScheduler().runTask(plugin, () -> SettingsMenu.OpenSettings(player, plugin));
                } else {
                    Bukkit.getScheduler().runTask(plugin, () -> IncontinenceMenu.IncontinenceSettings(player, plugin));
                }
            } finally {
                // Ensure the input state is always cleared
                plugin.clearAwaitingInput(playerUUID);
            }
        }
    }
}

