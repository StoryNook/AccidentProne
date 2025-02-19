package com.storynook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.ScoreboardManager;

import com.storynook.Event_Listeners.AccidentsRandom;
import com.storynook.Event_Listeners.Changing;
import com.storynook.Event_Listeners.Laxative;
import com.storynook.Event_Listeners.PantsCrafting;
import com.storynook.Event_Listeners.Sit;
import com.storynook.Event_Listeners.Toilet;
import com.storynook.Event_Listeners.washer;
import com.storynook.items.ItemManager;
import com.storynook.items.pants;
import com.storynook.items.underwear;
import com.storynook.menus.Caregivermenu;
import com.storynook.menus.HUDMenu;
import com.storynook.menus.IncontinenceMenu;
import com.storynook.menus.SettingsMenu;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

/*
 * diaperplugin java plugin
 */
public class Plugin extends JavaPlugin
{
  private HashMap<UUID, PlayerStats> playerStatsMap = new HashMap<>();
  private final Map<UUID, ArmorStand> armorStandTracker = new HashMap<>();
  // private Map<UUID, Integer> playerCyclesMap = new HashMap<>();
  // public Map<UUID, Integer> playerSecondsLeftMap = new HashMap<>();
  // public Map<UUID, Boolean> playerWarningsMap = new HashMap<>();
  public HashMap<UUID, Integer> rightclickCount = new HashMap<>();
  // HashMap<UUID, Integer> HydrationSpike = new HashMap<>();
  // HashMap<UUID, Double> activityMultiplier = new HashMap<>();
  HashMap<UUID, Boolean> wasJumping = new HashMap<>();
  HashMap<UUID, Boolean> wasSprinting = new HashMap<>();
  public HashMap<UUID, Boolean> firstimeran = new HashMap<>();
  // HashMap<UUID, Double> bladderfill = new HashMap<>();
  // HashMap<UUID, Double> bowelfill = new HashMap<>();
  private Map<UUID, BukkitTask> ParticleEffects = new HashMap<>();
  // private Map<UUID, Boolean> playerSneakStatus = new HashMap<>();


  public Map<UUID, ArmorStand> getArmorStandTracker() {
      return armorStandTracker;
  }
  private CommandHandler commandHandler;
  private ScoreboardManager manager;

  @Override
  public void onEnable()
  {
    getLogger().info("Plugin started, onEnable");
    //Creates DataFoler if it doesn't exist.
    if (!getDataFolder().exists()) {
      getDataFolder().mkdirs();
    }
    File playerDataFolder = new File(getDataFolder(), "players");
    if (!playerDataFolder.exists()) {
      try{
        playerDataFolder.mkdirs();
        getLogger().info("Player data folder created");
      } catch (Exception e) {
        getLogger().warning("FIALED:Player data folder not created");
      }
    }

    mergeConfigFiles("config.yml");
    mergeConfigFiles("sounds.yml");
    
    PlayerEventListener playerEventListener = new PlayerEventListener(this);
    Sit sit = new Sit(this);
    Toilet toilet = new Toilet(this);
    SettingsMenu SettingsMenu = new SettingsMenu(this);
    Caregivermenu caregivermenu = new Caregivermenu(this);
    HUDMenu hudmenu = new HUDMenu(this);
    IncontinenceMenu incontinencemenu = new IncontinenceMenu(this);
    PantsCrafting pantsCrafting = new PantsCrafting(this);
    AccidentsRandom accident = new AccidentsRandom(this);
    Changing change = new Changing(this);
    washer washer = new washer(this);
    Laxative lax = new Laxative(this);
    //Create all the custom recipes and items related
    ItemManager itemManager = new ItemManager(this);
    underwear underwear = new underwear(this);
    pants pants = new pants(this);

    itemManager.createToiletRecipe();
    underwear.createAllRecipes();
    pants.createCleanPantsRecipe();
    pants.WashedPants();
    itemManager.createWasherRecipe();
    // itemManager.createDiaperPailRecipe();
    // itemManager.createLaxRecipe();
  
   
    // Create an array of all your listener objects
    Object[] listeners = new Object[]{playerEventListener, SettingsMenu, caregivermenu, incontinencemenu, hudmenu, pantsCrafting, washer, sit, toilet, accident, lax, change};

    // Loop through and register each listener
    for (Object listener : listeners) {
        if (listener instanceof Listener) {
            getServer().getPluginManager().registerEvents((Listener) listener, this);
        }
    }
    UpdateStats();

    playerStatsMap = new HashMap<UUID, PlayerStats>();

    //Registers the commands
    commandHandler = new CommandHandler(this);

    String[] singleCommands = {"settings", "pee", "poop", "stats", "nightvision", "nv"};
    for (String cmd : singleCommands) {
        getCommand(cmd).setExecutor(commandHandler);
    }

    String[] dualCommands = {"debug", "check", "caregiver", "lockincon", "unlockincon", "minfill"};
    for (String cmd : dualCommands) {
        getCommand(cmd).setExecutor(commandHandler);
        getCommand(cmd).setTabCompleter(commandHandler);
    }

    //Score board setup
    manager = Bukkit.getScoreboardManager();
    UpdateStatsBar();
  }

  private void mergeConfigFiles(String fileName) {
    File dataFile = new File(getDataFolder(), fileName);

    try {
        if (!dataFile.exists()) {
            // Create file from resources
            InputStream resourceStream = this.getClass().getClassLoader()
                    .getResourceAsStream(fileName);

            if (resourceStream == null) {
                getLogger().severe("Could not find " + fileName + " in resources!");
                return;
            }

            try (OutputStream fileOutputStream = new FileOutputStream(dataFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = resourceStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
                getLogger().info("Created " + fileName);
            } finally {
                if (resourceStream != null) {
                    resourceStream.close();
                }
            }
        } else {
            // If file exists, merge new values without overwriting existing ones
            YamlConfiguration existingConfig = YamlConfiguration.loadConfiguration(dataFile);

            InputStream resourceStream = this.getClass().getClassLoader()
                    .getResourceAsStream(fileName);

            if (resourceStream == null) {
                getLogger().severe("Could not find " + fileName + " in resources!");
                return;
            }

            try {
                // Read the resource stream into a temporary file
                File tempFile = new File(getDataFolder(), "temp_" + fileName);
                try (OutputStream tempOutputStream = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = resourceStream.read(buffer)) != -1) {
                        tempOutputStream.write(buffer, 0, bytesRead);
                    }
                }

                YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(tempFile);

                // Merge new configuration into existing one
                // This will add missing entries but keep existing values
                for (String key : newConfig.getKeys(true)) {
                    if (!existingConfig.contains(key)) {
                        existingConfig.set(key, newConfig.get(key));
                    }
                }

                // Save merged configuration back to file
                existingConfig.save(dataFile);
                getLogger().info("Updated " + fileName);

                // Clean up temporary file
                tempFile.delete();
            } finally {
                resourceStream.close();
            }
        }
    } catch (IOException e) {
        getLogger().severe("Error handling " + fileName + ": " + e.getMessage());
        e.printStackTrace();
    }
  }

  @Override
  public void onDisable()
  {
    for (BukkitTask task : ParticleEffects.values()) {
        if (task != null) {
            task.cancel();
        }
    }
    saveAllPlayerStats();
  }

  public void loadPlayerStats(Player player) {
    getLogger().warning("Player Joined " + player.getName());
    UUID playerUUID = player.getUniqueId();
    PlayerStats stats = new PlayerStats(playerUUID, this);
    File playerFile = getPlayerFile(playerUUID);
    

    if (playerFile.exists()) {
      getLogger().info("Loading stats for player " + player.getName());
      FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

      stats.setBladder((double) config.getDouble("bladder", 0));
      stats.setBowels((double) config.getDouble("bowels", 0));
      stats.setDiaperWetness((double) config.getDouble("diaperWetness", 0));
      stats.setDiaperFullness((double) config.getDouble("diaperFullness", 0));
      stats.setBladderIncontinence((double) config.getDouble("bladderIncontinence", 1));
      stats.setBowelIncontinence((double) config.getDouble("bowelIncontinence", 1));
      stats.setBladderFillRate((double) config.getDouble("bladderFillRate", 0.2));
      stats.setBowelFillRate((double) config.getDouble("bowelFillRate", 0.07));
      stats.setHydration((double) config.getInt("hydration", 100));
      stats.setUrgeToGo((int) config.getInt("urgeToGo", 1));
      stats.setUnderwearType((int) config.getInt("UnderwearType", 0));
      stats.setLayers(config.getInt("Layers", 0));
      stats.setOptin(config.getBoolean("Optin"));
      stats.setMessing(config.getBoolean("Messing"));
      stats.setParticleEffects(config.getInt("Stinklines", 0));
      stats.setUI((int) config.getInt("UI", 1));
      stats.setBedwetting((int) config.getInt("Bedwetting", 0));
      stats.setEffectDuration((int) config.getInt("EffectDuration", 0));
      stats.setTimeWorn((int) config.getInt("TimeWorn", 0));
      stats.setMinFill((int) config.getInt("MinFill", 30));
      stats.setHardcore(config.getBoolean("Hardcore", false));
      stats.setHardcoreEnabledTime(config.getLong("hardcoreEnabledTime", -1L));
      stats.setspecialCG(config.getBoolean("specialCG", false));
      stats.setAllCaregiver(config.getBoolean("AllCaregiver", false));
      stats.setvisableUnderwear(config.getBoolean("visableUnderwear", false));
      stats.setBladderLockIncon(config.getBoolean("BladderLockIncon"));
      stats.setBowelLockIncon(config.getBoolean("BowelLockIncon"));
      stats.setfillbar(config.getBoolean("FillBar", false));
      stats.setshowfill(config.getBoolean("ShowFill",false));
      stats.setshowunderwear(config.getBoolean("showunderwear", true));
      if (config.contains("Caregivers")) {
        for (String uuid : config.getStringList("Caregivers")){
          stats.addCaregiver(UUID.fromString(uuid));
        }
      } 
      playerStatsMap.put(playerUUID, stats);
      if (stats.getOptin() && stats.getUI() > 0) {
        ScoreBoard.createSidebar(player);
      }
    }
    else {
      getLogger().info("No stats file found for player " + player.getName() + ". Creating default stats.");
      
      // Create new PlayerStats with default values
      // Set the default values if needed
      stats.setBladder(0);
      stats.setBowels(0);
      stats.setDiaperWetness(0);
      stats.setDiaperFullness(0);
      stats.setBladderIncontinence(1);
      stats.setBowelIncontinence(1);
      stats.setBladderFillRate(0.2); 
      stats.setBowelFillRate(0.07);   
      stats.setHydration(100);
      stats.setUrgeToGo(1);
      stats.setUnderwearType(0);
      stats.setLayers(0);
      stats.setOptin(false);
      stats.setMessing(false);
      stats.setParticleEffects(0);
      stats.setAllCaregiver(false);
      stats.setspecialCG(false);
      stats.setEffectDuration(0);
      stats.setTimeWorn(0);
      stats.setMinFill(30);
      stats.setHardcore(false);
      stats.setHardcoreEnabledTime(-1);
      stats.setvisableUnderwear(false);
      stats.setBladderLockIncon(false);
      stats.setBowelLockIncon(false);
      stats.setfillbar(false);
      stats.setshowfill(false);
      stats.setshowunderwear(true);
      stats.setUI(1);
      stats.setBedwetting(0);
      
      // Store the default stats in the playerStatsMap
      playerStatsMap.put(playerUUID, stats);

      // Save the default stats to a new file
      savePlayerStats(player);
    }
  }
  public void savePlayerStats(Player player) {
    getLogger().info("Save Player Stats loaded");
    UUID playerUUID = player.getUniqueId();
    PlayerStats stats = playerStatsMap.get(playerUUID);
    if (stats != null) {
        // Save stats to a file or database (not implemented in this example)
        File playerFile = getPlayerFile(playerUUID);
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

            // Save stats to the YAML file
            config.set("PlayerName", player.getName());
            config.set("bladder", stats.getBladder());
            config.set("bowels", stats.getBowels());
            config.set("diaperWetness", stats.getDiaperWetness());
            config.set("diaperFullness", stats.getDiaperFullness());
            config.set("bladderIncontinence", stats.getBladderIncontinence());
            config.set("bowelIncontinence", stats.getBowelIncontinence());
            config.set("bladderFillRate", stats.getBladderFillRate());
            config.set("bowelFillRate", stats.getBowelFillRate());
            config.set("hydration", stats.getHydration());
            config.set("urgeToGo", stats.getUrgeToGo());
            config.set("UnderwearType", stats.getUnderwearType());
            config.set("Layers", stats.getLayers());
            config.set("Optin", stats.getOptin());
            config.set("Messing", stats.getMessing());
            config.set("Stinklines", stats.getParticleEffects());
            config.set("specialCG", stats.getspecialCG());
            config.set("AllCaregiver", stats.getAllCaregiver());
            config.set("UI", stats.getUI());
            config.set("Bedwetting", stats.getBedwetting());
            config.set("EffectDuration", stats.getEffectDuration());
            config.set("TimeWorn", stats.getTimeWorn());
            config.set("MinFill", stats.getMinFill());
            config.set("Hardcore", stats.getHardcore());
            config.set("hardcoreEnabledTime", stats.getHardcoreEnabledTime());
            config.set("visableUnderwear", stats.getvisableUnderwear());
            config.set("BladderLockIncon", stats.getBladderLockIncon());
            config.set("BowelLockIncon", stats.getBowelLockIncon());
            config.set("ShowFill", stats.getshowfill());
            config.set("FillBar", stats.getfillbar());
            config.set("showunderwear", stats.getshowunderwear());
            List<String> uuidStrings = stats.getCaregivers().stream()
            .map(UUID::toString) // Convert UUID to string
            .collect(Collectors.toList());
            config.set("Caregivers", uuidStrings);
            
            try {
              config.save(playerFile);
              getLogger().info("Saved stats for player " + player.getName());
          } catch (IOException e) {
              getLogger().warning("Failed to save stats for player " + player.getName());
              e.printStackTrace();
          }
    }
  }
  private File getPlayerFile(UUID playerUUID) {
    return new File(getDataFolder(), "players" + File.separator + playerUUID.toString() + ".yml");
  }
  public PlayerStats getPlayerStats(UUID playerUUID) {
    return playerStatsMap.get(playerUUID);
  }
  private void saveAllPlayerStats() {
        for (UUID playerUUID : playerStatsMap.keySet()) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                savePlayerStats(player);
            }
        }
  }
  // public boolean isNearRunningWater(Player player) {
  //   Location playerLocation = player.getLocation();
  //   World world = player.getWorld();

  //   // Define the search radius around the player
  //   int radius = 3;

  //   for (int x = -radius; x <= radius; x++) {
  //       for (int y = -1; y <= 1; y++) { // Check one block above and below the player
  //           for (int z = -radius; z <= radius; z++) {
  //               // Get the block at the current offset
  //               Block block = world.getBlockAt(playerLocation.clone().add(x, y, z));

  //               // Check if the block is water and is flowing
  //               if (block.getType() == Material.WATER) {
  //                   BlockData data = block.getBlockData();
  //                   if (data instanceof Levelled) {
  //                       Levelled water = (Levelled) data;

  //                       // Flowing water has a level > 0
  //                       if (water.getLevel() > 0) {
  //                           return true; // Found flowing water nearby
  //                       }
  //                   }
  //               }
  //           }
  //       }
  //   }

  //   return false; // No running water found nearby
  // }

  // public boolean isOutsideInRain(Player player) {
  //   World world = player.getWorld();

  //   // Check if it is raining in the world
  //   if (!world.hasStorm()) {
  //       return false; // No rain, player cannot be in the rain
  //   }

  //   Location playerLocation = player.getLocation();
  //   int playerX = playerLocation.getBlockX();
  //   int playerY = playerLocation.getBlockY();
  //   int playerZ = playerLocation.getBlockZ();

  //   // Check for blocks directly above the player
  //   for (int y = playerY + 1; y <= world.getMaxHeight(); y++) {
  //       Block block = world.getBlockAt(playerX, y, playerZ);

  //       if (!block.isPassable()) { // A non-passable block is blocking the rain
  //           return false; // Player is sheltered from the rain
  //       }
  //   }

  //   return true; // No blocks above, and it's raining
  // }


  private void UpdateStats() {
    Bukkit.getScheduler().runTaskTimer(this, () -> {
      UpdateStats updateStats = new UpdateStats(this, commandHandler);
      updateStats.Update();
        // for (Player player : Bukkit.getOnlinePlayers()) {
        //     PlayerStats stats = getPlayerStats(player.getUniqueId());
        //     if (commandHandler.NightVisionToggle.getOrDefault(player.getUniqueId(), false))
        //     {
        //       player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        //       player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 15 * 20, 1), true);
        //     }

        //     if (stats != null && stats.getOptin() && !(player.getVehicle() instanceof ArmorStand)) { 
        //       double hydrationDecreaseRate = (0.1 * activityMultiplier.getOrDefault(player.getUniqueId(), 1.0));
        //       if (player.getLocation().getWorld().getEnvironment() == World.Environment.NETHER) {hydrationDecreaseRate = hydrationDecreaseRate * 2;}
        //       if (stats.getEffectDuration() == 0) {
        //         stats.setBladderFillRate(0.2);
        //         stats.setBowelFillRate(0.07);
        //       }
        //       double BladderAdjustedRate = 0;
        //       if (stats.getHydration() > 100) {
        //         BladderAdjustedRate = ((stats.getHydration()-100)/100);
        //         hydrationDecreaseRate += ((stats.getHydration()-100)/100);
        //       }
        //       //Decrease Hydration after Hydration check, and activitity multiplier
        //       stats.decreaseHydration(hydrationDecreaseRate);
        //       if (HydrationSpike.getOrDefault(player.getUniqueId(), 0) > 0 || isNearRunningWater(player) || isOutsideInRain(player)) {
        //         BladderAdjustedRate += (stats.getBladderFillRate() * 2) * activityMultiplier.getOrDefault(player.getUniqueId(), 1.0);
        //         if(HydrationSpike.getOrDefault(player.getUniqueId(), 0) > 0){HydrationSpike.put(player.getUniqueId(), (HydrationSpike.get(player.getUniqueId()) - 1));}
        //       } else {BladderAdjustedRate += stats.getBladderFillRate() * activityMultiplier.getOrDefault(player.getUniqueId(), 1.0);}
        //       if (stats.getHydration() < 30) {
        //         BladderAdjustedRate = 0.1 * activityMultiplier.getOrDefault(player.getUniqueId(), 1.0);
        //       }
        //       stats.increaseBladder(BladderAdjustedRate);
        //       bladderfill.put(player.getUniqueId(), Math.round((BladderAdjustedRate * 100)) / 100.0);

        //       if (stats.getMessing()) {
        //         double saturation = player.getSaturation();
        //         int hunger = player.getFoodLevel();

        //         double adjustedRate;

        //         if (saturation > 0) {
        //             // While saturation > 0, base fill rate on saturation depletion
        //             double saturationImpact = Math.min(saturation / 20.0, 1.0); // Scales from 0 to 1
        //             adjustedRate = stats.getBowelFillRate() * activityMultiplier.getOrDefault(player.getUniqueId(), 1.0) * (1 + saturationImpact);
        //         } else {
        //             double hungerImpact = Math.min(2.0, Math.max(1.0, 1.5 - (hunger / 40.0)));
        //             adjustedRate  = stats.getBowelFillRate() * activityMultiplier.getOrDefault(player.getUniqueId(), 1.0) * hungerImpact;
        //         }

        //         stats.increaseBowels(adjustedRate);
        //         bowelfill.put(player.getUniqueId(), Math.round((adjustedRate * 100)) / 100.0);
        //       }
        //       stats.decreaseEffectDuration(1);
        //       if (stats.getDiaperFullness() >= 100 || stats.getDiaperWetness() >= 100) {
        //         stats.increaseTimeWorn(1);
        //       }
        //       if (stats.getDiaperFullness() > 0) {
        //         // double reducedFullness = Math.min(stats.getDiaperFullness(), 100) / 100; // Ensure fullness does not exceed 100
                
        //         int underweartype = stats.getUnderwearType();
        //         int diaperFullness = (int) stats.getDiaperFullness();
                
        //         // Define thresholds for each underwear type
        //         int[][] fullnessThresholds = {
        //             {100},                     // Level 0: 1 threshold at 100%
        //             {50, 100},                 // Level 1: thresholds at 50% and 100%
        //             {33, 66, 100},             // Level 2: thresholds at ~33%, ~66%, and 100%
        //             {25, 50, 75, 100}          // Level 3: thresholds at 25%, 50%, 75%, and 100%
        //         };
                
        //         int slownessLevel = 0;
        //         int[] thresholdsForType = fullnessThresholds[underweartype];
                
        //         // Iterate from highest to lowest threshold
        //         for (int i = thresholdsForType.length - 1; i >= 0; i--) {
        //             if (diaperFullness >= thresholdsForType[i]) {
        //                 slownessLevel = i + 1;
        //                 break;
        //             }
        //         }
                
        //         player.removePotionEffect(PotionEffectType.SLOW);
        //         if (slownessLevel > 0) {
        //             player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, slownessLevel - 1), true);
        //         }
        //       }
        //       else if(stats.getDiaperFullness() > 0 && stats.getUnderwearType() < 1){
        //         player.removePotionEffect(PotionEffectType.SLOW);
        //         player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 1), true);
        //       }
        //       else if (stats.getDiaperFullness() == 0) {
        //         player.removePotionEffect(PotionEffectType.SLOW);
        //       }
        //       if (stats.getTimeWorn() >= 600 && player.getHealth() > 1) {
        //         if (stats.getTimeWorn() == 600) {
        //           player.sendMessage(ChatColor.RED + "You are taking damage from a rash.");
        //         }
        //         player.damage(0.5);
        //       }
        //       if (stats.getHydration() < 10) {
        //         player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 50, 2), true);
        //       }
        //       else if (stats.getHydration() >= 10) {
        //         player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
        //       }
        //       int cycles = playerCyclesMap.getOrDefault(player.getUniqueId(), 0);
        //       int secondsLeft = playerSecondsLeftMap.getOrDefault(player.getUniqueId(), 0);
        //       boolean warning = playerWarningsMap.getOrDefault(player.getUniqueId(), false);
        //       if (!warning && cycles > 6) {
        //         secondsLeft = 0;
        //         triggerWarnings(player, stats);
        //       }
        //       else {
        //         if (cycles > 7 && warning) {
        //           cycles = 0;
        //         }
        //         if (warning) {
        //           secondsLeft++;
        //           boolean isBladder = (stats.getBladder() * stats.getBladderIncontinence()) > (stats.getBowels() * stats.getBowelIncontinence()) ? true : false;
        //           double fullness = isBladder ? stats.getBladder() : stats.getBowels();
        //           double incontinenceLevel = isBladder ? stats.getBladderIncontinence() : stats.getBowelIncontinence();
        //           sneakCheck(player, stats, fullness, incontinenceLevel, isBladder);
        //         }
        //       }
        //       cycles++;
        //       playerCyclesMap.put(player.getUniqueId(), cycles);
        //       playerSecondsLeftMap.put(player.getUniqueId(), secondsLeft);
        //       // getLogger().info("Waning: " + warning + " cycles: " + cycles + " SecondsLeft: " + secondsLeft + " Player: " + player.getName());
        //   }
        // }
    }, 0L, 20L * 2);  // Run every 2 second (40 ticks)
  }
  public void calculateUrgeToGo(PlayerStats stats){
    double bladderUrge = stats.getBladder() * stats.getBladderIncontinence();
    double bowelUrge = stats.getBowels() * stats.getBowelIncontinence();
    double urgeToGo = Math.max(bladderUrge, bowelUrge);
    stats.setUrgeToGo((int)urgeToGo);
  }
  //Warning system, and accident triggers.
  public void triggerWarnings(Player player, PlayerStats stats) {
      if (stats != null) {
        calculateUrgeToGo(stats);
        boolean warning = UpdateStats.playerWarningsMap.getOrDefault(player.getUniqueId(), false);
        boolean isBladder = (stats.getBladder() * stats.getBladderIncontinence()) > (stats.getBowels() * stats.getBowelIncontinence()) ? true : false;
        double random = (Math.random() * 100) + (stats.getMinFill() - ((isBladder ? stats.getBladderIncontinence() : stats.getBowelIncontinence()) * (stats.getMinFill()/10)));
        if (Math.min((int)random, 100) <= ((int)stats.getUrgeToGo()) && warning == false) {
          handleWarning(player, stats, isBladder);
        }
      }
  }

  private void handleWarning(Player player, PlayerStats stats, boolean isBladder) {
    double fullness = isBladder ? stats.getBladder() : stats.getBowels();
    double incontinenceLevel = isBladder ? stats.getBladderIncontinence() : stats.getBowelIncontinence();
    double randomChance = (Math.random() * 10) + 1;
    double accidentProbability = Math.min(0.0, (incontinenceLevel * (fullness/10)));

    if (randomChance < accidentProbability) {
      stats.handleAccident(isBladder, player, true, false);
    } else if ((fullness/10) >= ((Math.random() * 8) + 1)) {
      if(stats.getUrgeToGo() > 50 && stats.getUrgeToGo() < 75){
        player.sendMessage(isBladder ? "You REALLY need to pee!" : "You REALLY need to Poop!");
        player.sendMessage("Hold sneak to hold it in! You only have 5 seconds!");
        UpdateStats.playerWarningsMap.put(player.getUniqueId(), true);
      }else if(stats.getUrgeToGo() > 75){
        player.sendMessage("You can't hold it much longer!");
        UpdateStats.playerWarningsMap.put(player.getUniqueId(), true);
      }else{
        player.sendMessage(isBladder ? "You need to pee!" : "You need to Poop!");
        player.sendMessage("Hold sneak to hold it in! You only have 5 seconds!");
        UpdateStats.playerWarningsMap.put(player.getUniqueId(), true);
      }
    }
  }

  public void sneakCheck(Player player, PlayerStats stats, double need, double incontinenceLevel, boolean isBladder) {
    double randomChance = (Math.random() * 10) + 1;
    boolean sneakFails = randomChance <= incontinenceLevel;
    int secondsleft = UpdateStats.playerSecondsLeftMap.get(player.getUniqueId());
    
    if ((!player.isSneaking() || sneakFails) && (isBladder ? stats.getBladder() > 10 : stats.getBowels() > 10) && secondsleft > 3) { 
      if (sneakFails && player.isSneaking()) {
        player.sendMessage("Your body has betrayed you. You couldn't hold it.");
        stats.handleAccident(isBladder, player, true, true);
      }
      else {stats.handleAccident(isBladder, player, true, false);}
      UpdateStats.playerSecondsLeftMap.put(player.getUniqueId(), 0);
      UpdateStats.playerWarningsMap.put(player.getUniqueId(), false);
    } else {
      if((isBladder ? stats.getBladder() > 10 : stats.getBowels() > 10) && player.isSneaking() && secondsleft <= 3){
        player.sendMessage("Good job! You held it in.");
        UpdateStats.playerSecondsLeftMap.put(player.getUniqueId(), 0);
        UpdateStats.playerWarningsMap.put(player.getUniqueId(), false);
      }
    }
  }

  private String buildStatusBar(int value, char fullChar, char emptyChar, boolean isWater){
    StringBuilder statusBar = new StringBuilder();
    int fullCount;
    if (value > 100) {
      value = 100;
    }
    if (isWater) {
      fullCount = (int) Math.ceil(value / 10.0);
    }
    else{
      fullCount = value/10;
    }
    int emptyCount = 10 - fullCount;
    if (isWater) {
      for (int i = 0; i < emptyCount; i++) {
        statusBar.append(emptyChar);
      }
      for (int i = 0; i < fullCount; i++) {
        statusBar.append(fullChar);
      }
    }
    else{
      for (int i = 0; i < fullCount; i++) {
        statusBar.append(fullChar);
      }
      for (int i = 0; i < emptyCount; i++) {
          statusBar.append(emptyChar);
      }
    }
      return statusBar.toString();
  }

  public void UpdateStatsBar() {
    char hydrationFull = '\uE043';
    char hydrationEmpty = '\uE044';
    char bladderFull = '\uE042';
    char bladderEmpty = '\uE045';
    char bowelsFull = '\uE048';
    char bowelsEmpty = '\uE049';

    Bukkit.getScheduler().runTaskTimer(this, () -> {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerStats stats = getPlayerStats(player.getUniqueId());
            if (stats != null && stats.getOptin() && stats.getUI() == 1) {
                ScoreBoard.updateSidebar(player, stats, UpdateStats.bladderfill.getOrDefault(player.getUniqueId(), 0.0), UpdateStats.bowelfill.getOrDefault(player.getUniqueId(), 0.0));
                if(player.getRemainingAir() == player.getMaximumAir()){
                  String hydrationBar = buildStatusBar((int)stats.getHydration(), hydrationFull, hydrationEmpty, true);
                  player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("\uF82B\uF82A\uF825"+hydrationBar));
                }
            }
            else if(stats != null && stats.getOptin() && stats.getUI() == 2){
              if(player.getRemainingAir() == player.getMaximumAir()){
                String hydrationBar = buildStatusBar((int)stats.getHydration(), hydrationFull, hydrationEmpty, true);
                String bladderBar = buildStatusBar((int)stats.getBladder(), bladderFull, bladderEmpty, false);
                String underwearImage = ScoreBoard.getUnderwearStatus((int)stats.getDiaperWetness(), (int)stats.getDiaperFullness(), (int)stats.getUnderwearType(), 0);
                if (stats.getMessing()){
                  String bowelBar = buildStatusBar((int)stats.getBowels(), bowelsFull, bowelsEmpty, false);
                  String withBowels = "\uF82B\uF82A\uF825\uF829\uF828" + hydrationBar + "\uF82A\uF80C\uF829" + bladderBar + "\uF80B\uF809" + bowelBar + (stats.getshowunderwear() ? underwearImage : "");
                  player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(withBowels));
                }
                else{
                  String basicBar = "\uF82B\uF82A\uF825\uF829\uF828" + hydrationBar + "\uF82A\uF80C\uF829" + bladderBar + (stats.getshowunderwear() ? underwearImage : "");
                  player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(basicBar));
                }
              }
            }
            else {player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());}
        }
    }, 0L, 20L);  // Update every second (20 ticks)
  }

  public void CheckLittles(Player player, PlayerStats stats, Player target){
    if (stats != null) {
        // Check for target player argument and optional validation
        String image = "";
        String state = "";
        
        if (stats != null) {
            image = ScoreBoard.getUnderwearStatus((int)stats.getDiaperWetness(), (int)stats.getDiaperFullness(), (int)stats.getUnderwearType(), 2);
            if ((int)stats.getDiaperWetness() > 1 && (int)stats.getDiaperFullness() > 1) {
                // state = "Wet And Messy";
            } else if ((int)stats.getDiaperWetness() > 1) {
                // state = "Wet";
            } else if ((int)stats.getDiaperFullness() > 1) {
                // state = "Messy";
            } else if ((int)stats.getDiaperFullness() == 0 && (int)stats.getDiaperWetness() == 0) {
                // state = "Clean";
            }
            player.sendTitle(image, state, 10, 20, 10);
            if (player != target) {
              target.sendTitle(ChatColor.GOLD + player.getName(), ChatColor.AQUA +" Just checked you.", 10, 20, 10);
            }
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.schedule(() -> rightclickCount.put(player.getUniqueId(), 0), 4, TimeUnit.SECONDS);
            // rightclickCount.put(player.getUniqueId(), 0);
        } 
    }
  }

  public void manageParticleEffects(Player player) {
    // UUID playerUUID = player.getUniqueId();
    // PlayerStats stats = getPlayerStats(playerUUID);
    
    // if (stats.getParticleEffects() == 2 && stats.getDiaperFullness() >= 100) {
    //   if (!ParticleEffects.containsKey(playerUUID)) {
    //     ParticleEffect effect = new ParticleEffect(player, this);
    //     ParticleEffects.put(playerUUID, effect.runTaskTimer(this, 0L, 5L)); // Run every second
    //   }
    // }
    // else if (stats.getParticleEffects() == 3 && stats.getDiaperWetness() >= 100 || stats.getDiaperFullness() >= 100){
    //   if (!ParticleEffects.containsKey(playerUUID)) {
    //     ParticleEffect effect = new ParticleEffect(player, this);
    //     ParticleEffects.put(playerUUID, effect.runTaskTimer(this, 0L, 5L)); // Run every second
    //   }
    // }
    // else if (stats.getParticleEffects() == 1 && stats.getDiaperWetness() >= 100) {
    //   if (!ParticleEffects.containsKey(playerUUID)) {
    //     ParticleEffect effect = new ParticleEffect(player, this);
    //     ParticleEffects.put(playerUUID, effect.runTaskTimer(this, 0L, 5L)); // Run every second
    //   }
    // }
    // else if (stats.getParticleEffects() == 0) {
    //     if (ParticleEffects.containsKey(playerUUID)) {
    //       ParticleEffects.get(playerUUID).cancel();
    //       ParticleEffects.remove(playerUUID);
    //     }
    // }
  }
  private ConcurrentHashMap<UUID, String> playerInputAwaiting = new ConcurrentHashMap<>();

  public boolean isAwaitingInput(UUID uuid) {
      return playerInputAwaiting.containsKey(uuid);
  }

  public String getAwaitingInputType(UUID uuid) {
      return playerInputAwaiting.get(uuid);
  }

  public void setAwaitingInput(UUID uuid, String type) {
      playerInputAwaiting.put(uuid, type);
  }

  public void clearAwaitingInput(UUID uuid) {
      playerInputAwaiting.remove(uuid);
  }
}
