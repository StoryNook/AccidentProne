package com.storynook;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

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
    private boolean optin;
    private boolean messing;
    private int UI;
    private int Bedwetting;
    private int EffectDuration = 0;
    private boolean hardcore;
    private boolean BladderLockIncon;
    private boolean BowelLockIncon;
    private int timeworn = 0;
    private List<UUID> caregivers = new ArrayList<>();

    private static final int MAX_VALUE = 100;

    public PlayerStats(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    // Getters and setters for all fields
    public List<UUID> getCaregivers() {return caregivers;}
    public void addCaregiver(UUID caregiverUUID) { if(!caregivers.contains(caregiverUUID)){caregivers.add(caregiverUUID);}}
    public void removeCaregiver(UUID caregiverUUID) { caregivers.remove(caregiverUUID);}
    public boolean isCaregiver(UUID uuid) {
        return caregivers.contains(uuid);
    }

    public boolean getOptin() {return optin;}
    public void setOptin(boolean bool) {optin = bool;}

    public boolean getBladderLockIncon() {return BladderLockIncon;}
    public void setBladderLockIncon(boolean bool) {BladderLockIncon = bool;}

    public boolean getBowelLockIncon() {return BowelLockIncon;}
    public void setBowelLockIncon(boolean bool) {BowelLockIncon = bool;}

    public int getUI() {return UI;}
    public void setUI(int number) {UI = number;}

    public int getBedwetting() {return Bedwetting;}
    public void setBedwetting(int number) {Bedwetting = number;}
    
    public boolean getMessing() {return messing;}
    public void setMessing(boolean bool) {messing = bool;}

    public boolean getHardcore() {return hardcore;}
    public void setHardcore(boolean bool) {hardcore = bool;}

    public double getBladder() { return bladder; }
    public void setBladder(double amount) { bladder = Math.max(0, amount); }
    public void increaseBladder(double amount) { bladder = Math.min(bladder + amount, MAX_VALUE); }

    public double getBowels() { return bowels; }
    public void setBowels(double amount) { bowels = Math.max(0, amount); }
    public void increaseBowels(double amount) { bowels = Math.min(bowels + amount, MAX_VALUE); }

    public double getDiaperWetness() { return diaperWetness; }
    public void setDiaperWetness(double amount) { diaperWetness = Math.max(0, amount); }
    public void increaseDiaperWetness(double amount) { diaperWetness = Math.min(diaperWetness + amount, MAX_VALUE); }

    public double getDiaperFullness() { return diaperFullness; }
    public void setDiaperFullness(double amount) { diaperFullness = Math.max(0, amount); } 
    public void increaseDiaperFullness(double amount) { diaperFullness = Math.min(diaperFullness + amount, MAX_VALUE); }

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

    public double getHydration() { return hydration; }
    public void setHydration(double amount) { hydration = Math.min(amount, MAX_VALUE); }
    public void increaseHydration(double amount) { hydration = Math.min(hydration + amount, MAX_VALUE); }
    public void decreaseHydration(double amount) { hydration = Math.max(hydration - amount, 0); }

    public int getUrgeToGo() { return urgeToGo; }
    public void setUrgeToGo(int amount) { urgeToGo = Math.min(amount, 100);}
    public void increaseUrgeToGo(int amount) { urgeToGo = Math.min(urgeToGo + amount, 100); }
    
    public int getUnderwearType() { return UnderwearType; }
    public void setUnderwearType(int type) { UnderwearType = Math.max(0, Math.min(type, 3)); }

    public int getLayers() { return layers; }
    public void setLayers(int type) { layers = type; }
    public void increaseLayers(int type) { layers = Math.min(layers + type, 2); }
    public void decreaseLayers(int type) { layers = Math.max(layers - type, 0);}


    // Method to handle an accident
    public void handleAccident(boolean isBladder, Player player, Boolean FromWarning) {
        if (isBladder) {
            if (UnderwearType == 0) {diaperWetness = 100;}
            if (UnderwearType == 1) {diaperWetness = Math.min(diaperWetness + bladder, MAX_VALUE);}
            if (UnderwearType == 2) {
                if (layers == 0) {diaperWetness = Math.min(diaperWetness + bladder/2, MAX_VALUE);}
                if (layers == 1) {diaperWetness = Math.min(diaperWetness + bladder/4, MAX_VALUE);}
                if (layers == 2) {diaperWetness = Math.min(diaperWetness + bladder/8, MAX_VALUE);}
            }
            if (UnderwearType == 3) {
                if (layers == 0) {diaperWetness = Math.min(diaperWetness + bladder/4, MAX_VALUE);}
                if (layers == 1) {diaperWetness = Math.min(diaperWetness + bladder/8, MAX_VALUE);}
                if (layers == 2) {diaperWetness = Math.min(diaperWetness + bladder/16, MAX_VALUE);}
            }
            bladder = 0;
            if (!getBladderLockIncon()) {increaseBladderIncontinence(0.5);}
            urgeToGo = 0;
            if (FromWarning) {
                player.playSound(player.getLocation(), "minecraft:pee1", SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
        } else {
            if (UnderwearType == 0) {diaperFullness = 100;}
            if (UnderwearType == 1) {diaperFullness = Math.min(diaperFullness + bowels, MAX_VALUE);}
            if (UnderwearType == 2) {
                if (layers == 0) {diaperFullness = Math.min(diaperFullness + bowels/2, MAX_VALUE);}
                if (layers == 1) {diaperFullness = Math.min(diaperFullness + bowels/4, MAX_VALUE);}
                if (layers == 2) {diaperFullness = Math.min(diaperFullness + bowels/8, MAX_VALUE);}
            }
            if (UnderwearType == 3) {
                if (layers == 0) {diaperFullness = Math.min(diaperFullness + bowels/4, MAX_VALUE);}
                if (layers == 1) {diaperFullness = Math.min(diaperFullness + bowels/8, MAX_VALUE);}
                if (layers == 2) {diaperFullness = Math.min(diaperFullness + bowels/16, MAX_VALUE);}
            }
            bowels = 0;
            if(!getBowelLockIncon()){increaseBowelIncontinence(0.5);}
            urgeToGo = 0;
            if (FromWarning) {
                player.playSound(player.getLocation(), "minecraft:mess1", SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
        }
        if (isBladder ? getBladderIncontinence() > 7 : getBowelIncontinence() > 7) {
            player.sendMessage("Seems like someone is losing control");
        }
        else if (isBladder ? getBladderIncontinence() > 3 : getBowelIncontinence() > 3) {
            player.sendMessage("You should try to make it to the potty next time.");
        }else {
            player.sendMessage("Oh no! You had an accident...");
        }
    }
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

