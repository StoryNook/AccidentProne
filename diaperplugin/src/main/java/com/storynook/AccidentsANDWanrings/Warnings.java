package com.storynook.AccidentsANDWanrings;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.storynook.Plugin;
import com.storynook.PlaySounds;
import com.storynook.PlayerStats;
import com.storynook.UpdateStats;

import net.md_5.bungee.api.ChatColor;


public class Warnings {
  private static Plugin plugin;
  public Warnings(Plugin plugin) {
      this.plugin = plugin;
  }

  public static void calculateUrgeToGo(PlayerStats stats){
    double bladderUrge = stats.getBladder() * stats.getBladderIncontinence();
    double bowelUrge = stats.getBowels() * stats.getBowelIncontinence();
    double urgeToGo = Math.max(bladderUrge, bowelUrge);
    stats.setUrgeToGo((int)urgeToGo/10);
  }
  //Warning system, and accident triggers.
  public static void triggerWarnings(Player player, PlayerStats stats) {
    if (stats != null) {
      calculateUrgeToGo(stats);
        
      boolean warning = UpdateStats.playerWarningsMap.getOrDefault(player.getUniqueId(), false);
      // boolean isBladder = (stats.getBladder() * stats.getBladderIncontinence()) > 
                        //  (stats.getBowels() * stats.getBowelIncontinence());
      
      // Calculate thresholds based on minFill and incontinence
      double bladderThreshold = 100.0 / Math.max(stats.getBladderIncontinence(), 1); 
      double bowelThreshold = 100.0 / Math.max(stats.getBowelIncontinence(), 1);
      
      // Apply minFill to thresholds (lower minFill means lower required levels)
      bladderThreshold *= stats.getMinFill() / 100.0;
      bowelThreshold *= stats.getMinFill() / 100.0;
      
      // Calculate chances based on how far above threshold we are
      double bladderChance = Math.max(0, (stats.getBladder() - bladderThreshold) / bladderThreshold);
      double bowelChance = Math.max(0, (stats.getBowels() - bowelThreshold) / bowelThreshold);
      
      // Randomize between 1-100 considering the chances
      double randomValue = Math.random() * 100;
      boolean triggerBladderWarning = randomValue <= bladderChance * 100 && !warning;
      boolean triggerBowelWarning = randomValue <= bowelChance * 100 && !warning;
      Bukkit.getLogger().info("Random is: " + (int)randomValue + " Bladder is: " + (int)(bladderChance* 100));
      Bukkit.getLogger().info("Random is: " + (int)randomValue + " Bowel is: " + (int)(bowelChance* 100));

      if (triggerBladderWarning) {
          handleWarning(player, stats, triggerBladderWarning);
      }
      if (triggerBowelWarning) {
        handleWarning(player, stats, triggerBowelWarning);
      }

      // calculateUrgeToGo(stats);
      // boolean warning = UpdateStats.playerWarningsMap.getOrDefault(player.getUniqueId(), false);
      // boolean isBladder = (stats.getBladder() * stats.getBladderIncontinence()) > (stats.getBowels() * stats.getBowelIncontinence());
      // double random = (Math.random() * 100) + (stats.getMinFill() - ((isBladder ? stats.getBladderIncontinence() : stats.getBowelIncontinence()) * (stats.getMinFill()/10)));
      // Bukkit.getLogger().info("Random is: " + (int)random + " urge to go is: " + (int)stats.getUrgeToGo());
      // if (Math.min((int)random, 100) <= ((int)stats.getUrgeToGo()) && warning == false) {
      //   handleWarning(player, stats, isBladder);
      // }
    }
  }
          
  private static void handleWarning(Player player, PlayerStats stats, boolean isBladder) {
    double fullness = isBladder ? stats.getBladder() : stats.getBowels();
    double incontinenceLevel = isBladder ? stats.getBladderIncontinence() : stats.getBowelIncontinence();
    double minFullnessForAccident = 100 - (8 * (incontinenceLevel - 1));
    if (minFullnessForAccident < fullness) {
      double accidentProbability;
      if (incontinenceLevel >= 10.0) {
        accidentProbability = 0.95;
      } else if (incontinenceLevel >= 9.0) {
          accidentProbability = 0.80;
      } else if (incontinenceLevel >= 8.0) {
          accidentProbability = 0.70;
      } else if (incontinenceLevel >= 7.0) {
          accidentProbability = 0.60;
      } else if (incontinenceLevel >= 6.0) {
          accidentProbability = 0.50;
      } else if (incontinenceLevel >= 5.0) {
          accidentProbability = 0.40;
      } else if (incontinenceLevel >= 4.0) {
          accidentProbability = 0.25;
      } else if (incontinenceLevel >= 3.0) {
          accidentProbability = 0.20;
      } else if (incontinenceLevel >= 2.0) {
          accidentProbability = 0.15;
      } else if (incontinenceLevel >= 1.0) {
          accidentProbability = 0.10;
      } else {
          accidentProbability = 0.0;
      }

      accidentProbability *= fullness / 100;
      accidentProbability = Math.min(accidentProbability, 1.0);
      double random = Math.random();

      if (random < accidentProbability) {
        player.sendMessage(ChatColor.RED +"You couldn't hold it anymore.");
        HandleAccident.handleAccident(isBladder, player, true, true);
        return;
      } 
    }
    if ((fullness/10) >= ((Math.random() * 8) + 1)) {
      if (!isBladder) {
        PlaySounds.playsounds(player,"tummyrumble", 5,1.0,0.2, false);
      }
      if(stats.getUrgeToGo() > 50 && stats.getUrgeToGo() < 75){
        player.sendMessage(ChatColor.GOLD + (isBladder ? "You REALLY need to pee!" : "You REALLY need to Poop!"));
        player.sendMessage(ChatColor.GOLD + "Hold sneak to hold it in! You only have 5 seconds!");
        UpdateStats.playerWarningsMap.put(player.getUniqueId(), true);
      }else if(stats.getUrgeToGo() > 75){
        player.sendMessage(ChatColor.RED + "You can't hold it much longer!");
        UpdateStats.playerWarningsMap.put(player.getUniqueId(), true);
      }else{
        player.sendMessage(ChatColor.GOLD + (isBladder ? "You need to pee!" : "You need to Poop!"));
        player.sendMessage(ChatColor.GOLD + "Hold sneak to hold it in! You only have 5 seconds!");
        UpdateStats.playerWarningsMap.put(player.getUniqueId(), true);
      }
    }
  }

  public static void sneakCheck(Player player, PlayerStats stats, double need, double incontinenceLevel, boolean isBladder) {
    double randomChance = (Math.random() * 10) + 1;
    boolean sneakFails = randomChance <= incontinenceLevel;
    int secondsleft = UpdateStats.playerSecondsLeftMap.get(player.getUniqueId());
    
    if ((!player.isSneaking() || sneakFails) && (isBladder ? stats.getBladder() > 10 : stats.getBowels() > 10) && secondsleft > 3) { 
      if (sneakFails && player.isSneaking()) {
        player.sendMessage(ChatColor.RED + "Your body has betrayed you. You couldn't hold it.");
        HandleAccident.handleAccident(isBladder, player, true, true);
      }
      else {HandleAccident.handleAccident(isBladder, player, true, false);}
      UpdateStats.playerSecondsLeftMap.put(player.getUniqueId(), 0);
      UpdateStats.playerWarningsMap.put(player.getUniqueId(), false);
    } else {
      if((isBladder ? stats.getBladder() > 10 : stats.getBowels() > 10) && player.isSneaking() && secondsleft <= 3){
        player.sendMessage(ChatColor.GREEN + "Good job! You held it in.");
        UpdateStats.playerSecondsLeftMap.put(player.getUniqueId(), 0);
        UpdateStats.playerWarningsMap.put(player.getUniqueId(), false);
      }
    }
  }
}
