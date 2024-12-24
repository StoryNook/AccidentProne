package com.storynook;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
// import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.ScoreboardManager;

import net.md_5.bungee.api.ChatColor;


public class CustomInventory implements Listener {
    private Plugin plugin;

    public CustomInventory(Plugin plugin) {
        this.plugin = plugin;
    }
    public static void OpenSettings(Player player, Plugin plugin) {
        UUID playerUUID = player.getUniqueId();
        PlayerStats stats = plugin.getPlayerStats(playerUUID);
        Inventory menu = Bukkit.createInventory(player, 9, "Settings");

        ItemStack Optin = new ItemStack(Material.SLIME_BALL); // Custom button
        ItemMeta OptinMeta = Optin.getItemMeta();
        if (OptinMeta != null) {
            List<String> lore = Arrays.asList(
                "Diaper Plugin: " + (stats.getOptin() ? ChatColor.GREEN + "On" : ChatColor.RED + "Off"), 
                "Enables the plugin to track stats.", 
                "This like, bladder, underwear type, incontinence, etc."
            );
            OptinMeta.setLore(lore);
            OptinMeta.setDisplayName("Opt into plugin");
            OptinMeta.setCustomModelData(626009);
            Optin.setItemMeta(OptinMeta);   
        }
        ItemStack Messing = new ItemStack(Material.SLIME_BALL);
        ItemMeta MessingMeta = Messing.getItemMeta();
        if (MessingMeta != null) {
            List<String> lore = Arrays.asList(
                "Messing accidents can happen: " + (stats.getMessing() ? ChatColor.GREEN + "On" : ChatColor.RED + "Off"),
                "Enables the bowels to start filling."
            );
            MessingMeta.setLore(lore);
            MessingMeta.setDisplayName("Messing");
            MessingMeta.setCustomModelData(626004);
            Messing.setItemMeta(MessingMeta);
        }
        ItemStack ScoreBoard = new ItemStack(Material.PAINTING); // Custom button
        ItemMeta ScoreobardMeta = ScoreBoard.getItemMeta();
        if (ScoreobardMeta != null) {
            String UISetting;
            if (stats.getUI() == 0) {UISetting = "Hidden";}
            if (stats.getUI() == 1) {UISetting = "Scoreboard";}
            if(stats.getUI() == 2){UISetting = "HotBar";}
            else{UISetting = "Error";}
            List<String> lore = Arrays.asList(
                "Current UI Selected " + UISetting,
                "Different styles for you to pick from.",
                "Classic Scoreboard style is defualt,",
                "there is also HotBar, and Hidden."
            );
            ScoreobardMeta.setLore(lore);
            ScoreobardMeta.setDisplayName("Stats Visable");
            ScoreBoard.setItemMeta(ScoreobardMeta);   
        }

        ItemStack HardCore = new ItemStack(Material.FIRE_CHARGE); // Custom button
        ItemMeta HardCoreMeta = HardCore.getItemMeta();
        if (HardCoreMeta != null) {
            List<String> lore = Arrays.asList(
                "HardCore: " + (stats.getHardcore() ? ChatColor.GREEN + "On" : ChatColor.RED + "Off"),
                "Removes you ablility to change yourself.",
                "Only Caregivers can change you.",
                "Also can't unlock or set your incontinence levels."
            );
            HardCoreMeta.setLore(lore);
            HardCoreMeta.setDisplayName("HardCore");
            HardCore.setItemMeta(HardCoreMeta);   
        }
        
        menu.setItem(0, Optin);
        menu.setItem(1, Messing);
        menu.setItem(2, ScoreBoard);
        menu.setItem(3, HardCore);
        player.openInventory(menu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Settings")) {
            return;
        }

        event.setCancelled(true); // Prevent default behavior

        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        UUID playerUUID = player.getUniqueId();
        PlayerStats stats = plugin.getPlayerStats(playerUUID);

        if (stats == null) {
            return;
        }

        ItemMeta meta = event.getCurrentItem().getItemMeta();

        if (meta.hasCustomModelData() && meta.getCustomModelData() == 626009) {
            stats.setOptin(!stats.getOptin());
            player.sendMessage(stats.getOptin() ? "You have opted into the plugin." : "You have opted out of the plugin.");
            updateScoreboard(player, stats);
        } else if (meta.hasCustomModelData() && meta.getCustomModelData() == 626004) {
            stats.setMessing(!stats.getMessing());
            player.sendMessage(stats.getMessing() ? "Messing has been enabled." : "Messing has been disabled.");
        } else if (event.getCurrentItem().getType() == Material.PAINTING) {
            if(stats.getOptin()){
                int newUI = stats.getUI();
                newUI++;
                if (newUI > 2) {
                    newUI = 0;
                }
                stats.setUI(newUI);
                if (stats.getUI() == 1){updateScoreboard(player, stats); player.sendMessage("Scoreboard Style Selected");}
                if (stats.getUI() == 0){updateScoreboard(player, stats); player.sendMessage("Stats Hidden");}
                if (stats.getUI() == 2){updateScoreboard(player, stats); player.sendMessage("Hotbar Style Selected");}
                plugin.savePlayerStats(player); // Save the updated stats
            }
        } else if (event.getCurrentItem().getType() == Material.FIRE_CHARGE) {
            stats.setHardcore(!stats.getHardcore());
            player.sendMessage(stats.getHardcore() ? "You have enabled " + ChatColor.RED + "HardCore." : "You have Disabled " + ChatColor.RED + "HardCore.");
            plugin.savePlayerStats(player); // Save the updated stats
        }
    }
    private void updateScoreboard(Player player, PlayerStats stats) {
        // Ensure ScoreboardManager is not null
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        if (scoreboardManager == null) {
            plugin.getLogger().warning("ScoreboardManager is null; Unable to set player scoreboard!");
            return;
        }

        // Update the scoreboard or clear it
        if (stats.getOptin() && stats.getUI() == 1) {
            ScoreBoard.createSidebar(player);
        } else if(stats.getOptin() && !(stats.getUI() == 1)){
            player.setScoreboard(scoreboardManager.getNewScoreboard()); // Clear the scoreboard
        }
    }
}
