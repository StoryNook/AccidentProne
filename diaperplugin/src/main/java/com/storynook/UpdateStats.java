package com.storynook;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.storynook.AccidentsANDWanrings.Warnings;

import net.md_5.bungee.api.ChatColor;

public class UpdateStats {
    private final CommandHandler commandHandler;

    public static Map<UUID, Integer> playerCyclesMap = new HashMap<>();
    public static Map<UUID, Integer> playerSecondsLeftMap = new HashMap<>();
    public static Map<UUID, Boolean> playerWarningsMap = new HashMap<>();

    static HashMap<UUID, Double> bladderfill = new HashMap<>();
    static HashMap<UUID, Double> bowelfill = new HashMap<>();
    static HashMap<UUID, Double> activityMultiplier = new HashMap<>();
    static HashMap<UUID, Integer> HydrationSpike = new HashMap<>();

    private final Plugin plugin;
        public UpdateStats(Plugin plugin, CommandHandler commandHandler) {
            this.plugin = plugin;
            this.commandHandler = commandHandler;
        } 
        public static boolean isNearRunningWater(Player player) {
            Location playerLocation = player.getLocation();
            World world = player.getWorld();
    
            // Define the search radius around the player
            int radius = 3;
    
            for (int x = -radius; x <= radius; x++) {
                for (int y = -1; y <= 1; y++) { // Check one block above and below the player
                    for (int z = -radius; z <= radius; z++) {
                        // Get the block at the current offset
                        Block block = world.getBlockAt(playerLocation.clone().add(x, y, z));
    
                        // Check if the block is water and is flowing
                        if (block.getType() == Material.WATER) {
                            BlockData data = block.getBlockData();
                            if (data instanceof Levelled) {
                                Levelled water = (Levelled) data;
    
                                // Flowing water has a level > 0
                                if (water.getLevel() > 0) {
                                    return true; // Found flowing water nearby
                                }
                            }
                        }
                    }
                }
            }
    
            return false; // No running water found nearby
        }
    
        public static boolean isOutsideInRain(Player player) {
            World world = player.getWorld();
    
            // Check if it is raining in the world
            if (!world.hasStorm()) {
                return false; // No rain, player cannot be in the rain
            }
    
            Location playerLocation = player.getLocation();
            int playerX = playerLocation.getBlockX();
            int playerY = playerLocation.getBlockY();
            int playerZ = playerLocation.getBlockZ();
    
            // Check for blocks directly above the player
            for (int y = playerY + 1; y <= world.getMaxHeight(); y++) {
                Block block = world.getBlockAt(playerX, y, playerZ);
    
                if (!block.isPassable()) { // A non-passable block is blocking the rain
                    return false; // Player is sheltered from the rain
                }
            }
    
            return true; // No blocks above, and it's raining
        }
    
        public void Update(){
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());
            if (commandHandler.NightVisionToggle.getOrDefault(player.getUniqueId(), false))
            {
              player.removePotionEffect(PotionEffectType.NIGHT_VISION);
              player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 15 * 20, 1), true);
            }

            if (stats != null && stats.getOptin() && !(player.getVehicle() instanceof ArmorStand)) { 
              double hydrationDecreaseRate = (0.1 * activityMultiplier.getOrDefault(player.getUniqueId(), 1.0));
              if (player.getLocation().getWorld().getEnvironment() == World.Environment.NETHER) {hydrationDecreaseRate = hydrationDecreaseRate * 2;}
              if (stats.getEffectDuration() == 0) {
                stats.setBladderFillRate(0.2);
                stats.setBowelFillRate(0.07);
              }
              double BladderAdjustedRate = 0;
              if (stats.getHydration() > 100) {
                BladderAdjustedRate = ((stats.getHydration()-100)/100);
                hydrationDecreaseRate += ((stats.getHydration()-100)/100);
              }
              //Decrease Hydration after Hydration check, and activitity multiplier
              stats.decreaseHydration(hydrationDecreaseRate);
              if (HydrationSpike.getOrDefault(player.getUniqueId(), 0) > 0 || isNearRunningWater(player) || isOutsideInRain(player)) {
                BladderAdjustedRate += (stats.getBladderFillRate() * 2) * activityMultiplier.getOrDefault(player.getUniqueId(), 1.0);
                if(HydrationSpike.getOrDefault(player.getUniqueId(), 0) > 0){HydrationSpike.put(player.getUniqueId(), (HydrationSpike.get(player.getUniqueId()) - 1));}
              } else {BladderAdjustedRate += stats.getBladderFillRate() * activityMultiplier.getOrDefault(player.getUniqueId(), 1.0);}
              if (stats.getHydration() < 30) {
                BladderAdjustedRate = 0.1 * activityMultiplier.getOrDefault(player.getUniqueId(), 1.0);
              }
              stats.increaseBladder(BladderAdjustedRate);
              bladderfill.put(player.getUniqueId(), Math.round((BladderAdjustedRate * 100)) / 100.0);

              if (stats.getMessing()) {
                double saturation = player.getSaturation();
                int hunger = player.getFoodLevel();

                double adjustedRate;

                if (saturation > 0) {
                    // While saturation > 0, base fill rate on saturation depletion
                    double saturationImpact = Math.min(saturation / 20.0, 1.0); // Scales from 0 to 1
                    adjustedRate = stats.getBowelFillRate() * activityMultiplier.getOrDefault(player.getUniqueId(), 1.0) * (1 + saturationImpact);
                } else {
                    double hungerImpact = Math.min(2.0, Math.max(1.0, 1.5 - (hunger / 40.0)));
                    adjustedRate  = stats.getBowelFillRate() * activityMultiplier.getOrDefault(player.getUniqueId(), 1.0) * hungerImpact;
                }

                stats.increaseBowels(adjustedRate);
                bowelfill.put(player.getUniqueId(), Math.round((adjustedRate * 100)) / 100.0);
              }
              stats.decreaseEffectDuration(1);
              if (stats.getDiaperFullness() >= 100 || stats.getDiaperWetness() >= 100) {
                stats.increaseTimeWorn(1);
              }
              if (stats.getDiaperFullness() > 0) {
                // double reducedFullness = Math.min(stats.getDiaperFullness(), 100) / 100; // Ensure fullness does not exceed 100
                
                int underweartype = stats.getUnderwearType();
                int diaperFullness = (int) stats.getDiaperFullness();
                
                // Define thresholds for each underwear type
                int[][] fullnessThresholds = {
                    {100},                     // Level 0: 1 threshold at 100%
                    {50, 100},                 // Level 1: thresholds at 50% and 100%
                    {33, 66, 100},             // Level 2: thresholds at ~33%, ~66%, and 100%
                    {25, 50, 75, 100}          // Level 3: thresholds at 25%, 50%, 75%, and 100%
                };
                
                int slownessLevel = 0;
                int[] thresholdsForType = fullnessThresholds[underweartype];
                
                // Iterate from highest to lowest threshold
                for (int i = thresholdsForType.length - 1; i >= 0; i--) {
                    if (diaperFullness >= thresholdsForType[i]) {
                        slownessLevel = i + 1;
                        break;
                    }
                }
                
                player.removePotionEffect(PotionEffectType.SLOW);
                if (slownessLevel > 0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, slownessLevel - 1), true);
                }
              }
              else if(stats.getDiaperFullness() > 0 && stats.getUnderwearType() < 1){
                player.removePotionEffect(PotionEffectType.SLOW);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 1), true);
              }
              else if (stats.getDiaperFullness() == 0) {
                player.removePotionEffect(PotionEffectType.SLOW);
              }
              if (stats.getTimeWorn() >= 600 && player.getHealth() > 1) {
                if (stats.getTimeWorn() == 600) {
                  player.sendMessage(ChatColor.RED + "You are taking damage from a rash.");
                }
                player.damage(0.5);
              }
              if (stats.getHydration() < 10) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 50, 2), true);
              }
              else if (stats.getHydration() >= 10) {
                player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
              }
              int cycles = playerCyclesMap.getOrDefault(player.getUniqueId(), 0);
              int secondsLeft = playerSecondsLeftMap.getOrDefault(player.getUniqueId(), 0);
              boolean warning = playerWarningsMap.getOrDefault(player.getUniqueId(), false);
              if (!warning && cycles > 6) {
                secondsLeft = 0;
                Warnings.triggerWarnings(player, stats);
              }
              else {
                if (cycles > 7 && warning) {
                  cycles = 0;
                }
                if (warning) {
                  secondsLeft++;
                  boolean isBladder = (stats.getBladder() * stats.getBladderIncontinence()) > (stats.getBowels() * stats.getBowelIncontinence()) ? true : false;
                  double fullness = isBladder ? stats.getBladder() : stats.getBowels();
                  double incontinenceLevel = isBladder ? stats.getBladderIncontinence() : stats.getBowelIncontinence();
                  Warnings.sneakCheck(player, stats, fullness, incontinenceLevel, isBladder);
                }
              }
              cycles++;
              playerCyclesMap.put(player.getUniqueId(), cycles);
              playerSecondsLeftMap.put(player.getUniqueId(), secondsLeft);
              // getLogger().info("Waning: " + warning + " cycles: " + cycles + " SecondsLeft: " + secondsLeft + " Player: " + player.getName());
          }
        }
    }
}
