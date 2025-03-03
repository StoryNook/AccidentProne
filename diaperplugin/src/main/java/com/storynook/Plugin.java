package com.storynook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.ScoreboardManager;

import com.storynook.AccidentsANDWanrings.HandleAccident;
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
import com.storynook.menus.SoundEffectsMenu;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Plugin extends JavaPlugin
{
  private HashMap<UUID, PlayerStats> playerStatsMap = new HashMap<>();
  private final Map<UUID, ArmorStand> armorStandTracker = new HashMap<>();
  public HashMap<UUID, Integer> rightclickCount = new HashMap<>();
  HashMap<UUID, Boolean> wasJumping = new HashMap<>();
  HashMap<UUID, Boolean> wasSprinting = new HashMap<>();
  public HashMap<UUID, Boolean> firstimeran = new HashMap<>();
  private Map<UUID, BukkitTask> ParticleEffects = new HashMap<>();
  public static Map<String, Map<String, Boolean>> soundConfig = new HashMap<>();


  public Map<UUID, ArmorStand> getArmorStandTracker() {
      return armorStandTracker;
  }
  private CommandHandler commandHandler = new CommandHandler(this);
  private HandleAccident handleAccident = new HandleAccident(this);
  private PlaySounds playsounds = new PlaySounds(this);
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
    //Menus
    SettingsMenu SettingsMenu = new SettingsMenu(this);
    Caregivermenu caregivermenu = new Caregivermenu(this);
    HUDMenu hudmenu = new HUDMenu(this);
    IncontinenceMenu incontinencemenu = new IncontinenceMenu(this);
    SoundEffectsMenu soundmenu = new SoundEffectsMenu(this);

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
    itemManager.createlaxedItem();
  
   
    // Create an array of all your listener objects
    Object[] listeners = new Object[]{playerEventListener, SettingsMenu, caregivermenu, incontinencemenu, hudmenu, soundmenu, pantsCrafting, washer, sit, toilet, accident, lax, change};

    // Loop through and register each listener
    for (Object listener : listeners) {
        if (listener instanceof Listener) {
            getServer().getPluginManager().registerEvents((Listener) listener, this);
        }
    }
    loadSounds();
    UpdateStats();

    playerStatsMap = new HashMap<UUID, PlayerStats>();

    //Registers the commands


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
            // If file exists, merge new values, overwriting existing ones
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
                // This will overwrite existing entries with the new values
                for (String key : newConfig.getKeys(true)) {
                    existingConfig.set(key, newConfig.get(key));
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
        getLogger().severe("Error handling " + fileName + ": " + e.getLocalizedMessage());
        e.printStackTrace();
    }
  }

  // private void mergeConfigFiles(String fileName) {
  //   File dataFile = new File(getDataFolder(), fileName);

  //   try {
  //       if (!dataFile.exists()) {
  //           // Create file from resources
  //           InputStream resourceStream = this.getClass().getClassLoader()
  //                   .getResourceAsStream(fileName);

  //           if (resourceStream == null) {
  //               getLogger().severe("Could not find " + fileName + " in resources!");
  //               return;
  //           }

  //           try (OutputStream fileOutputStream = new FileOutputStream(dataFile)) {
  //               byte[] buffer = new byte[1024];
  //               int bytesRead;
  //               while ((bytesRead = resourceStream.read(buffer)) != -1) {
  //                   fileOutputStream.write(buffer, 0, bytesRead);
  //               }
  //               getLogger().info("Created " + fileName);
  //           } finally {
  //               if (resourceStream != null) {
  //                   resourceStream.close();
  //               }
  //           }
  //       } else {
  //           // If file exists, merge new values without overwriting existing ones
  //           YamlConfiguration existingConfig = YamlConfiguration.loadConfiguration(dataFile);

  //           InputStream resourceStream = this.getClass().getClassLoader()
  //                   .getResourceAsStream(fileName);

  //           if (resourceStream == null) {
  //               getLogger().severe("Could not find " + fileName + " in resources!");
  //               return;
  //           }

  //           try {
  //               // Read the resource stream into a temporary file
  //               File tempFile = new File(getDataFolder(), "temp_" + fileName);
  //               try (OutputStream tempOutputStream = new FileOutputStream(tempFile)) {
  //                   byte[] buffer = new byte[1024];
  //                   int bytesRead;
  //                   while ((bytesRead = resourceStream.read(buffer)) != -1) {
  //                       tempOutputStream.write(buffer, 0, bytesRead);
  //                   }
  //               }

  //               YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(tempFile);

  //               // Merge new configuration into existing one
  //               // This will add missing entries but keep existing values
  //               for (String key : newConfig.getKeys(true)) {
  //                   if (!existingConfig.contains(key)) {
  //                       existingConfig.set(key, newConfig.get(key));
  //                   }
  //               }

  //               // Save merged configuration back to file
  //               existingConfig.save(dataFile);
  //               getLogger().info("Updated " + fileName);

  //               // Clean up temporary file
  //               tempFile.delete();
  //           } finally {
  //               resourceStream.close();
  //           }
  //       }
  //   } catch (IOException e) {
  //       getLogger().severe("Error handling " + fileName + ": " + e.getMessage());
  //       e.printStackTrace();
  //   }
  // }

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

  private void loadSounds() {
    try {
        File soundsFile = new File(getDataFolder(), "sounds.yml");

        // Create default file if it doesn't exist
        if (!soundsFile.exists()) {
            saveResource("sounds. yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(soundsFile);

        // Load each category and its sounds into the hashmap
        for (String category : config.getKeys(false)) {
            Map<String, Boolean> categoryMap = new HashMap<>();

            List<String> sounds = config.getStringList(category);
            if (!sounds.isEmpty()) {
                for (String sound : sounds) {
                    categoryMap.put(sound, true);
                    getLogger().info("Loaded sound: " + sound + " under category: " + category);
                }
            } else {
                getLogger().warning("No sounds found in category: " + category);
            }

            // Put the category map into the main map
            soundConfig.put(category, categoryMap);
        }

        if (soundConfig.isEmpty()) {
            getLogger().severe("No sounds loaded. Please check your sounds.yml file.");
        } else {
            getLogger().info("Successfully loaded " + soundConfig.size() + " categories of sounds.");
            for (Map.Entry<String, Map<String, Boolean>> entry : soundConfig.entrySet()) {
                String category = entry.getKey();
                int soundCount = entry.getValue().size();
                getLogger().info("Category: " + category + ", Sounds loaded: " + soundCount);
            }
        }

    } catch (Exception e) {
        getLogger().severe("Error loading sounds.yml: " + e.getMessage());
        e.printStackTrace();
    }
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
      stats.setLaxEffectDuration((int) config.getInt("LaxEffectDuration", 0));
      stats.setLaxEffectDelay((int) config.getInt("LaxEffectDelay", 0));
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
      stats.setcanhear(config.getInt("CanHear", 0));
      stats.setlethear(config.getInt("LetHear", 0));
      if (config.contains("Caregivers")) {
        for (String uuid : config.getStringList("Caregivers")){
          stats.addCaregiver(UUID.fromString(uuid));
        }
      }
      if (config.contains("StoredSounds")) {
        ConfigurationSection storedSounds = config.getConfigurationSection("StoredSounds");

        // Create a map to hold the sounds configuration for the player
        Map<String, Map<String, Boolean>> soundsMap = new HashMap<>();

        for (String category : soundConfig.keySet()) {
            Map<String, Boolean> soundMap = soundConfig.get(category);
            if (!storedSounds.contains(category)) {
                storedSounds.createSection(category);
            }
            ConfigurationSection categorySection = storedSounds.getConfigurationSection(category);

            // Populate the map with Boolean values
            Map<String, Boolean> categorySounds = new HashMap<>();
            for (Map.Entry<String, Boolean> entry : soundMap.entrySet()) {
                String soundName = entry.getKey();
                Boolean defaultValueStr = entry.getValue();

                if (!categorySection.contains(soundName)) {
                    // Set the default value in the config
                    categorySection.set(soundName, defaultValueStr);
                    // Add to the map with Boolean
                    categorySounds.put(soundName, defaultValueStr);
                } else {
                    // Use the existing value from the config and parse it as a Boolean
                    Object existingValue = categorySection.get(soundName);
                    if (existingValue instanceof String) {
                        String strVal = (String) existingValue;
                        if (strVal.equalsIgnoreCase("true")) {
                            categorySounds.put(soundName, true);
                        } else if (strVal.equalsIgnoreCase("false")) {
                            categorySounds.put(soundName, false);
                        } else {
                            categorySounds.put(soundName, Boolean.parseBoolean(strVal));
                        }
                    } else if (existingValue instanceof Boolean) {
                        categorySounds.put(soundName, (Boolean) existingValue);
                    } else {
                        // Handle unexpected types (e.g., numbers)
                        try {
                            int intValue = Integer.parseInt(existingValue.toString());
                            categorySounds.put(soundName, intValue > 0);
                        } catch (NumberFormatException e) {
                            categorySounds.put(soundName, false);
                        }
                    }
                }
            }
            soundsMap.put(category, categorySounds);
        }

        // Set the populated sounds into the player's stats
        stats.setStoredSounds(soundsMap);
      }
      else {
        // Create StoredSounds section with all sounds from soundConfig
        ConfigurationSection storedSounds = config.createSection("StoredSounds");
        Map<String, Map<String, Boolean>> soundsMap = new HashMap<>();

        for (String category : soundConfig.keySet()) {
            Map<String, Boolean> soundMap = soundConfig.get(category);
            ConfigurationSection categorySection = storedSounds.createSection(category);

            // Populate the map with Boolean values
            Map<String, Boolean> categorySounds = new HashMap<>();
            for (Map.Entry<String, Boolean> entry : soundMap.entrySet()) {
                String soundName = entry.getKey();
                Boolean defaultValueStr = entry.getValue();

                categorySection.set(soundName, defaultValueStr);
                categorySounds.put(soundName, defaultValueStr);
            }
            soundsMap.put(category, categorySounds);
        }

        // Set the populated sounds into the player's stats
        stats.setStoredSounds(soundsMap);
        
        // try {
        //     config.save(playerFile);
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
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
      stats.setLaxEffectDuration(0);
      stats.setLaxEffectDelay(0);
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
      stats.setStoredSounds(soundConfig);
      stats.setlethear(0);
      stats.setcanhear(0);
      
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
      config.set("LaxEffectDuration", stats.getLaxEffectDuration());
      config.set("LaxEffectDelay", stats.getLaxEffectDelay());
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
      config.set("CanHear", stats.getcanhear());
      config.set("LetHear", stats.getlethear());
      List<String> uuidStrings = stats.getCaregivers().stream()
      .map(UUID::toString) // Convert UUID to string
      .collect(Collectors.toList());
      config.set("Caregivers", uuidStrings);

      ConfigurationSection storedSoundsConfig = config.createSection("StoredSounds");

      // Iterate through all categories in StoredSounds
      for (Map.Entry<String, Map<String, Boolean>> categoryEntry : stats.getStoredSounds().entrySet()) {
          String categoryName = categoryEntry.getKey();
          ConfigurationSection categoryConfig = storedSoundsConfig.createSection(categoryName);
          
          // Get the sounds map for the current category
          Map<String, Boolean> sounds = categoryEntry.getValue();
          
          // Add each sound to the category configuration
          for (Map.Entry<String, Boolean> soundEntry : sounds.entrySet()) {
              String soundName = soundEntry.getKey();
              boolean isEnabled = soundEntry.getValue();
              categoryConfig.set(soundName, isEnabled);
          }
      }
      
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

  private void UpdateStats() {
    Bukkit.getScheduler().runTaskTimer(this, () -> {
      UpdateStats updateStats = new UpdateStats(this, commandHandler);
      updateStats.Update();
    }, 0L, 20L * 2);  // Run every 2 second (40 ticks)
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
