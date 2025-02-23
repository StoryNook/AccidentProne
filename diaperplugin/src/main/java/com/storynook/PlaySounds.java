package com.storynook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PlaySounds {
    private static Plugin plugin;
    public PlaySounds(Plugin plugin) {
        this.plugin = plugin;
    }
    public static void playsounds (Player player, String categoryName, int triggerDistance, Double maxVolume, Double minVolume, Boolean bypass){
        PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());
        World world = player.getLocation().getWorld();
        Collection<Entity> nearbyEntities = world.getNearbyEntities(player.getLocation(), triggerDistance, triggerDistance, triggerDistance);

        Boolean shouldBroadcast = stats.getlethear();
        List<Player> affectedPlayers = new ArrayList<>();

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player) {
                Player targetPlayer = (Player) entity;
                PlayerStats targetstats = plugin.getPlayerStats(targetPlayer.getUniqueId());
                if (targetPlayer != null && targetPlayer.getLocation() != null) {
                    if (shouldBroadcast) {
                        if (targetstats.getcanhear() || bypass) {
                            affectedPlayers.add(targetPlayer);
                        }
                        else if (targetPlayer.equals(player)) {
                            affectedPlayers.add(player);
                        }
                    }
                    else {
                        if (targetPlayer.equals(player)) {
                            affectedPlayers.add(player);
                        }
                    }  
                }
            }
        }
        for (Player target : affectedPlayers) {
            // Skip checking if target is the main player
            String soundName = stats.getRandomSoundFromCategory(categoryName);
            if (target.getUniqueId().equals(player.getUniqueId())) {
                // Play sound for main player without additional checks
                target.playSound(target.getLocation(), 
                                "minecraft:" + soundName, 
                                SoundCategory.PLAYERS, 
                                1.0f, 1.0f);
                continue;
            }

            double distance = target.getLocation().distance(player.getLocation());

            float volume = (float) ((float) ((triggerDistance - distance) / triggerDistance * (maxVolume - minVolume)) + minVolume);

            // Get target's PlayerStats and check if the sound is enabled
            PlayerStats targetStats = plugin.getPlayerStats(target.getUniqueId());
            Map<String, Map<String, Boolean>> targetSounds = targetStats.getStoredSounds();

            boolean isSoundEnabled = false;
            if (targetSounds.containsKey(categoryName)) {
                Map<String, Boolean> soundsInCategory = targetSounds.get(categoryName);
                if (soundsInCategory.containsKey(soundName) && soundsInCategory.get(soundName)) {
                    isSoundEnabled = true;
                }
            }

            // Only play sound if it's enabled for the target
            if (isSoundEnabled) {
                target.playSound(target.getLocation(), 
                                "minecraft:" + soundName, 
                                SoundCategory.PLAYERS, 
                                volume, 1.0f);
            }
        }
    }
}
