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

  public static int calculateUrgeToGo(PlayerStats stats){
    double bladderUrge = stats.getBladder() + (stats.getBladderIncontinence() * (stats.getBladderIncontinence()-1));
    double bowelUrge = stats.getBowels() + (stats.getBowelIncontinence() * (stats.getBowelIncontinence()-1));
    double urgeToGo = Math.max(bladderUrge, bowelUrge);
    return (int)urgeToGo;
  }
  //Warning system, and accident triggers.
  public static void triggerWarnings(Player player, PlayerStats stats) {
    if (stats != null) {

      int urge = calculateUrgeToGo(stats);
      boolean warning = UpdateStats.playerWarningsMap.getOrDefault(player.getUniqueId(), false);

      double randombladder= Math.random() * 10;
      randombladder += Math.max(0,(stats.getMinFill() - (stats.getBladderIncontinence()) * stats.getBladderIncontinence()));
      double randombowel= Math.random() * 10;
      randombowel += Math.max(0, (stats.getMinFill() - (stats.getBowelIncontinence()) * stats.getBowelIncontinence()));
      boolean triggerBladderWarning = false;
      boolean triggerBowelWarning = false;
      if (!warning) {
        if (randombladder  <= (urge + stats.getUrgeToGo())) {
          triggerBladderWarning = true;
        }
        if (randombowel <= (urge + stats.getUrgeToGo())) {
          triggerBowelWarning = true;
        }
      }
      if (triggerBladderWarning) {
          handleWarning(player, stats, true);
      }
      if (triggerBowelWarning) {
        handleWarning(player, stats, false);
      }
    }
  }
          
  private static void handleWarning(Player player, PlayerStats stats, boolean isBladder) {
    double fullness = isBladder ? stats.getBladder() : stats.getBowels();
    fullness += stats.getUrgeToGo();
    double incontinenceLevel = isBladder ? stats.getBladderIncontinence() : stats.getBowelIncontinence();
    double minFullnessForAccident = 100 - (8 * (incontinenceLevel - 1));
    if (minFullnessForAccident <= fullness) {
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
      } else if (incontinenceLevel >= 0.0) {
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
      return; 
    }
    else if ((fullness/10) >= ((Math.random() * 8) + 1)) {
      if (!isBladder) {
        PlaySounds.playsounds(player,"tummyrumble", 5,1.0,0.2, false);
      }
      if(stats.getUrgeToGo() > 5 && stats.getUrgeToGo() < 7){
        player.sendMessage(ChatColor.GOLD + (isBladder ? "You REALLY need to pee!" : "You REALLY need to Poop!"));
        player.sendMessage(ChatColor.GOLD + "Hold sneak to hold it in! You only have 5 seconds!");
        UpdateStats.playerWarningsMap.put(player.getUniqueId(), true);
      }else if(stats.getUrgeToGo() > 7){
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
        stats.increaseUrgeToGo(1);
        player.sendMessage(ChatColor.GREEN + "Good job! You held it in.");
        UpdateStats.playerSecondsLeftMap.put(player.getUniqueId(), 0);
        UpdateStats.playerWarningsMap.put(player.getUniqueId(), false);
      }
    }
  }
}
