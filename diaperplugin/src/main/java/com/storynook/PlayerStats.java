package com.storynook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.storynook.Event_Listeners.PantsCrafting;

public class PlayerStats {
    private UUID playerUUID;
    private double bladder = 0;
    private double bowels = 0;
    private double diaperWetness = 0;
    private double diaperFullness = 0;
    private double bladderIncontinence = 1; // 1-10
    private double bowelIncontinence = 1; // 1-10
    private double bladderFillRate = 0.2; // Default fill rate
    private double bowelFillRate = 0.07; // Default fill rate
    private double hydration = 100; // Starts fully hydrated
    private int urgeToGo = 1;
    private int UnderwearType = 0;
    private int layers = 0;
    private int ParticleEffects;
    private int UI;
    private int Bedwetting;
    private int EffectDuration = 0;
    private int timeworn = 0;
    private int minFill = 30;
    private boolean optin, messing, hardcore, BladderLockIncon, BowelLockIncon, AllCaregiver, specialCG, visableUnderwear, fillbar, showfill, showunderwear, canhear, lethear;
    private long hardcoreEnabledTime = -1;
    private Map<String, Map<String, Boolean>> StoredSounds = new HashMap<>();
    // private List<String> blockedsounds = new ArrayList<>();
    private List<UUID> caregivers = new ArrayList<>();

    private static final int MAX_VALUE = 100;

    public PlayerStats(UUID playerUUID, Plugin plugin) {
        this.playerUUID = playerUUID;
        this.plugin = plugin;
    }
    private Plugin plugin;

    //Sound Settings

    // public void printAllStoredSounds() {
    //     System.out.println("Current StoredSounds contents:");
    //     for (Map.Entry<String, Map<String, Boolean>> categoryEntry : StoredSounds.entrySet()) {
    //         String categoryName = categoryEntry.getKey();
    //         Map<String, Boolean> sounds = categoryEntry.getValue();
    //         System.out.println("Category: " + categoryName);
    //         for (Map.Entry<String, Boolean> soundEntry : sounds.entrySet()) {
    //             String soundName = soundEntry.getKey();
    //             boolean isEnabled = soundEntry.getValue();
    //             System.out.println("- Sound: " + soundName + " | Enabled: " + isEnabled);
    //         }
    //     }
    // }
    public Map<String, Map<String, Boolean>> getStoredSounds() {
        return StoredSounds;
    }
    public void setStoredSounds(Map<String, Map<String, Boolean>> sounds) {
        this.StoredSounds = sounds;
    }
    
    public void toggleSound(String categoryName, String soundName) {
        Map<String, Boolean> sounds = StoredSounds.get(categoryName);
        if (sounds != null && sounds.containsKey(soundName)) {
            boolean currentStatus = sounds.get(soundName);
            sounds.put(soundName, !currentStatus);  // Toggle the status
        }
    }
    public String getRandomSoundFromCategory(String categoryName) {
        Map<String, Boolean> sounds = StoredSounds.get(categoryName);
        if (sounds == null || sounds.isEmpty()) {
            return null;  // No sounds in this category
        }
        
        List<String> enabledSounds = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : sounds.entrySet()) {
            if (entry.getValue()) {  // Only add enabled sounds
                enabledSounds.add(entry.getKey());
            }
        }
        
        if (enabledSounds.isEmpty()) {
            return null;  // No enabled sounds in this category
        }
        
        Random random = new Random();
        int index = random.nextInt(enabledSounds.size());
        return enabledSounds.get(index);
    }

    // Caregiver Settings
    public List<UUID> getCaregivers() {return caregivers;}
    public void addCaregiver(UUID caregiverUUID) { if(!caregivers.contains(caregiverUUID)){caregivers.add(caregiverUUID);}}
    public void removeCaregiver(UUID caregiverUUID) { caregivers.remove(caregiverUUID);}
    public boolean isCaregiver(UUID uuid, Boolean Specified) {
        PlayerStats triggerStats = plugin.getPlayerStats(uuid);
        if (caregivers.contains(uuid)) {
            return true;
        } else if (Specified && AllCaregiver && triggerStats.getspecialCG()){
            return true;
        }
        else {return false;}
    }

    //Quick Boolean Settings
    public boolean getlethear() {return lethear;}
    public void setlethear(boolean bool) {lethear = bool;}

    public boolean getcanhear() {return canhear;}
    public void setcanhear(boolean bool) {canhear = bool;}

    public boolean getOptin() {return optin;}
    public void setOptin(boolean bool) {optin = bool;}

    public boolean getshowfill() {return showfill;}
    public void setshowfill(boolean bool) {showfill = bool;}

    public boolean getshowunderwear() {return showunderwear;}
    public void setshowunderwear(boolean bool) {showunderwear = bool;}

    public boolean getfillbar() {return fillbar;}
    public void setfillbar(boolean bool) {fillbar = bool;}

    public boolean getvisableUnderwear() {return visableUnderwear;}
    public void setvisableUnderwear(boolean bool) {visableUnderwear = bool;}

    public int getParticleEffects() {return ParticleEffects;}
    public void setParticleEffects(int number) {ParticleEffects = number;}

    public boolean getspecialCG() {return specialCG;}
    public void setspecialCG(boolean bool) {specialCG = bool;}

    public boolean getAllCaregiver() {return AllCaregiver;}
    public void setAllCaregiver(boolean bool) {AllCaregiver = bool;}

    public boolean getBladderLockIncon() {return BladderLockIncon;}
    public void setBladderLockIncon(boolean bool) {BladderLockIncon = bool;}

    public boolean getBowelLockIncon() {return BowelLockIncon;}
    public void setBowelLockIncon(boolean bool) {BowelLockIncon = bool;}

    public boolean getMessing() {return messing;}
    public void setMessing(boolean bool) {messing = bool;}

    public boolean getHardcore() {return hardcore;}
    public void setHardcore(boolean bool) {hardcore = bool;}

    //Settings Menu Options
    public int getUI() {return UI;}
    public void setUI(int number) {UI = number;}

    public int getBedwetting() {return Bedwetting;}
    public void setBedwetting(int number) {Bedwetting = number;}

    //Hardcore timer
    public long getHardcoreEnabledTime() {return hardcoreEnabledTime;}
    public void setHardcoreEnabledTime(long time) {this.hardcoreEnabledTime = time;}

    //Bladder, Bowels, Diaper Fill, etc.
    public double getBladder() { return bladder; }
    public void setBladder(double amount) { bladder = Math.max(0, amount); }
    public void increaseBladder(double amount) { bladder = Math.min(bladder + amount, MAX_VALUE); }

    public double getBowels() { return bowels; }
    public void setBowels(double amount) { bowels = Math.max(0, amount); }
    public void increaseBowels(double amount) { bowels = Math.min(bowels + amount, MAX_VALUE); }

    public double getDiaperWetness() { return diaperWetness; }
    public void setDiaperWetness(double amount) { diaperWetness = Math.max(0, amount); }
    public void increaseDiaperWetness(double amount) { diaperWetness = diaperWetness + amount; }

    public double getDiaperFullness() { return diaperFullness; }
    public void setDiaperFullness(double amount) { diaperFullness = Math.max(0, amount); } 
    public void increaseDiaperFullness(double amount) { diaperFullness = diaperFullness + amount;}

    public double getBladderIncontinence() { return bladderIncontinence; }
    public void increaseBladderIncontinence(double amount) { bladderIncontinence = Math.min(bladderIncontinence + amount, 10); }
    public void decreaseBladderIncontinence(double amount) { bladderIncontinence = Math.max(bladderIncontinence - amount, 1); }
    public void setBladderIncontinence(double amount) { bladderIncontinence = Math.max(1, Math.min(10, amount)); }

    public double getBowelIncontinence() { return bowelIncontinence; }
    public void increaseBowelIncontinence(double amount) { bowelIncontinence = Math.min(bowelIncontinence + amount, 10); }
    public void decreaseBowelIncontinence(double amount) { bowelIncontinence = Math.max(bladderIncontinence - amount, 1); }
    public void setBowelIncontinence(double amount) { bowelIncontinence = Math.max(1, Math.min(10, amount)); }

    public int getEffectDuration() { return EffectDuration; }
    public void increaseEffectDuration(int amount) { EffectDuration = Math.min(EffectDuration + amount, 1000); }
    public void decreaseEffectDuration(int amount) { EffectDuration = Math.max(EffectDuration - amount, 0); }
    public void setEffectDuration(int amount) { EffectDuration = Math.max(0, amount); }
    
    public int getTimeWorn() { return timeworn; }
    public void increaseTimeWorn(int amount) { timeworn = Math.max(timeworn + amount, 0); }
    public void setTimeWorn(int amount) { timeworn = Math.max(0, amount); }
    
    public double getBladderFillRate() { return bladderFillRate; }
    public void setBladderFillRate(double rate) { bladderFillRate = Math.max(rate, 0.001); }

    public double getBowelFillRate() { return bowelFillRate; }
    public void setBowelFillRate(double rate) { bowelFillRate = Math.max(rate, 0.001); }

    public double getMinFill() { return minFill; }
    public void setMinFill(int threshold) { minFill = Math.max(0, Math.min(100, threshold)); }

    public double getHydration() { return hydration; }
    public void setHydration(double amount) { hydration = amount; }
    public void increaseHydration(double amount) { hydration = hydration + amount; }
    public void decreaseHydration(double amount) { hydration = Math.max(hydration - amount, 0); }

    public int getUrgeToGo() { return urgeToGo; }
    public void setUrgeToGo(int amount) { urgeToGo = Math.min(amount, 100);}
    public void increaseUrgeToGo(int amount) { urgeToGo = Math.min(urgeToGo + amount, 100); }
    
    public int getUnderwearType() { return UnderwearType; }
    public void setUnderwearType(int type) { UnderwearType = Math.max(0, Math.min(type, 3)); }

    public int getLayers() { return layers; }
    public void setLayers(int type) { layers = type; }
    public void increaseLayers(int type) { layers = Math.min(layers + type, 4); }
    public void decreaseLayers(int type) { layers = Math.max(layers - type, 0);}


    // Method to handle an accident
    // public void handleAccident(boolean isBladder, Player player, Boolean PlaySound, Boolean MessageSent) {
    //     if (isBladder) {
    //         double wetbyamount;
    //         switch (layers) {
    //             case 0: wetbyamount = bladder; break;
    //             case 1: wetbyamount = bladder/1.5; break;
    //             case 2: wetbyamount = bladder/2; break;
    //             case 3: wetbyamount = bladder/3; break;
    //             case 4: wetbyamount = bladder/4; break;
    //             default: wetbyamount = bladder; break;
    //         }
    //         switch (UnderwearType) {
    //             case 0:
    //                 if (layers > 0) {if(diaperWetness >= 100) {diaperWetness += 100;} else {diaperWetness += 50;}}
    //                 else {diaperWetness += 100;}
    //                 break;
    //             case 1: diaperWetness += wetbyamount; break;
    //             case 2: diaperWetness += wetbyamount/2; break;
    //             case 3: diaperWetness += wetbyamount/4; break;
    //             default: break;
    //         }
    //         bladder = 0;
    //         if (!getBladderLockIncon()) {increaseBladderIncontinence(0.5);}
    //         urgeToGo = 0;
    //     } else {
    //         if (UnderwearType == 0) {diaperFullness += 100;}
    //         else if (UnderwearType == 1) {diaperFullness = diaperFullness + bowels;}
    //         else if (UnderwearType == 2) {diaperFullness = diaperFullness + bowels/2;}
    //         else if (UnderwearType == 3) {diaperFullness = diaperFullness + bowels/4;}
    //         bowels = 0;
    //         if(!getBowelLockIncon()){increaseBowelIncontinence(0.5);}
    //         urgeToGo = 0;
    //     }
    //     if (PlaySound) {
    //         World world = player.getLocation().getWorld();
    //         Collection<Entity> nearbyEntities = world.getNearbyEntities(player.getLocation(), 5.0, 5.0, 5.0);

    //         Boolean shouldBroadcast = getlethear();
    //         List<Player> affectedPlayers = new ArrayList<>();

    //         for (Entity entity : nearbyEntities) {
    //             if (entity instanceof Player) {
    //                 Player targetPlayer = (Player) entity;
    //                 if (targetPlayer != null && targetPlayer.getLocation() != null) {
    //                     if (shouldBroadcast) {
    //                         if (targetPlayer.getcanhear) {
                                
    //                         }
    //                     }
    //                     double distance = targetPlayer.getLocation().distance(player.getLocation());

    //                     // Calculate volume based on distance, with max at 1.0f and min at 0.2f
    //                     float maxVolume = 1.0f;
    //                     float minVolume = 0.2f;  // Minimum volume to still hear the sound
    //                     float volume = (float) ((5 - distance) / 5 * (maxVolume - minVolume)) + minVolume;
    
    //                     targetPlayer.playSound(targetPlayer.getLocation(), 
    //                                             "minecraft:pee1",
    //                                             SoundCategory.PLAYERS, 
    //                                             volume,
    //                                             1.0f);
                        
    //                 }
    //             }
    //         }
    //     }
    //     if (!MessageSent) {
    //         if (isBladder ? getBladderIncontinence() == 10 && !BladderLockIncon : getBowelIncontinence() == 10 && !BowelLockIncon) {
    //             player.sendMessage("Oh no! Someone has no control!");
    //         }
    //         else if (isBladder ? getBladderIncontinence() > 7 : getBowelIncontinence() > 7) {
    //             if (isBladder ? BladderLockIncon : BowelLockIncon) {
    //                 player.sendMessage("Some one likes using their pants, huh?");
    //             }
    //             else{
    //                 player.sendMessage("Seems like someone is losing control");
    //             }
    //         }
    //         else if (isBladder ? getBladderIncontinence() > 3 : getBowelIncontinence() > 3) {
    //             player.sendMessage("You should try to make it to the potty next time.");
    //         }else {
    //             player.sendMessage("Oh no! You had an accident...");
    //         }
    //     }
        
    //     if (diaperWetness >= 100 && diaperFullness >= 100) {
    //         changeLeggingsModel(player, 626018);
    //     }
    //     else if (diaperWetness >= 100 && diaperFullness < 100) {
    //         changeLeggingsModel(player, 626016);
    //     }
    //     else if(diaperFullness >= 100 && diaperWetness < 100){
    //         changeLeggingsModel(player, 626017);
    //     }
    //     if (visableUnderwear) {
    //         PantsCrafting.equipDiaperArmor(player, false, true);
    //     }
    //     // plugin.manageParticleEffects(player);
    // }

    // private void changeLeggingsModel(Player player, int modelData) {
    //     ItemStack leggings = player.getInventory().getLeggings();
    //     if (leggings != null && leggings.getType() == Material.LEATHER_LEGGINGS) {
    //         LeatherArmorMeta meta = (LeatherArmorMeta) leggings.getItemMeta();
    //         if (meta.getCustomModelData() == 626015 || meta.getCustomModelData() == 626016 || meta.getCustomModelData() == 626017) {
    //             meta.setCustomModelData(modelData);
    //             leggings.setItemMeta(meta);
    //             return;
    //         }
    //     }
    // }
    public void unlockIncon(String type) {
        switch (type.toLowerCase()) {
            case "bladder":
                setBladderLockIncon(false);
                break;
            case "bowel":
                setBowelLockIncon(false);
                break;
            case "both":
                setBladderLockIncon(false);
                setBowelLockIncon(false);
                break;
            default:
                System.out.println("Invalid incontinence type specified.");
        }
    }
}

