package com.storynook;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.ScoreboardManager;
import com.storynook.items.ItemManager;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

/*
 * diaperplugin java plugin
 */
public class Plugin extends JavaPlugin
{
  private HashMap<UUID, PlayerStats> playerStatsMap = new HashMap<>();
  private final Map<UUID, ArmorStand> armorStandTracker = new HashMap<>();

  public Map<UUID, ArmorStand> getArmorStandTracker() {
      return armorStandTracker;
  }
  private CommandHandler commandHandler;
  private int Multiplier;
  private ScoreboardManager manager;
  @Override
  public void onEnable()
  {
    ItemManager.init();
    getLogger().warning("Plugin started, onEnable");
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
        getLogger().info("FIALED:Player data folder not created");
      }
    }
    PlayerEventListener playerEventListener = new PlayerEventListener(this);
    CustomInventory customInventory = new CustomInventory(this);
    //Create all the custom recipes and items related
    ItemManager itemManager = new ItemManager(this);
    itemManager.createToiletRecipe();
    itemManager.createTapeRecipe();
    itemManager.createDiaperStufferRecipe();
    itemManager.createDiaperRecipe();
    itemManager.createPullupRecipe();
    itemManager.createThickDiaperRecipe();
    itemManager.createUnderwearRecipe();
    itemManager.createDiaperPailRecipe();
    itemManager.createLaxRecipe();
  
   
    getServer().getPluginManager().registerEvents(playerEventListener, this);
    getServer().getPluginManager().registerEvents(customInventory, this);
    UpdateStats();

    playerStatsMap = new HashMap<UUID, PlayerStats>();

    //Registers the commands
    commandHandler = new CommandHandler(this);
    getCommand("settings").setExecutor(commandHandler);
    getCommand("stats").setExecutor(commandHandler);
    getCommand("check").setExecutor(commandHandler);
    // getCommand("caregiver").setExecutor(commandHandler);

    //Score board setup
    manager = Bukkit.getScoreboardManager();
    UpdateStatsBar();
  }
  @Override
  public void onDisable()
  {
    saveAllPlayerStats();
  }

  public void loadPlayerStats(Player player) {
    getLogger().warning("Player Joined " + player.getName());
    UUID playerUUID = player.getUniqueId();
    PlayerStats stats = new PlayerStats(playerUUID);
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
      stats.setBladderFillRate((double) config.getDouble("bladderFillRate", 0.268));
      stats.setBowelFillRate((double) config.getDouble("bowelFillRate", 0.1));
      stats.setHydration((int) config.getInt("hydration", 100));
      stats.setUrgeToGo((int) config.getInt("urgeToGo", 1));
      stats.setUnderwearType((int) config.getInt("UnderwearType", 0));
      stats.setLayers(config.getInt("layers", 0));
      stats.setOptin(config.getBoolean("Optin"));
      stats.setMessing(config.getBoolean("Messing"));
      stats.setScoreBoard(config.getBoolean("Scoreboard"));
      stats.setUI((int) config.getInt("UI", 1));
      // stats.setHardcore(config.getBoolean("Hardcore"));
      // if (config.contains("Caregivers")) {
      //   for (String uuid : config.getStringList("Caregivers")){
      //     stats.addCaregiver(UUID.fromString(uuid));
      //   }
      // }
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
      stats.setBladderFillRate(0.268); 
      stats.setBowelFillRate(0.1);   
      stats.setHydration(100);
      stats.setUrgeToGo(1);
      stats.setUnderwearType(0);
      stats.setLayers(0);
      stats.setOptin(false);
      stats.setMessing(false);
      // stats.setHardcore(false);
      stats.setScoreBoard(false);
      stats.setUI(1);
      
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
            config.set("UI", stats.getUI());
            // config.set("Hardcoare", stats.getHardcore());
            config.set("Scoreboard", stats.getScoreBoard());
            // List<String> uuidStrings = stats.getCaregivers().stream()
            // .map(UUID::toString) // Convert UUID to string
            // .collect(Collectors.toList());
            // config.set("Caregivers", uuidStrings);
            
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
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerStats stats = getPlayerStats(player.getUniqueId());

            if (stats != null && stats.getOptin() && !(player.getVehicle() instanceof ArmorStand)) {
              stats.decreaseHydration(1);
              stats.increaseBladder(stats.getBladderFillRate());
              if (player.getFoodLevel() > 2 && stats.getMessing()) {
                  stats.increaseBowels(stats.getBowelFillRate());
              }
              calculateUrgeToGo(stats);
              handleWarnings(player, stats);
          }
        }
    }, 0L, 20L * 2);  // Run every 2 second (40 ticks)
  }
  private void calculateUrgeToGo(PlayerStats stats){
    double bladderUrge = stats.getBladder() * stats.getBladderIncontinence();
    double bowelUrge = stats.getBowels() * stats.getBowelIncontinence();
    double urgeToGo = Math.max(bladderUrge, bowelUrge);
    stats.setUrgeToGo((int)urgeToGo);
    
  }
  //Warning system, and accident triggers.
  private static final int MAX_VALUE = 100;
  private int warningCounter = 0;
  private void handleWarnings(Player player, PlayerStats stats) {
      if (stats != null) {
          Multiplier = (stats.getUrgeToGo() >= 10) ? 8 : 75 - ((75 - 8) * stats.getUrgeToGo()) / 10;
        warningCounter++;
        if (warningCounter >= Multiplier) {
          warningCounter = 0;
          getLogger().info("Handling warnings for player: " + player.getName());
          if (stats.getBladder() * stats.getBladderIncontinence() > stats.getBowels() * stats.getBowelIncontinence()) {
            handleBladderWarning(player, stats);
          }
          else{handleBowelWarning(player, stats);}
        }
        getLogger().info("Multiplier: " + Multiplier);
      }
  }

  private void handleBladderWarning(Player player, PlayerStats stats) {
    double bladder = stats.getBladder();
    double incontinenceLevel = stats.getBladderIncontinence();
    double randomChance = Math.random();

    // Calculate accident probability based on incontinence and fullness
    double accidentProbability = calculateAccidentProbability(incontinenceLevel, bladder);

    if (randomChance < accidentProbability) {
        player.sendMessage("Oh no! You had an accident...");
        stats.handleAccident(true, player, true);
    } else if (bladder >= 0.6 * MAX_VALUE) {
        player.sendMessage("You need to pee!");
        sneakCheck(player, randomChance, stats, bladder, true);
    }
}

private void handleBowelWarning(Player player, PlayerStats stats) {
    double bowels = stats.getBowels();
    double incontinenceLevel = stats.getBowelIncontinence();
    double randomChance = Math.random();

    double accidentProbability = calculateAccidentProbability(incontinenceLevel, bowels);

    if (randomChance < accidentProbability) {
        player.sendMessage("Oh no! You had an accident...");
        stats.handleAccident(false, player, true);
    } else if (bowels >= 0.6 * MAX_VALUE) {
        player.sendMessage("You need to poop!");
        sneakCheck(player, randomChance, stats, bowels, false);
    }
}

private double calculateAccidentProbability(double incontinenceLevel, double fullness) {
    // Base probability due to fullness, scaled by proximity to MAX_VALUE
    double fullnessFactor = Math.max(0, fullness - 80) / 20.0;

    // Base probability due to incontinence level
    double incontinenceFactor = incontinenceLevel / 10.0;

    // Combine these into an overall probability
    return Math.min(1.0, incontinenceFactor + fullnessFactor);
}

private void sneakCheck(Player player, double randomChance, PlayerStats stats, double need, boolean isBladder) {
    Bukkit.getScheduler().runTaskLater(this, () -> {
        boolean sneakFails = randomChance < calculateSneakFailureProbability(stats, need, isBladder);

        if (!player.isSneaking() || sneakFails) {
            player.sendMessage("Oh no! You had an accident...");
            stats.handleAccident(isBladder, player, true);
        } else {
            stats.increaseUrgeToGo(1);
        }
    }, 60L);
}

private double calculateSneakFailureProbability(PlayerStats stats, double need, boolean isBladder) {
    double incontinenceLevel = isBladder ? stats.getBladderIncontinence() : stats.getBowelIncontinence();
    return Math.min(1.0, need / (MAX_VALUE * 0.95) + incontinenceLevel / 20.0);
}



  // private void handleBladderWarning(Player player, PlayerStats stats) {
  //   int bladder = stats.getBladder();
  //   double warningThreshold = 0.6 * (1 - stats.getBladderIncontinence() / 100.0);
  //   double accidentThreshold = 0.95 * (1 - stats.getBladderIncontinence() / 99.0);
  //   double random = Math.random();

  //   if (random < stats.getBladderIncontinence() / 100.0) {
  //       // Random accident chance based on incontinence level
  //       player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Oh no! You had an accident..."));
  //       stats.handleAccident(true);
  //   } else if (bladder >= warningThreshold * MAX_VALUE) {
  //       player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("You need to pee!"));
  //       sneakCheck(player, accidentThreshold, random, stats, bladder, true);
  //   }
  // }

  // private void handleBowelWarning(Player player, PlayerStats stats) {
  //   int bowels = stats.getBowels();
  //   double warningThreshold = 0.6 * (1 - stats.getBowelIncontinence() / 100.0);
  //   double accidentThreshold = 0.95 * (1 - stats.getBowelIncontinence() / 99.0);
  //   double random = Math.random();

  //   if (random < stats.getBowelIncontinence() / 100.0) {
  //       // Random accident chance based on incontinence level
  //       player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Oh no! You had an accident..."));
  //       stats.handleAccident(true);
  //   } else if (bowels >= warningThreshold * MAX_VALUE) {
  //       player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("You need to poop!"));
  //       sneakCheck(player, accidentThreshold, random, stats, bowels, false);
  //   }
  // }
  // private void sneakCheck(Player player, double accidentThreshold, double random, PlayerStats stats, int need, Boolean isBladder) {
  //   Bukkit.getScheduler().runTaskLater(this, () -> {
  //       if (!player.isSneaking()) {
  //           if (need >= accidentThreshold * MAX_VALUE || random < need / 150.0) {
  //               player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Oh no! You had an accident..."));
  //               stats.handleAccident(isBladder); // bowel accident
  //           }
  //       }
  //       else {stats.increaseUrgeToGo(1);}
  //   },  60L); // Check after 3 seconds (60 ticks)
  // }

  private String buildStatusBar(int value, char fullChar, char emptyChar, boolean isWater){
    StringBuilder statusBar = new StringBuilder();
    int fullCount = value / 10;
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
                ScoreBoard.updateSidebar(player, stats);
                if(player.getRemainingAir() == player.getMaximumAir()){
                  String hydrationBar = buildStatusBar(stats.getHydration(), hydrationFull, hydrationEmpty, true);
                  String statusMessage = hydrationBar;
                  player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("\uF82B\uF82A\uF825"+statusMessage));
                }
            }
            else if(stats != null && stats.getOptin() && stats.getUI() == 2){
              if(player.getRemainingAir() == player.getMaximumAir()){
                String hydrationBar = buildStatusBar(stats.getHydration(), hydrationFull, hydrationEmpty, true);
                String bladderBar = buildStatusBar((int)stats.getBladder(), bladderFull, bladderEmpty, false);
                String bowelBar = buildStatusBar((int)stats.getBowels(), bowelsFull, bowelsEmpty, false);
                String underwearImage = ScoreBoard.getUnderwearStatus((int)stats.getDiaperWetness(), (int)stats.getDiaperFullness(), (int)stats.getUnderwearType(), true);
                String statusMessage = "\uF82B\uF82A\uF825\uF829\uF828" + hydrationBar + "\uF82A\uF80C\uF829" + bladderBar + "\uF80B\uF809" + bowelBar + underwearImage;
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(statusMessage));
              }
            }
            else {
              player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard()); // Clear the scoreboard if not opted in or not displaying
          }
        }
    }, 0L, 20L);  // Update every second (20 ticks)
  }
}
