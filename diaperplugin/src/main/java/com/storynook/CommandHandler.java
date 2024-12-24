package com.storynook;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class CommandHandler implements CommandExecutor{
    private Plugin plugin;
    public CommandHandler(Plugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
       if (command.getName().equalsIgnoreCase("settings") && sender instanceof Player) {
        Player player = (Player) sender;
        CustomInventory.OpenSettings(player, plugin);

        return true;
        }
        if (command.getName().equalsIgnoreCase("stats") && sender instanceof Player) {
            PlayerStats stats = plugin.getPlayerStats(((Player) sender).getUniqueId());
            if (stats != null) {
                sender.sendMessage("Your current statistics are:");
                sender.sendMessage("HardCore: " + (stats.getHardcore() ? "On" : "Off"));
                sender.sendMessage("Diaper wetness: " + stats.getDiaperWetness() + "/100");
                sender.sendMessage("Diaper fullness: " + stats.getDiaperFullness() + "/100");
                sender.sendMessage("Bladder incontinence: " + stats.getBladderIncontinence() + "/10");
                sender.sendMessage("Bowel incontinence: " + stats.getBowelIncontinence() + "/10");
                sender.sendMessage("Time Full " + stats.getTimeWorn());
                sender.sendMessage("Bladder Locked? " + stats.getBladderLockIncon() + " Bowels Locked? " + stats.getBowelLockIncon());
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
                String image = "";
                String state = "";
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
                        image = ScoreBoard.getUnderwearStatus((int)targetStats.getDiaperWetness(), (int)targetStats.getDiaperFullness(), (int)targetStats.getUnderwearType(), 2);
                        if ((int)targetStats.getDiaperWetness() > 1 && (int)targetStats.getDiaperFullness() > 1) {
                            // state = "Wet And Messy";
                        } else if ((int)targetStats.getDiaperWetness() > 1) {
                            // state = "Wet";
                        } else if ((int)targetStats.getDiaperFullness() > 1) {
                            // state = "Messy";
                        } else if ((int)targetStats.getDiaperFullness() == 0 && (int)targetStats.getDiaperWetness() == 0) {
                            // state = "Clean";
                        }
                        
                        player.sendTitle(image, state, 10, 20, 10);
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


        // if (command.getName().equalsIgnoreCase("Check") && sender instanceof Player) {
        //     PlayerStats stats = plugin.getPlayerStats(((Player) sender).getUniqueId());
        //     Player player = (Player) sender;
        //     if (stats != null) {
        //         String image = ScoreBoard.getUnderwearStatus((int)stats.getDiaperWetness(), (int)stats.getDiaperFullness(), (int)stats.getUnderwearType(), 2);
        //         String State = "";
        //         if(stats.getDiaperWetness() > 1 && stats.getDiaperFullness() > 1){State = "Wet And Messy";}
        //         else if (stats.getDiaperWetness() > 1) {State = "Wet";}
        //         else if (stats.getDiaperFullness() > 1) {State = "Messy";}
        //         else if(stats.getDiaperFullness() == 0 && stats.getDiaperWetness() == 0){State = "Clean";}
        //         player.sendTitle(image,State,10, 20, 10);
        //     } else {
                
        //     }
        //     return true;
        // }

        if (command.getName().equalsIgnoreCase("setunderwearlevels") && sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("diaperplugin.setunderwearlevels") || !player.isOp()) {
                player.sendMessage("You do not have permission to use this command.");
                return true;
            }

            if (args.length != 2) {
                player.sendMessage("Usage: /setunderwearlevels <wetness|fullness> <value>");
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

            PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());
            if (stats == null) {
                player.sendMessage("Player Stats not available");
                return true;
            }

            switch (type) {
                case "wetness":
                    stats.setDiaperWetness(value);
                    player.sendMessage("Set diaper wetness to: " + value);
                    break;
                case "fullness":
                    stats.setDiaperFullness(value);
                    player.sendMessage("Set diaper fullness to: " + value);
                    break;
                default:
                    player.sendMessage("Usage: /setunderwearlevels <wetness|fullness> <value>");
            }
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

            // Check and unlock based on the argument provided
            String type = args.length > 0 ? args[0].toLowerCase() : "both";
            targetStats.unlockIncon(type);
            sender.sendMessage("Incontinence settings have been unlocked for the specified type.");

            return true;
        }
        return false;
    }
}
