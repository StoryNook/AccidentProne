package com.storynook;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


public class StinklinesEffect extends BukkitRunnable {

    private Player player;
    private Plugin plugin;
    
    public StinklinesEffect(Player player, Plugin plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            this.cancel(); // Stop the task if the player is offline
            return;
        }
        
        PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());
        if (stats != null && stats.getStinklines() && stats.getDiaperFullness() >= 100) {
            Location stinkLocation = calculateStinkLocation(player);
            stinkLocation.setPitch(0);
            stinkLocation.add(stinkLocation.getDirection().normalize().multiply(-0.3));
            spawnStinkParticles(stinkLocation);
        }
    }
    private Location calculateStinkLocation(Player player) {
        Location playerLocation = player.getLocation();
        double waveOffsetX = Math.sin(System.currentTimeMillis() / 200.0) * 0.2;
        double waveOffsetZ = Math.cos(System.currentTimeMillis() / 200.0) * 0.2;
        

        return playerLocation.add(waveOffsetX, 1.0, waveOffsetZ);
    }

    private void spawnStinkParticles(Location location) {
        Particle.DustOptions dustOptions = new Particle.DustOptions(org.bukkit.Color.fromRGB(128, 64, 0), 1.0f);
        // player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 3, 0.1, 0.1, 0.1, 0.02);
        player.getWorld().spawnParticle(Particle.REDSTONE, location, 3, 0.1, 0.85, 0.1, 0.02, dustOptions);
    }
}
