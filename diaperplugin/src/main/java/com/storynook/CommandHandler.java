package com.storynook;
import com.storynook.items.ItemManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;


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
            sender.sendMessage("Bladder: " + stats.getBladder() + "/100");
            sender.sendMessage("Bowels: " + stats.getBowels() + "/100");
            sender.sendMessage("Diaper wetness: " + stats.getDiaperWetness() + "/100");
            sender.sendMessage("Diaper fullness: " + stats.getDiaperFullness() + "/100");
            sender.sendMessage("Bladder incontinence: " + stats.getBladderIncontinence() + "/10");
            sender.sendMessage("Bowel incontinence: " + stats.getBowelIncontinence() + "/10");
            sender.sendMessage("Bladder fill rate: " + stats.getBladderFillRate());
            sender.sendMessage("Bowel fill rate: " + stats.getBowelFillRate());
            sender.sendMessage("Hydration: " + stats.getHydration() + "/100");
            sender.sendMessage("Urge to go: " + stats.getUrgeToGo() + "/100");
        } else {
            sender.sendMessage("Your current statistics are not available.");
        }
        return true;
    }

    if (command.getName().equalsIgnoreCase("Check") && sender instanceof Player) {
        PlayerStats stats = plugin.getPlayerStats(((Player) sender).getUniqueId());
        if (stats != null) {

        } else {
            
        }
        return true;
    }
    // if (command.getName().equalsIgnoreCase("caregiver") && sender instanceof Player) {
    //     if (args.length > 0) {
    //         PlayerStats stats = plugin.getPlayerStats(((Player) sender).getUniqueId());
    //         if (stats == null) {
    //             sender.sendMessage("Player Stats not available");
    //             return true;
    //         }
    
    //         switch (args[0].toLowerCase()) {
    //             case "add":
    //                 if (args.length != 2) {
    //                     sender.sendMessage("Usage: /caregiver add <playername>");
    //                     return true;
    //                 }
    //                 Player target = plugin.getServer().getPlayer(args[1]);
    //                 if (target == null) {
    //                     sender.sendMessage("Player not found.");
    //                     return true;
    //                 }
    //                 UUID targetUUID = target.getUniqueId();
    //                 if (!stats.getCaregivers().contains(targetUUID)) {
    //                     stats.addCaregiver(targetUUID);
    //                     sender.sendMessage("Added player " + target.getName() + " as a caregiver.");
    //                 } else {
    //                     sender.sendMessage("This player is already a caregiver.");
    //                 }
    //                 break;
    
    //             case "remove":
    //                 if (args.length != 2) {
    //                     sender.sendMessage("Usage: /caregiver remove <playername>");
    //                     return true;
    //                 }
    //                 Player targetToRemove = plugin.getServer().getPlayer(args[1]);
    //                 if (targetToRemove == null) {
    //                     sender.sendMessage("Player not found.");
    //                     return true;
    //                 }
    //                 UUID targetUUIDToRemove = targetToRemove.getUniqueId();
    //                 if (stats.getCaregivers().contains(targetUUIDToRemove)) {
    //                     stats.removeCaregiver(targetUUIDToRemove);
    //                     sender.sendMessage("Removed player " + targetToRemove.getName() + " as a caregiver.");
    //                 } else {
    //                     sender.sendMessage("This player is not a caregiver.");
    //                 }
    //                 break;
    
    //             case "list":
    //                 if (args.length != 1) {
    //                     sender.sendMessage("Usage: /caregiver list");
    //                     return true;
    //                 }
    //                 StringBuilder caregiversList = new StringBuilder();
    //                 for (UUID uuid : stats.getCaregivers()) {
    //                     Player caregiver = plugin.getServer().getPlayer(uuid);
    //                     if (caregiver != null) {
    //                         caregiversList.append(caregiver.getName()).append(", ");
    //                     }
    //                 }
    //                 if (caregiversList.length() > 0) {
    //                     sender.sendMessage("Caregivers: " + caregiversList.substring(0, caregiversList.length() - 2));
    //                 } else {
    //                     sender.sendMessage("No caregivers found.");
    //                 }
    //                 break;
    
    //             default:
    //                 sender.sendMessage("Usage: /caregiver <add|remove|list> [playername]");
    //         }
    //     } else {
    //         sender.sendMessage("Usage: /caregiver <add|remove|list> [playername]");
    //     }
    //     return true;
    // }
    return false;
}
}
