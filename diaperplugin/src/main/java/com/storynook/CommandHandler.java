package com.storynook;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.storynook.menus.SettingsMenu;

import net.md_5.bungee.api.ChatColor;


public class CommandHandler implements CommandExecutor, TabCompleter{
    private Plugin plugin;
    HashMap<UUID, Boolean> NightVisionToggle = new HashMap<>();
    public CommandHandler(Plugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
       if (command.getName().equalsIgnoreCase("settings") && sender instanceof Player) {
        Player player = (Player) sender;
        SettingsMenu.OpenSettings(player, plugin);

        return true;
        }
        if (command.getName().equalsIgnoreCase("pee") && sender instanceof Player) {
            Player player = (Player) sender;
            PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());
            if (stats.getBladder() > 10) {
                stats.handleAccident(true, player, true);
            }
    
            return true;
        }
        if (command.getName().equalsIgnoreCase("poop") && sender instanceof Player) {
            Player player = (Player) sender;
            PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());
            if (stats.getBowels() > 10) {
                stats.handleAccident(false, player, true);
            }
    
            return true;
        }
        if (command.getName().equalsIgnoreCase("stats") && sender instanceof Player) {
            PlayerStats stats = plugin.getPlayerStats(((Player) sender).getUniqueId());
            if (stats != null) {
                sender.sendMessage("Your current statistics are:");
                sender.sendMessage("HardCore: " + (stats.getHardcore() ? ChatColor.RED + "On" : ChatColor.GREEN + "Off"));
                sender.sendMessage("Diaper wetness: " + Math.round(stats.getDiaperWetness()) + "/100");
                if(stats.getDiaperFullness() > 0){sender.sendMessage("Diaper fullness: " + Math.round(stats.getDiaperFullness()) + "/100");}
                sender.sendMessage("Bladder incontinence: " + Math.round(stats.getBladderIncontinence()) + "/10");
                if(stats.getMessing()){sender.sendMessage("Bowel incontinence: " + Math.round(stats.getBowelIncontinence()) + "/10");}
                sender.sendMessage("MinFill: " + (int)stats.getMinFill());
                sender.sendMessage("Time Full " + stats.getTimeWorn());
                sender.sendMessage("Bladder Locked? " + stats.getBladderLockIncon() + (stats.getMessing() ? " Bowels Locked? " + stats.getBowelLockIncon() : ""));
            } else {
                sender.sendMessage("Your current statistics are not available.");
            }
            return true;
        }


        if (command.getName().equalsIgnoreCase("Check") && sender instanceof Player) {
            Player player = (Player) sender;
            PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());
            
            if (stats != null) {
                // Check for target player argument and optional validation
                Player target = null;
                
                if (args.length > 0) {
                    target = Bukkit.getPlayer(args[0]);
                    if (target == null || !plugin.getPlayerStats(target.getUniqueId()).isCaregiver(player.getUniqueId())) {
                        player.sendMessage("You do not have permission to check this player's statistics.");
                        return true;
                    }
                } else {
                    target = player; // Default to checking the sender's own stats if no target is specified
                }
                
                PlayerStats targetStats = plugin.getPlayerStats(target.getUniqueId());
                if (targetStats != null) {

                    Location playerLoc = player.getLocation();
                    Location targetLoc = target.getLocation();
                    
                    double distance = playerLoc.distance(targetLoc);
                    if(distance <=10){
                        plugin.CheckLittles(player, targetStats, target);
                    } else {
                        player.sendMessage("The target player is not within the specified distance.");
                    } 
                } 
                else {
                    player.sendMessage("Cannot fetch target player's statistics.");
                }
            }
            else {
                player.sendMessage("Your current statistics are not available.");
            }
            return true;
        }
    
        if (command.getName().equalsIgnoreCase("caregiver") && sender instanceof Player) {
            if (args.length > 0) {
                PlayerStats stats = plugin.getPlayerStats(((Player) sender).getUniqueId());
                if (stats == null) {
                    sender.sendMessage("Player Stats not available");
                    return true;
                }
        
                switch (args[0].toLowerCase()) {
                    case "add":
                        if (args.length != 2) {
                            sender.sendMessage("Usage: /caregiver add <playername>");
                            return true;
                        }
                        Player target = plugin.getServer().getPlayer(args[1]);
                        if (target == null) {
                            sender.sendMessage("Player not found.");
                            return true;
                        }
                        UUID targetUUID = target.getUniqueId();
                        if (!stats.getCaregivers().contains(targetUUID)) {
                            if (sender != target) {
                                stats.addCaregiver(targetUUID);
                                sender.sendMessage("Added player " + target.getName() + " as a caregiver.");
                            }
                            else{
                                sender.sendMessage("You can't add yourself as a caregiver, silly bab.");
                            }
                        } else {
                            sender.sendMessage("This player is already a caregiver.");
                        }
                        break;
        
                    case "remove":
                        if (args.length != 2) {
                            sender.sendMessage("Usage: /caregiver remove <playername>");
                            return true;
                        }
                        Player targetToRemove = plugin.getServer().getPlayer(args[1]);
                        if (targetToRemove == null) {
                            sender.sendMessage("Player not found.");
                            return true;
                        }
                        UUID targetUUIDToRemove = targetToRemove.getUniqueId();
                        if (stats.getCaregivers().contains(targetUUIDToRemove)) {
                            stats.removeCaregiver(targetUUIDToRemove);
                            sender.sendMessage("Removed player " + targetToRemove.getName() + " as a caregiver.");
                        } else {
                            sender.sendMessage("This player is not a caregiver.");
                        }
                        break;
        
                    case "list":
                        if (args.length != 1) {
                            sender.sendMessage("Usage: /caregiver list");
                            return true;
                        }
                        StringBuilder caregiversList = new StringBuilder();
                        for (UUID uuid : stats.getCaregivers()) {
                            Player caregiver = plugin.getServer().getPlayer(uuid);
                            if (caregiver != null) {
                                caregiversList.append(caregiver.getName()).append(", ");
                            }
                        }
                        if (caregiversList.length() > 0) {
                            sender.sendMessage("Caregivers: " + caregiversList.substring(0, caregiversList.length() - 2));
                        } else {
                            sender.sendMessage("No caregivers found.");
                        }
                        break;
        
                    default:
                        sender.sendMessage("Usage: /caregiver <add|remove|list> [playername]");
                }
            } else {
                sender.sendMessage("Usage: /caregiver <add|remove|list> [playername]");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("minfill") && sender instanceof Player) {
            Player player = (Player) sender;
            PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());
        
            if (stats == null) {
                player.sendMessage("Player Stats not available");
                return true;
            }
        
            if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
                player.sendMessage(ChatColor.GOLD + "Usage: /minfill <amount>");
                player.sendMessage(ChatColor.GOLD + "This command sets the minimum fill level before a warning is triggered.");
                player.sendMessage(ChatColor.GOLD + "This number assumes 1 incontinence.");
                player.sendMessage(ChatColor.GOLD + "Example: /minfill 10 would set it so 1 full drop will need to be filled before a warning.");
                player.sendMessage(ChatColor.GOLD + "Example: /minfill 0 would mean complete randomness. This is equivalent to full incontinence.");
                player.sendMessage(ChatColor.GOLD + "Example: /minfill 100 would mean 9 drops are needed before a warning.");
                return true;
            } else if (args.length == 1) {
                // Existing code for setting min fill
                double value;
                try {
                    value = Double.parseDouble(args[0]);
                    stats.setMinFill((int) value);
                    player.sendMessage("Min Fill set to: " + (int)stats.getMinFill());
                    return true;
                } catch (NumberFormatException e) {
                    player.sendMessage("Invalid number format.");
                    return true;
                }
            } else {
                // If the command is not recognized or incorrect usage, provide a message to the user
                player.sendMessage("Usage: /minfill [amount|help]");
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("lockincon") && sender instanceof Player) {
            Player player = (Player) sender;
            PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());

            if (stats == null) {
                player.sendMessage("Player Stats not available");
                return true;
            }

            if (args.length == 0) {
                player.sendMessage("Usage: /lockincon <bladder|bowel|both> <number> [playername]");
                return true;
            }

            String type = args[0].toLowerCase();
            double value;
            try {
                value = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("Invalid number format.");
                return true;
            }

            Player target;
            if (args.length > 2) {
                target = plugin.getServer().getPlayer(args[2]);
                if (target == null) {
                    player.sendMessage("Player not found.");
                    return true;
                }
            } else {
                target = player; // Default to the sender if no target is specified
            }
            PlayerStats targetStats = plugin.getPlayerStats(target.getUniqueId());
            if (targetStats == null) {
                player.sendMessage("Target player stats not available.");
                return true;
            }
            if (!targetStats.isCaregiver(player.getUniqueId()) && target != player) {
                player.sendMessage("You do not have permission to use this command on the target player.");
                return true;
            }
            if (targetStats.getHardcore() && target == sender) {
                player.sendMessage("You are in hardcore mode. Ask a caregiver for help.");
                return true;
            }

            if (player != null && sender instanceof Player && target != player) {
                Player senderPlayer = (Player) sender;
                Location senderLocation = senderPlayer.getLocation();
                Location targetLocation = target.getLocation();
        
                // Check the distance between the sender and the target player
                double distance = senderLocation.distance(targetLocation);
                if (distance > 10) {
                    sender.sendMessage("The target player is not within your range of 10 blocks.");
                    return true;
                }
            }

            switch (type) {
                case "bladder":
                    targetStats.setBladderLockIncon(true);
                    targetStats.setBladderIncontinence(value);
                    player.sendMessage("Set bladder incontinence to: " + value);
                    break;
                case "bowel":
                    targetStats.setBowelLockIncon(true);
                    targetStats.setBowelIncontinence(value);
                    player.sendMessage("Set bowel incontinence to: " + value);
                    break;
                case "both":
                    targetStats.setBladderLockIncon(true);
                    targetStats.setBowelLockIncon(true);
                    targetStats.setBladderIncontinence(value);
                    targetStats.setBowelIncontinence(value);
                    player.sendMessage("Set both bladder and bowel incontinence to: " + value);
                    break;
                default:
                    player.sendMessage("Usage: /lockincon <bladder|bowel|both> <number> [playername]");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("unlockincon") && sender instanceof Player) {
            Player player = (Player) sender;
            PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());

            if (stats == null) {
                return true;
            }

            if(args.length == 0){
                sender.sendMessage("Usage: /unlockincon <bladder|bowel|both> [playername]");
                return true;
            }
            
            Player target;
            if (args.length > 1) {
                target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("Player not found.");
                    return true;
                }
            } else {
                target = player; // Default to the sender if no target is specified
            }
            PlayerStats targetStats = plugin.getPlayerStats(target.getUniqueId());
            if (targetStats == null) {
                player.sendMessage("Target player stats not available.");
                return true;
            }
            if (!targetStats.isCaregiver(player.getUniqueId()) && target != player) {
                player.sendMessage("You do not have permission to use this command on the target player.");
                return true;
            }
            if (targetStats.getHardcore() && target == sender) {
                player.sendMessage("You are in hardcore mode. Ask a caregiver for help.");
                return true;
            }

            if (player != null && sender instanceof Player && target != player) {
                Player senderPlayer = (Player) sender;
                Location senderLocation = senderPlayer.getLocation();
                Location targetLocation = target.getLocation();
        
                // Check the distance between the sender and the target player
                double distance = senderLocation.distance(targetLocation);
                if (distance > 10) {
                    sender.sendMessage("The target player is not within your range of 10 blocks.");
                    return true;
                }
            }

            // Check and unlock based on the argument provided
            String type = args.length > 0 ? args[0].toLowerCase() : "both";
            targetStats.unlockIncon(type);
            sender.sendMessage("Incontinence settings have been unlocked for the specified type.");

            return true;
        }
        if (command.getName().equalsIgnoreCase("nightvision") || command.getName().equalsIgnoreCase("nv")  && sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("diaperplugin.nightvision") && !player.isOp()) {
                player.sendMessage("You do not have permission to use this command.");
                return true;
            }
            UUID playerUUID = player.getUniqueId();
            boolean currentState = NightVisionToggle.getOrDefault(playerUUID, false);
            NightVisionToggle.put(playerUUID, !currentState);
    
            return true;
        }
        if (command.getName().equalsIgnoreCase("debug") && sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("diaperplugin.debug") && !player.isOp()) {
                player.sendMessage("You do not have permission to use this command.");
                return true;
            }

            PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());

            if (stats == null) {
                player.sendMessage("Player Stats not available");
                return true;
            }

            if (args.length == 0) {
                player.sendMessage("Usage: /debug <bladder|bowel|both|type|wetness|fullness|effectduration|timeworn> <number> [playername]");
                return true;
            }

            String type = args[0].toLowerCase();
            // if (type.equals("showfill")) {
            //     // Toggle showfill for the sender
            //     boolean currentState = plugin.showfill.getOrDefault(player.getUniqueId(), false);
            //     plugin.showfill.put(player.getUniqueId(), !currentState);
            //     player.sendMessage("Live feed of fill: " + (plugin.showfill.get(player.getUniqueId()) ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
            //     return true;
            // }

            if (args.length < 2) {
                player.sendMessage("Usage: /debug " + type + " <number> [playername]");
                return true;
            }
            
            double value;
            try {
                value = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("Invalid number format.");
                return true;
            }

            Player target;
            if (args.length > 2) {
                target = plugin.getServer().getPlayer(args[2]);
                if (target == null) {
                    player.sendMessage("Player not found.");
                    return true;
                }
            } else {
                target = player; // Default to the sender if no target is specified
            }

            PlayerStats targetStats = plugin.getPlayerStats(target.getUniqueId());
            if (targetStats == null) {
                player.sendMessage("Target player stats not available.");
                return true;
            }

            switch (type) {
                case "bladder":
                    targetStats.setBladder(value);
                    player.sendMessage("Set bladder to: " + value);
                    break;
                case "bowel":
                    targetStats.setBowels(value);
                    player.sendMessage("Set bowels to: " + value);
                    break;
                case "both":
                    targetStats.setBladder(value);
                    targetStats.setBowels(value);
                    player.sendMessage("Set both bladder and bowel to: " + value);
                    break;
                case "type":
                    targetStats.setUnderwearType((int)value);
                    break;
                case "wetness":
                    targetStats.setDiaperWetness(value);
                    player.sendMessage("Set diaper wetness to: " + value);
                    break;
                case "fullness":
                    targetStats.setDiaperFullness(value);
                    player.sendMessage("Set diaper fullness to: " + value);
                    break;
                case "effectduration":
                    targetStats.setEffectDuration((int)value);;
                    player.sendMessage("Set duration to: " + value);
                    break;
                case "timeworn":
                    targetStats.setTimeWorn((int)value);
                    player.sendMessage("Set timeworn to: " + value);
                    break;
                default:
                    player.sendMessage("Usage: /debug <bladder|bowel|both|type|wetness|fullness|effectduration|timeworn> <number> [playername]");
            }
            return true;
        }
        
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Your tab completion logic here
        List<String> completions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("debug")) {
            if (args.length == 1) { // First argument completion
                completions.add("bladder");
                completions.add("bowel");
                completions.add("both");
                completions.add("type");
                completions.add("wetness");
                completions.add("fullness");
                completions.add("effectduration");
                completions.add("timeworn");
                // completions.add("showfill");
                return completions; // Filter based on arguments (optional)
            } else if (args.length == 2 && args[0].equalsIgnoreCase("type")) {
                completions.add("0");
                completions.add("1");
                completions.add("2");
                completions.add("3");
            } else if (args.length == 3) {
                // Add online player names to completions
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            }
        }
        else if (command.getName().equalsIgnoreCase("caregiver")) {
            if (args.length == 1) { // First argument completion
                completions.add("add");
                completions.add("remove");
                completions.add("list");
                return completions; // Filter based on arguments (optional)
            }else if (args.length == 2) {
                // Add online player names to completions
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            }
        }
        else if (command.getName().equalsIgnoreCase("lockincon")) {
            if (args.length == 1) { // First argument completion
                completions.add("bladder");
                completions.add("bowel");
                completions.add("both");
                return completions; // Filter based on arguments (optional)
            }
            else if (args.length == 2) {
                completions.add("1");
                completions.add("2");
                completions.add("3");
                completions.add("4");
                completions.add("5");
                completions.add("6");
                completions.add("7");
                completions.add("8");
                completions.add("9");
                completions.add("10");
            }
            else if (args.length == 3) {
                // Add online player names to completions
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            }
        }
        else if (command.getName().equalsIgnoreCase("unlockincon")) {
            if (args.length == 1) { // First argument completion
                completions.add("bladder");
                completions.add("bowel");
                completions.add("both");
                return completions; // Filter based on arguments (optional)
            }else if (args.length == 2) {
                // Add online player names to completions
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            }
        }
        else if (command.getName().equalsIgnoreCase("check")) {
            if (args.length == 1) {
                // Add online player names to completions
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            }
        }
        return completions; // Always return the list, potentially empty.
    }
}
