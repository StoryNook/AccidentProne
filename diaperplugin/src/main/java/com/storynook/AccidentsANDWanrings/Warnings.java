package com.storynook.AccidentsANDWanrings;

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
    stats.setUrgeToGo((int)urgeToGo);
  }
  //Warning system, and accident triggers.
  public static void triggerWarnings(Player player, PlayerStats stats) {
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
          
  private static void handleWarning(Player player, PlayerStats stats, boolean isBladder) {
    double fullness = isBladder ? stats.getBladder() : stats.getBowels();
    double incontinenceLevel = isBladder ? stats.getBladderIncontinence() : stats.getBowelIncontinence();
    double minFullnessForAccident = 100 - (8 * (incontinenceLevel - 1));
    if (minFullnessForAccident < fullness) {
      double accidentProbability;
      switch ((int) incontinenceLevel) {
          case 1: accidentProbability = 0.10; break; // 10%
          case 2: accidentProbability = 0.15; break; // 15%
          case 3: accidentProbability = 0.20; break; // 20%
          case 4: accidentProbability = 0.25; break; // 25%
          case 5: accidentProbability = 0.40; break; // 40%
          case 6: accidentProbability = 0.50; break; // 50%
          case 7: accidentProbability = 0.60; break; // 60%
          case 8: accidentProbability = 0.70; break; // 70%
          case 9: accidentProbability = 0.80; break; // 80%
          case 10: accidentProbability = 0.95; break; // 95%
          default: accidentProbability = 0.0; break;
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
