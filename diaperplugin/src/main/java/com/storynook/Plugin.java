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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
    // itemManager.createDiaperPailRecipe();
    // itemManager.createLaxRecipe();
  
   
    getServer().getPluginManager().registerEvents(playerEventListener, this);
    getServer().getPluginManager().registerEvents(customInventory, this);
    UpdateStats();

    playerStatsMap = new HashMap<UUID, PlayerStats>();

    //Registers the commands
    commandHandler = new CommandHandler(this);
    getCommand("settings").setExecutor(commandHandler);
    getCommand("stats").setExecutor(commandHandler);
    getCommand("check").setExecutor(commandHandler);
    getCommand("setunderwearlevels").setExecutor(commandHandler);
    getCommand("caregiver").setExecutor(commandHandler);
    getCommand("lockincon").setExecutor(commandHandler);
    getCommand("unlockincon").setExecutor(commandHandler);

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
      stats.setBladderFillRate((double) config.getDouble("bladderFillRate", 0.2));
      stats.setBowelFillRate((double) config.getDouble("bowelFillRate", 0.07));
      stats.setHydration((double) config.getInt("hydration", 100));
      stats.setUrgeToGo((int) config.getInt("urgeToGo", 1));
      stats.setUnderwearType((int) config.getInt("UnderwearType", 0));
      stats.setLayers(config.getInt("layers", 0));
      stats.setOptin(config.getBoolean("Optin"));
      stats.setMessing(config.getBoolean("Messing"));
      stats.setUI((int) config.getInt("UI", 1));
      stats.setEffectDuration((int) config.getInt("EffectDuration", 0));
      stats.setTimeWorn((int) config.getInt("TimeWorn", 0));
      stats.setHardcore(config.getBoolean("Hardcore"));
      stats.setBladderLockIncon(config.getBoolean("BladderLockIncon"));
      stats.setBowelLockIncon(config.getBoolean("BowelLockIncon"));
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
      stats.setEffectDuration(0);
      stats.setTimeWorn(0);
      stats.setHardcore(false);
      stats.setBladderLockIncon(false);
      stats.setBowelLockIncon(false);
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
            config.set("EffectDuration", stats.getEffectDuration());
            config.set("TimeWorn", stats.getTimeWorn());
            config.set("Hardcoare", stats.getHardcore());
            config.set("BladderLockIncon", stats.getBladderLockIncon());
            config.set("BowelLockIncon", stats.getBowelLockIncon());
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
  private void UpdateStats() {
    Bukkit.getScheduler().runTaskTimer(this, () -> {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerStats stats = getPlayerStats(player.getUniqueId());

            if (stats != null && stats.getOptin() && !(player.getVehicle() instanceof ArmorStand)) {
              stats.decreaseHydration(0.1);
              if (stats.getEffectDuration() == 0) {
                stats.setBladderFillRate(0.2);
                stats.setBowelFillRate(0.07);
              }
              stats.increaseBladder(stats.getBladderFillRate());
              if (player.getFoodLevel() > 2 && stats.getMessing()) {
                  stats.increaseBowels(stats.getBowelFillRate());
              }
              stats.decreaseEffectDuration(1);
              if (stats.getDiaperFullness() >= 100 || stats.getDiaperWetness() >= 100) {
                stats.increaseTimeWorn(1);
              }
              if (stats.getTimeWorn() >= 600 && player.getHealth() > 1) {
                  double newHealth = player.getHealth() - 0.5;
                  player.setHealth(Math.max(newHealth, 0));
              }
              if (stats.getHydration() <= 10) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 2 * 20, 2), true);
              }
              else if (stats.getHydration() >= 11) {
                player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
              }
              triggerWarnings(player, stats);
          }
        }
    }, 0L, 20L * 2);  // Run every 2 second (40 ticks)
  }
  private void calculateUrgeToGo(PlayerStats stats){
    double bladderUrge = stats.getBladder() * stats.getBladderIncontinence();
    double bowelUrge = stats.getBowels() * stats.getBowelIncontinence();
    double urgeToGo = Math.max(bladderUrge, bowelUrge);
    if(stats.getUrgeToGo() < urgeToGo){stats.setUrgeToGo((int)urgeToGo);}
  }
  //Warning system, and accident triggers.
  private int warningCounter = 0;
  private void triggerWarnings(Player player, PlayerStats stats) {
      if (stats != null) {
        calculateUrgeToGo(stats);
        Multiplier = Math.max(8, 75 - (67 * Math.min(stats.getUrgeToGo(), 100)) / 100);
        warningCounter++;
        if (warningCounter >= Multiplier) {
          warningCounter = 0;
          // getLogger().info("Handling warnings for player: " + player.getName());
          boolean isBladder = stats.getBladderIncontinence() > stats.getBowels() * stats.getBowelIncontinence() ? true : false;
          handleWarning(player, stats, isBladder);
        }
      }
  }

  private void handleWarning(Player player, PlayerStats stats, boolean isBladder) {
    double fullness = isBladder ? stats.getBladder() : stats.getBowels();
    double incontinenceLevel = isBladder ? stats.getBladderIncontinence() : stats.getBowelIncontinence();
    double randomChance = (Math.random() * (10 - 1 + 1)) + 1;
    double accidentProbability = Math.min(0.0, (incontinenceLevel * (fullness/10)));

    if (randomChance < accidentProbability) {
        player.sendMessage("Oh no! You had an accident...");
        stats.handleAccident(isBladder, player, true);
    } else if ((fullness/10) >= (Math.random() * (8 - 1 + 1) + 1)) {
        player.sendMessage(isBladder ? "You need to pee!" : "You need to Poop!");
        sneakCheck(player, stats, fullness, incontinenceLevel, isBladder);
    }
}

private void sneakCheck(Player player, PlayerStats stats, double need, double incontinenceLevel, boolean isBladder) {
    Bukkit.getScheduler().runTaskLater(this, () -> {
        double randomChance = (Math.random() * (10 - 1 + 1)) + 1;
        boolean sneakFails = randomChance <= incontinenceLevel;

        if (!player.isSneaking() || sneakFails) {
            player.sendMessage("Oh no! You had an accident...");
            stats.handleAccident(isBladder, player, true);
        } else {
            stats.increaseUrgeToGo(1);
        }
    }, 20L * 5);
}

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
                  String withBowels = "\uF82B\uF82A\uF825\uF829\uF828" + hydrationBar + "\uF82A\uF80C\uF829" + bladderBar + "\uF80B\uF809" + bowelBar + underwearImage;
                  player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(withBowels));
                }
                else{
                  String basicBar = "\uF82B\uF82A\uF825\uF829\uF828" + hydrationBar + "\uF82A\uF80C\uF829" + bladderBar + underwearImage;
                  player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(basicBar));
                }
              }
            }
            else {player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());}
        }
    }, 0L, 20L);  // Update every second (20 ticks)
  }
}
