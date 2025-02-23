package com.storynook.Event_Listeners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.storynook.PlaySounds;
import com.storynook.PlayerStats;
import com.storynook.Plugin;
import com.storynook.items.CustomItems;
import com.storynook.items.underwear;

public class Changing implements Listener{
    private Set<UUID> cooldown = new HashSet<>();
    static HashMap<UUID, Boolean> Justchanged = new HashMap<>();
    HashMap<UUID, Double> distanceinBlocks = new HashMap<>();
    Map<UUID, UUID> playsounds = new HashMap<>();
    
    private final Plugin plugin;
    public Changing(Plugin plugin) {
        this.plugin = plugin;
    }  

    @EventHandler
    public void onPlayerInteractWithEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {

            Player actor = event.getPlayer();
            Player target = (Player) event.getRightClicked();
        
            if (target instanceof Player){
                int rightclicktimes = plugin.rightclickCount.getOrDefault(actor.getUniqueId(), 0);
                PlayerStats targetStats = plugin.getPlayerStats(target.getUniqueId());
                ItemStack item = actor.getInventory().getItemInMainHand();
                if (targetStats != null && targetStats.getOptin()) {
                    if (targetStats != null && targetStats.isCaregiver(actor.getUniqueId(), true)) {
                        if (item != null && item.getType() != Material.AIR) {
                            ItemMeta meta = item.getItemMeta();
                            if (meta != null && meta.hasCustomModelData()) {
                                int customModelData = item.getItemMeta().getCustomModelData();
                                if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) {
                                    return;
                                }
                                rightclicktimes++;
                                if (rightclicktimes > 1) {
                                    // rightclicktimes = 1;
                                    plugin.rightclickCount.put(actor.getUniqueId(), rightclicktimes);
                                    return;
                                }
                                else if (rightclicktimes == 1){
                                    plugin.firstimeran.put(actor.getUniqueId(), true);
                                    plugin.rightclickCount.put(actor.getUniqueId(), rightclicktimes);
                                    playAudio(actor, customModelData, targetStats.getUnderwearType());
                                    handleRightClickHold(actor, target, true, customModelData, targetStats.getUnderwearType());
                                }
                                else {
                                    return;
                                }
                            }
                        }
                        if (item != null && item.getType() == Material.AIR && actor.isSneaking()) {
                            rightclicktimes++;
                            if (rightclicktimes > 1) {
                                // rightclicktimes = 1;
                                plugin.rightclickCount.put(actor.getUniqueId(), rightclicktimes);
                                return;
                            }
                            else if (rightclicktimes == 1){
                                plugin.firstimeran.put(actor.getUniqueId(), true);
                                plugin.rightclickCount.put(actor.getUniqueId(), rightclicktimes);
                                plugin.CheckLittles(actor, targetStats, target);
                            }
                            else {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
            
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player actor = event.getPlayer();
        PlayerStats Stats = plugin.getPlayerStats(actor.getUniqueId());
        ItemStack itemInHand = actor.getInventory().getItemInMainHand();
        if (itemInHand != null && itemInHand.getType() != Material.AIR) {
            ItemMeta meta = itemInHand.getItemMeta();
            if (meta != null && meta.hasCustomModelData()) {
                int rightclicktimes = plugin.rightclickCount.getOrDefault(actor.getUniqueId(), 0);
                int customModelData = meta.getCustomModelData();
                if (customModelData == 626007) {
                    event.setCancelled(true); // Cancel the interaction
                    if (Stats.getOptin() && Stats.getLayers() < 4) {
                        int maxLayers = 0;
                        switch(Stats.getUnderwearType()) {
                            case 0: maxLayers = 1; break;
                            case 1: maxLayers = 2; break;
                            case 2: maxLayers = 3; break;
                            case 3: maxLayers = 4; break;
                            default: return;
                        }
                        if (Stats.getLayers() >= maxLayers) {
                            actor.sendMessage(ChatColor.RED + "You cannot add more layers with your current underwear.");
                            return;
                        }
                        if (Stats.getDiaperWetness() >= 100) {
                            actor.sendMessage(ChatColor.RED + "It's a little too late for that, don't you think?");
                            return;
                        }
                        Stats.setLayers(Stats.getLayers() + 1);
                        actor.sendMessage(ChatColor.GREEN + "Added a layer! Current layers: " + Stats.getLayers());

                        if (itemInHand.getAmount() > 1) {
                            itemInHand.setAmount(itemInHand.getAmount() - 1);
                        } else {
                            actor.getInventory().setItemInMainHand(null);
                        }
                    
                        // Apply cooldown (5 seconds)
                        cooldown.add(actor.getUniqueId());
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> cooldown.remove(actor.getUniqueId()), 20 * 5);
                    }
                    return; // Exit the method to prevent further execution
                }
                else if(CustomItems.VailidUnderwear(itemInHand)){
                    if (event.getAction().name().contains("RIGHT_CLICK")) {
                        if (itemInHand == null || !itemInHand.hasItemMeta() || !itemInHand.getItemMeta().hasCustomModelData()) {
                            plugin.rightclickCount.put(actor.getUniqueId(), 0);
                            plugin.firstimeran.put(actor.getUniqueId(), false);
                            return;
                        } else if (Stats.getHardcore()) {
                            actor.sendMessage("You are in HardCore mode. You should ask a caregiver for help.");
                            return;
                        }
                        else if (!Stats.getOptin()) {
                            return;
                        }
                        else {
                            rightclicktimes++;
                            if (rightclicktimes > 1) {
                                plugin.rightclickCount.put(actor.getUniqueId(), rightclicktimes);
                                return;
                            }
                            else if (rightclicktimes == 1){
                                plugin.firstimeran.put(actor.getUniqueId(), true);
                                plugin.rightclickCount.put(actor.getUniqueId(), rightclicktimes);
                                playAudio(actor, customModelData, Stats.getUnderwearType());
                                handleRightClickHold(actor, null, false, customModelData, Stats.getUnderwearType());
                            }
                            else {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
            
    private void handleRightClickHold(Player actor, Player target, boolean isCaregiverInteraction, int totype, int fromtype) {

        if (plugin.rightclickCount.get(actor.getUniqueId()) > 0 && plugin.firstimeran.get(actor.getUniqueId())) {
            plugin.firstimeran.put(actor.getUniqueId(), false);
            BossBar bossBar = Bukkit.createBossBar(ChatColor.GREEN + "Changing", BarColor.BLUE, BarStyle.SOLID);
            bossBar.addPlayer(actor);
            bossBar.setProgress(0.0); // Start with progress 0 (empty)

            int timeLeft; // Time in seconds

            if (isCaregiverInteraction && target != null) {
                PlayerStats stats = plugin.getPlayerStats(target.getUniqueId());
                timeLeft = stats.getDiaperFullness() > 50 ? 5 : 3;
            }
            else{
                PlayerStats stats = plugin.getPlayerStats(actor.getUniqueId());
                timeLeft = (int) Math.min(10, Math.max(5, stats.getDiaperFullness() * 0.1));
            }

            BukkitRunnable task = new BukkitRunnable() {
                private int ticksLeft = 20 * timeLeft; // Convert seconds to ticks
                @Override
                public void run() {
                    if (isCaregiverInteraction && target != null) {
                        Player caregiver = (Player) actor;
                        Location senderLocation = caregiver.getLocation();
                        Location targetLocation = target.getLocation();
                
                        // Check the distance between the sender and the target player
                        double distance = senderLocation.distance(targetLocation);
                        distanceinBlocks.put(actor.getUniqueId(), distance);
                    }
                    if (ticksLeft <= 0) {
                        bossBar.removePlayer(actor);
                        handleInteraction(actor, target, isCaregiverInteraction);
                        stopAudio(actor, totype, fromtype);
                        plugin.rightclickCount.put(actor.getUniqueId(), 0);
                        this.cancel();
                    } else {
                        ItemStack item = actor.getInventory().getItemInMainHand();
                        if (item == null || !item.hasItemMeta() || !CustomItems.VailidUnderwear(item) || (isCaregiverInteraction && distanceinBlocks.get(actor.getUniqueId()) > 3)) {
                            plugin.rightclickCount.put(actor.getUniqueId(), 0);
                            bossBar.removePlayer(actor);
                            stopAudio(actor,totype, fromtype);
                            this.cancel();
                        }
                        ticksLeft--;
                        double progress = (double) ticksLeft / (20 * timeLeft);
                        bossBar.setProgress(progress);
                    }
                }
            };

            task.runTaskTimer(plugin, 0L, 1L); // Run every tick
        }
    }

    private void playAudio(Player player, int totype, int fromtype) {
        if ((totype == 626002 && fromtype != 0) || fromtype != 0) {
            PlaySounds.playsounds(player, "changing", 5, 1.0, 0.2, true);
            // Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
    
            // // Iterate through each online player to check their distance
            // for (Player targetPlayer : onlinePlayers) {
            //     if (targetPlayer != null) {  // Ensure we have a valid player reference
            //         if (targetPlayer.getLocation().getWorld() == player.getLocation().getWorld()) {
            //             double distance = targetPlayer.getLocation().distance(player.getLocation());
                        
            //             // Check if the player is within the specified radius of 5 blocks
            //             if (distance <= 5.0) {
            //                 // Calculate volume based on distance, with max at 1.0f and min at 0.2f
            //                 float maxVolume = 1.0f;
            //                 float minVolume = 0.2f; // Minimum volume to still hear the sound
            //                 float volume = (float) ((5 - distance) / 5 * (maxVolume - minVolume)) + minVolume;
                            
            //                 targetPlayer.playSound(targetPlayer.getLocation(), 
            //                     "minecraft:diaperchange", // Updated sound reference using Sound enum
            //                     SoundCategory.PLAYERS, 
            //                     volume, // Volume decreases with distance
            //                     1.0f);   // Keep pitch constant at normal speed
            //                     playsounds.put(targetPlayer.getUniqueId(), player.getUniqueId());
            //             }
            //         }
            //     }
            // }
            
            // player.playSound(player.getLocation(), "minecraft:diaperchange", SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
    }

    private void stopAudio(Player player, int totype, int fromtype) {
        if ((totype == 626002 && fromtype != 0) || fromtype !=0) {
            List<Map.Entry<UUID, UUID>> entriesToRemove = new ArrayList<>();
            // Iterate through each online player to check their distance
            for (Map.Entry<UUID, UUID> entry : playsounds.entrySet()) {
                UUID targetUuid = entry.getKey();
                UUID triggerUuid = entry.getValue();
                
                // Check if the trigger player's UUID matches the current event
                if (triggerUuid.equals(player.getUniqueId())) {  // Assuming 'player' is the trigger player
                    Player targetPlayer = Bukkit.getPlayer(targetUuid);
                    
                    if (targetPlayer != null) {   // Ensure the player is still online
                        targetPlayer.stopSound("minecraft:diaperchange", SoundCategory.PLAYERS);
                        
                        // Mark this entry for removal
                        entriesToRemove.add(entry);
                    }
                }
            }
            for (Map.Entry<UUID, UUID> entry : entriesToRemove) {
                playsounds.remove(entry.getKey());
            }
            // player.stopSound("minecraft:diaperchange", SoundCategory.PLAYERS);
        }
    }
    private void handleInteraction(Player actor, Player target, boolean isCaregiverInteraction) {
        if (!isCaregiverInteraction) {
            target = actor;
        }
        PlayerStats stats = plugin.getPlayerStats(target.getUniqueId());
        ItemStack item = actor.getInventory().getItemInMainHand();

        if (CustomItems.VailidUnderwear(item)) {
            int customModelData = item.getItemMeta().getCustomModelData();
            
            // Remove or decrement the item the actor is holding
            decrementItem(actor, item);
            // Logic to provide items based on wetness and fullness
            distributeUsedItems(actor, target, stats);

            // Reset and update
            resetAndUpdateStats(stats, customModelData, target);

            if (actor == target) {
                actor.sendMessage(ChatColor.GREEN + "You got changed and cleaned!");
            }
            else if(actor != target){
                actor.sendMessage(ChatColor.GREEN + "You changed: " + target.getName());
                target.sendMessage(ChatColor.GREEN + "You were changed by: " + actor.getName() + " Be sure to thank them!");
            }
        } else{
            if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) {
                return;
            }
        }
    }
            
    private void distributeUsedItems(Player actor, Player target, PlayerStats stats) {
        // Distribute items according to the target's diaper status

        if(stats.getDiaperFullness() > 0 && stats.getUnderwearType() > 0){ItemStack stinkydiaper = underwear.createStinkyDiaper(target, (int)stats.getDiaperWetness(),(int)stats.getDiaperFullness(),stats.getTimeWorn());actor.getInventory().addItem(stinkydiaper);}
        else if(stats.getDiaperWetness() > 0 && stats.getUnderwearType() == 1){ItemStack wetpullup = underwear.createWetPullup(target, (int)stats.getDiaperWetness(),(int)stats.getDiaperFullness(),stats.getTimeWorn()); actor.getInventory().addItem(wetpullup);}
        else if(stats.getDiaperWetness() > 0 && stats.getUnderwearType() == 2){ItemStack wetdiaper = underwear.createWetDiaper(target, (int)stats.getDiaperWetness(),(int)stats.getDiaperFullness(),stats.getTimeWorn());actor.getInventory().addItem(wetdiaper);}
        else if(stats.getDiaperWetness() > 0 && stats.getUnderwearType() == 3){ItemStack wetthickdiaper = underwear.createWetThickDiaper(target, (int)stats.getDiaperWetness(),(int)stats.getDiaperFullness(),stats.getTimeWorn());actor.getInventory().addItem(wetthickdiaper);}
        else if (stats.getDiaperWetness() >= 100 && stats.getDiaperFullness() >= 100){
            ItemStack wetANDdirtyunderwear = underwear.createWetANDDirtyUndies(target, (int)stats.getDiaperWetness(),(int)stats.getDiaperFullness(),stats.getTimeWorn());actor.getInventory().addItem(wetANDdirtyunderwear);
        }
        else if(stats.getDiaperWetness() >= 100 && stats.getUnderwearType() == 0){ItemStack wetunderwear = underwear.createWetUndies(target, (int)stats.getDiaperWetness(),(int)stats.getDiaperFullness(),stats.getTimeWorn());actor.getInventory().addItem(wetunderwear);}
        else if(stats.getDiaperFullness() >= 100 && stats.getUnderwearType() == 0){ItemStack dirtyunderwear = underwear.createDirtyUndies(target, (int)stats.getDiaperWetness(),(int)stats.getDiaperFullness(),stats.getTimeWorn());actor.getInventory().addItem(dirtyunderwear);}
        else if (stats.getDiaperFullness() == 0 && stats.getDiaperWetness() == 0 || stats.getUnderwearType() == 0 && stats.getDiaperWetness() < 100) {
            switch (stats.getUnderwearType()) {
                case 0:
                    actor.getInventory().addItem(underwear.Underwear());
                    break;
                case 1:
                    actor.getInventory().addItem(underwear.Pullup());
                    break;
                case 2:
                    actor.getInventory().addItem(underwear.Diaper());
                    break;
                case 3:
                    actor.getInventory().addItem(underwear.ThickDiaper());
                    break;
                default:
                    break;
            }
        }
    }
            
    private void decrementItem(Player actor, ItemStack item) {
        // Decrement or remove the item from the actor's inventory

        int currentAmount = item.getAmount();
        if (currentAmount > 1) {
            item.setAmount(currentAmount - 1);
        } else {
            actor.getInventory().setItemInMainHand(null);
        }
    }

    private void resetAndUpdateStats(PlayerStats stats, int customModelData, Player target) {
        // Reset stats and set new underwear type
        stats.setDiaperFullness(0);
        stats.setDiaperWetness(0);
        stats.setTimeWorn(0);
        stats.setLayers(0);
        if (customModelData == 626002) {stats.setUnderwearType(0);} //Underwear
        if (customModelData == 626003) {stats.setUnderwearType(1);} //Pullup
        if (customModelData == 626009) {stats.setUnderwearType(2);} //Diaper
        if (customModelData == 626001) {stats.setUnderwearType(3);} //Thick Diaper
        Justchanged.put(target.getUniqueId(), true);
        PantsCrafting.equipDiaperArmor(target, true, false);
        plugin.manageParticleEffects(target);
    }
}
