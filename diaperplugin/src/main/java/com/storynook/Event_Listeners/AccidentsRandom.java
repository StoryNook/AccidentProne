package com.storynook.Event_Listeners;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.util.Vector;

import com.storynook.PlayerStats;
import com.storynook.Plugin;

public class AccidentsRandom implements Listener {
    private final Plugin plugin;
    public AccidentsRandom(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLightningStrike(LightningStrikeEvent event) {
        Location strikeLocation = event.getLightning().getLocation();
        
        // Iterate through all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isNearLightningInWorld(player.getLocation(), strikeLocation, player.getWorld())) {
                handleThunderEffect(player);
            }
        }
    }
    private boolean isNearLightningInWorld(Location location, Location lightningLocation, World world) {
        // First check if they are in the same world
        if (!location.getWorld().equals(world)) {
            return false;
        }
        
        // Check distance between the two locations within the specified radius (50 blocks)
        return location.distance(lightningLocation) <= 50;
    }

    // Handle an accident if the player is near thunder
    private void handleThunderEffect(Player player) {
        PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());
        Random random = new Random();
        if (stats != null && stats.getOptin()) {
            double maxIncontinence = Math.max(stats.getBladderIncontinence(), stats.getBowelIncontinence());
            double chance = Math.min(4, Math.max(0, maxIncontinence / 2));// 1 in 4 chance of having an accident
            if (random.nextInt(4) < chance) {
                boolean bladderAccident = stats.getBladderIncontinence() >= stats.getBowelIncontinence();
                if (bladderAccident ? stats.getBladder() > 10 : stats.getBowels() > 10) {
                    stats.handleAccident(bladderAccident, player, true, true);
                    player.sendMessage("You got so scared by the lightening that you had an accident!");
                }
                return;
            }
        }
    }
    //Mob attack envirorment check
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Entity damager = event.getDamager();

        // Check if it is night or the player is in a dark place
        boolean isNight = player.getWorld().getTime() > 12300 && player.getWorld().getTime() < 23850;
        int lightLevel = player.getLocation().getBlock().getLightLevel();
        boolean isDark = lightLevel < 7;

        if ((isNight || isDark) && damager instanceof Mob) {
            // Check if the mob is not on the player's screen
            Vector toEntity = damager.getLocation().toVector().subtract(player.getLocation().toVector());
            Vector direction = player.getLocation().getDirection();

            // Check if mob is outside field of view (Use a dot product for a simple "in-front" check)
            double fieldOfView = 0.5; // Adjust for larger FOV
            if (direction.dot(toEntity.normalize()) < fieldOfView) {
                handleScaryEvent(player);
            }
        }
    }
    //Mob Attack chance
    private void handleScaryEvent(Player player) {
        PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());
        Random random = new Random();
        if (stats != null && stats.getOptin()) {
            double maxIncontinence = Math.max(stats.getBladderIncontinence(), stats.getBowelIncontinence());
            double chance = Math.min(4, Math.max(0, maxIncontinence / 2));

            if (random.nextInt(4) < chance) {
                if (stats.getBladder() > 10 || stats.getBowels() > 10) {
                    boolean bladderAccident = stats.getBladderIncontinence() >= stats.getBowelIncontinence();
                    stats.handleAccident(bladderAccident, player, true, true);
                    player.sendMessage("You got so scared by the attack that you had an accident!");
                }
            }
        }
    }
    //Bedwetting chance
    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        // Check if world time indicates it's daytime (usually around 0 to 1,000 ticks in Minecraft)
        long worldTime = player.getWorld().getTime();
        if (worldTime >= 0 && worldTime < 1000) {
            PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());
            if (stats != null && stats.getOptin()) {
                
                if (stats.getBedwetting() == 1) {
                    double chance = Math.min(4, Math.max(0, stats.getBladderIncontinence() / 2));
                    Random random = new Random();
                    if (random.nextInt(4) < chance) {
                        if (stats.getBladder() > 10) {
                            stats.handleAccident(true, player,false, true);
                            player.sendMessage("Oh no! You wet the bed!");
                            return;
                        }
                    }
                }
                else if(stats.getBedwetting() == 2){
                    if (stats.getBladder() > 10) {
                        stats.handleAccident(true, player,false, true);
                        player.sendMessage("Oh no! You wet the bed!");
                        return;
                    }
                }
                stats.increaseBladder(40);
                if(stats.getMessing()){stats.increaseBowels(20);}
            }
        }
    }
}
