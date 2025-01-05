package com.storynook;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scoreboard.ScoreboardManager;

import net.md_5.bungee.api.ChatColor;


public class CustomInventory implements Listener {
    private Plugin plugin;
    private static Map<String, UUID> playerHeadMap = new HashMap<>();
    private static Map<UUID, Integer> currentPage = new HashMap<>();
    
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
                else if (stats.getUI() == 1) {UISetting = "Scoreboard";}
                else if(stats.getUI() == 2){UISetting = "HotBar";}
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
    
            ItemStack Bedwetting = new ItemStack(Material.RED_BED); // Custom button
            ItemMeta BedwettingMeta = Bedwetting.getItemMeta();
            if (BedwettingMeta != null) {
                String BedwettingSetting;
                if (stats.getBedwetting() == 2) {BedwettingSetting = ChatColor.GREEN + "On";}
                else if (stats.getBedwetting() == 1) {BedwettingSetting = ChatColor.YELLOW + "Limited";}
                else if(stats.getBedwetting() == 0){BedwettingSetting = ChatColor.RED + "Off";}
                else{BedwettingSetting = "Error";}
                List<String> lore = Arrays.asList(
                    "Bedwetting: " + BedwettingSetting,
                    "Limited will be random based on your bladder control."
                );
                BedwettingMeta.setLore(lore);
                BedwettingMeta.setDisplayName("Bedwetting");
                Bedwetting.setItemMeta(BedwettingMeta);   
            }
            ItemStack CareGiver = new ItemStack(Material.PLAYER_HEAD); // Custom button
            ItemMeta CareGivermeta = CareGiver.getItemMeta();
            if (CareGivermeta != null) {
                List<String> lore = Arrays.asList(
                    "Edit your caregiver settings",
                    ChatColor.GREEN + "Add, " + ChatColor.RED + "Remove, " + ChatColor.GOLD + "List"
                );
                CareGivermeta.setLore(lore);
                CareGivermeta.setDisplayName("Caregiver Settings");
                CareGiver.setItemMeta(CareGivermeta);   
            }
            ItemStack MinFill = new ItemStack(Material.WATER_BUCKET); // Custom button
            ItemMeta MinFillMeta = MinFill.getItemMeta();
            if (CareGivermeta != null) {
                List<String> lore = Arrays.asList(
                    "Minimum amount needed for a warning.",
                    "Current limit: " + ChatColor.GOLD + stats.getMinFill(),
                    "Each Icon is 10, range 0-100",
                    "Use /minfill help for more info"
                );
                MinFillMeta.setLore(lore);
                MinFillMeta.setDisplayName("Warning threshold");
                MinFill.setItemMeta(MinFillMeta);   
            }
            ItemStack Incon = new ItemStack(Material.OAK_SIGN); // Custom button
            ItemMeta InconlMeta = Incon.getItemMeta();
            if (InconlMeta != null) {
                List<String> lore = Arrays.asList(
                    "Current Bladder incon: " + stats.getBladderIncontinence(),
                    stats.getMessing() ? "Current Bowel Incon: " + stats.getBowelIncontinence() : "Locked? " + (stats.getBladderLockIncon() ? ChatColor.RED + "Yes" : ChatColor.GREEN + "No"),
                    stats.getMessing() ? "Bladder locked? " + (stats.getBladderLockIncon() ? ChatColor.RED + "Yes" : ChatColor.GREEN + "No") : "",
                    stats.getMessing() ? "Bowel locked? " + (stats.getBowelLockIncon() ? ChatColor.RED + "Yes" : ChatColor.GREEN + "No") : ""
                );
                InconlMeta.setLore(lore);
                InconlMeta.setDisplayName("Incontinence");
                Incon.setItemMeta(InconlMeta);   
            }
            ItemStack Stinklines = new ItemStack(Material.SLIME_BALL); // Custom button
            ItemMeta StinklinesMeta = Stinklines.getItemMeta();
            if (StinklinesMeta != null) {
                List<String> lore = Arrays.asList(
                    "Stink Lines Visable " + (stats.getStinklines() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"),
                    "Shows stink lines when your diaper is full"
                );
                StinklinesMeta.setLore(lore);
                StinklinesMeta.setDisplayName("Stink Lines");
                Stinklines.setItemMeta(StinklinesMeta);   
            }
            
            menu.setItem(0, Optin);
            menu.setItem(1, Messing);
            menu.setItem(2, ScoreBoard);
            menu.setItem(3, HardCore);
            menu.setItem(4, Bedwetting);
            menu.setItem(5, CareGiver);
            menu.setItem(6, MinFill);
            menu.setItem(7, Incon);
            // menu.setItem(8, Stinklines);
            player.openInventory(menu);
        }

        public static void IncontinenceSettings(Player player, Plugin plugin) {
            UUID playerUUID = player.getUniqueId();
            PlayerStats stats = plugin.getPlayerStats(playerUUID);
            Inventory menu = Bukkit.createInventory(player, 9, "Incontinence Settings");
    
            ItemStack Back = new ItemStack(Material.RED_STAINED_GLASS_PANE); // Custom button
            ItemMeta BackMeta = Back.getItemMeta();
            if (BackMeta != null) {
                BackMeta.setDisplayName("Back to Settings");
                Back.setItemMeta(BackMeta);   
            }
            ItemStack BladderIncon = new ItemStack(Material.WATER_BUCKET);
            ItemMeta BladderInconMeta = BladderIncon.getItemMeta();
            if (BladderInconMeta != null) {
                List<String> lore = Arrays.asList(
                    "Current setting: " + ChatColor.BLUE + stats.getBladderIncontinence(),
                    stats.getHardcore() ? ChatColor.RED + "LOCKED" : ""
                );
                BladderInconMeta.setLore(lore);
                BladderInconMeta.setDisplayName("Bladder Incontinence");
                BladderIncon.setItemMeta(BladderInconMeta);
            }
            if (stats.getMessing()) {
                ItemStack BowelIncon = new ItemStack(Material.COCOA_BEANS); // Custom button
                ItemMeta BowelInconMeta = BowelIncon.getItemMeta();
                if (BowelInconMeta != null) {
                    List<String> lore = Arrays.asList(
                        "Current setting: " + ChatColor.BLUE + stats.getBowelIncontinence(),
                        stats.getHardcore() ? ChatColor.RED + "LOCKED" : ""
                    );
                    BowelInconMeta.setLore(lore);
                    BowelInconMeta.setDisplayName("Bowel Incontinence");
                    BowelIncon.setItemMeta(BowelInconMeta);   
                }
                menu.setItem(2, BowelIncon);
                ItemStack BowelInconLock = new ItemStack(Material.MAGMA_BLOCK); // Custom button
                ItemMeta BowelInconLcokMeta = BowelInconLock.getItemMeta();
                if (BowelInconMeta != null) {
                    List<String> lore = Arrays.asList(
                        " Locked? " + (stats.getBowelLockIncon() ? ChatColor.RED + "Yes" : ChatColor.GREEN + "No"),
                        stats.getHardcore() ? ChatColor.RED + "SETTING LOCKED" : ""
                    );
                    BowelInconLcokMeta.setLore(lore);
                    BowelInconLcokMeta.setDisplayName("Bowel Incontinence Lock");
                    BowelInconLock.setItemMeta(BowelInconLcokMeta);   
                }
                menu.setItem(4, BowelInconLock);
            }
    
            ItemStack BladderInconLock = new ItemStack(Material.SPONGE); // Custom button
            ItemMeta BladderInconLockMeta = BladderInconLock.getItemMeta();
            if (BladderInconLockMeta != null) {
                List<String> lore = Arrays.asList(
                    "Locked?: " + (stats.getBladderLockIncon() ? ChatColor.RED + "Yes" : ChatColor.GREEN + "No"),
                    stats.getHardcore() ? ChatColor.RED + "SETTING LOCKED" : ""
                );
                BladderInconLockMeta.setLore(lore);
                BladderInconLockMeta.setDisplayName("Bladder Incontinence Lock");
                BladderInconLock.setItemMeta(BladderInconLockMeta);   
            }
            
            menu.setItem(0, Back);
            menu.setItem(1, BladderIncon);
            if(!stats.getMessing()){menu.setItem(2, BladderInconLock);}else{menu.setItem(3, BladderInconLock);}
            player.openInventory(menu);
        }
        public static void OpenCareGiverSettings(Player player, Plugin plugin, int page) {
            UUID playerUUID = player.getUniqueId();
            PlayerStats stats = plugin.getPlayerStats(playerUUID);
            Inventory menu = Bukkit.createInventory(player, 9 * 6, "CareGiver Settings");
            int start = page *35;
            int end = page + 35;

            if (page > 0) {
                ItemStack Home = new ItemStack(Material.BARRIER); // Custom button
                ItemMeta HomeMeta = Home.getItemMeta();
                if (HomeMeta != null) {
                    HomeMeta.setDisplayName("Back to Settings");
                    Home.setItemMeta(HomeMeta);   
                }
                menu.setItem(0, Home);
            }
    
            ItemStack Back = new ItemStack(Material.RED_STAINED_GLASS_PANE); // Custom button
            ItemMeta BackMeta = Back.getItemMeta();
            if (BackMeta != null) {
                if (page > 0) {
                    BackMeta.setDisplayName("Previous Page");
                }
                else{
                    BackMeta.setDisplayName("Back to Settings");
                }
                Back.setItemMeta(BackMeta);   
            }
            ItemStack EveryoneCG = new ItemStack(Material.ENDER_PEARL);
            if (stats.getAllCaregiver()) {
                EveryoneCG = new ItemStack(Material.ENDER_EYE); // Change to eye of ender if true
            } else {
                EveryoneCG = new ItemStack(Material.ENDER_PEARL); // Keep as ender pearl if false
            }
            ItemMeta EveryoneCGmeta = EveryoneCG.getItemMeta();
            if (EveryoneCGmeta != null) {
                List<String> lore = Arrays.asList(
                    "Everyone is a caregiver: " + (stats.getAllCaregiver() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"),
                    "Anyone opted to be a caregiver,",
                    "can be one for you.",
                    ChatColor.RED + "WARNING:",
                    "That means anyone can change some of your settings."
                );
                EveryoneCGmeta.setLore(lore);
                EveryoneCGmeta.setDisplayName("Caregivers");
                EveryoneCG.setItemMeta(EveryoneCGmeta);   
            }
            int slot = 9;
            List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
            onlinePlayers.remove(player);
            for (int i = start; i < Math.min(end, onlinePlayers.size()); i++) {
                Player otherPlayer = onlinePlayers.get(i);
                UUID otherUUID = otherPlayer.getUniqueId();
                ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
                SkullMeta headMeta = (SkullMeta) playerHead.getItemMeta();
                if (headMeta != null) {
                    headMeta.setOwningPlayer(otherPlayer);
                    List<String> lore = Arrays.asList("Caregiver: " + (stats.isCaregiver(otherUUID) ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
                    headMeta.setLore(lore);
                    headMeta.setDisplayName(otherPlayer.getName());
                    playerHead.setItemMeta(headMeta);
                }
                menu.setItem(slot++, playerHead);
                playerHeadMap.put(otherPlayer.getName(), otherPlayer.getUniqueId());
            }
            if (end < onlinePlayers.size()) {
                ItemStack nextPage = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                ItemMeta nextPageMeta = nextPage.getItemMeta();
                if (nextPageMeta != null) {
                    nextPageMeta.setDisplayName("Next Page");
                    nextPage.setItemMeta(nextPageMeta);
                }
                menu.setItem(53, nextPage); // Next page item slot
            }
            ItemStack NotCGall = new ItemStack(Material.REDSTONE_LAMP); // Custom button
            ItemMeta NotCGallmeta = NotCGall.getItemMeta();
            if (NotCGallmeta != null) {
                List<String> lore = Arrays.asList(
                    "I have to pick my littles: " + (stats.getspecialCG() ? ChatColor.RED + "No" : ChatColor.GREEN + "Yes"),
                    "Only my littles and I play.",
                    "I can not play with a little unless I am added."
                );
                NotCGallmeta.setLore(lore);
                NotCGallmeta.setDisplayName("My Littles");
                NotCGall.setItemMeta(NotCGallmeta);   
            }
        menu.setItem(45, Back);
        menu.setItem(49, NotCGall);
        menu.setItem(4, EveryoneCG);
        player.openInventory(menu);
        currentPage.put(playerUUID, page);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Settings") && !event.getView().getTitle().equals("CareGiver Settings") && !event.getView().getTitle().equals("Incontinence Settings")) {
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
        if (event.getView().getTitle().equals("Settings")) {
            if (meta.hasCustomModelData() && meta.getCustomModelData() == 626009) {
                stats.setOptin(!stats.getOptin());
                player.sendMessage(stats.getOptin() ? "You have opted into the plugin." : "You have opted out of the plugin.");
                updateScoreboard(player, stats);
                OpenSettings(player, plugin);
            } else if (meta.hasCustomModelData() && meta.getCustomModelData() == 626004) {
                stats.setMessing(!stats.getMessing());
                player.sendMessage(stats.getMessing() ? "Messing has been enabled." : "Messing has been disabled.");
                OpenSettings(player, plugin);
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
                OpenSettings(player, plugin);
            } else if (event.getCurrentItem().getType() == Material.FIRE_CHARGE) {
                stats.setHardcore(!stats.getHardcore());
                player.sendMessage(stats.getHardcore() ? "You have enabled " + ChatColor.RED + "HardCore." : "You have Disabled " + ChatColor.RED + "HardCore.");
                plugin.savePlayerStats(player); // Save the updated stats
                OpenSettings(player, plugin);
            }
            else if (event.getCurrentItem().getType() == Material.RED_BED) {
                if(stats.getOptin()){
                    int newBedwetting = stats.getBedwetting();
                    newBedwetting++;
                    if (newBedwetting > 2) {
                        newBedwetting = 0;
                    }
                    stats.setBedwetting(newBedwetting);
                    if (stats.getBedwetting() == 1){player.sendMessage(ChatColor.YELLOW + "Limited " + ChatColor.WHITE + "Bedwetting");}
                    if (stats.getBedwetting() == 0){player.sendMessage("Bedwetting " + ChatColor.RED + "Disabled");}
                    if (stats.getBedwetting() == 2){player.sendMessage("Bedwetting set to " + ChatColor.GREEN + "always");}
                    plugin.savePlayerStats(player); // Save the updated stats
                }
                OpenSettings(player, plugin);
            }
            else if (event.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                OpenCareGiverSettings(player, plugin, 0);
            }
            else if (event.getCurrentItem().getType() == Material.WATER_BUCKET) {
                if(stats.getOptin()){
                    plugin.setAwaitingInput(player.getUniqueId(), "minFill");
                    player.closeInventory(); // Optionally close the inventory
                    player.sendMessage(ChatColor.YELLOW + "Please enter a new minimum fill value:");
                }
            }
            else if (event.getCurrentItem().getType() == Material.OAK_SIGN) {
                if(stats.getOptin()){
                    IncontinenceSettings(player, plugin);
                }
            }
            else if (event.getCurrentItem().getType() == Material.SLIME_BALL) {
                if(stats.getOptin()){
                    stats.setStinklines(!stats.getStinklines());
                    player.sendMessage(stats.getMessing() ? "Stink Lines has been enabled." : "Stink Lines has been disabled.");
                    plugin.manageStinkEffects(player);
                    OpenSettings(player, plugin);
                }
            }
        }
        else if (event.getView().getTitle().contains("CareGiver Settings")) {
            ItemStack clickedItem = event.getCurrentItem();
            int currentPageNumber = currentPage.getOrDefault(playerUUID, 0);
            if (event.getCurrentItem().getType() == Material.RED_STAINED_GLASS_PANE) {
                if (currentPageNumber > 0) {
                    OpenCareGiverSettings(player, plugin, currentPageNumber - 1);
                }
                else if (currentPageNumber == 0) {
                    OpenSettings(player, plugin);
                }
            }else if (event.getCurrentItem().getType() == Material.GREEN_STAINED_GLASS_PANE) {
                OpenCareGiverSettings(player, plugin, currentPageNumber + 1);
            } else if (event.getCurrentItem().getType() == Material.BARRIER) {
                OpenSettings(player, plugin);
            }else if (event.getCurrentItem().getType() == Material.ENDER_EYE || event.getCurrentItem().getType() == Material.ENDER_PEARL) {
                stats.setAllCaregiver(!stats.getAllCaregiver());
                OpenCareGiverSettings(player, plugin, currentPageNumber);
            }else if (event.getCurrentItem().getType() == Material.REDSTONE_LAMP) {
                stats.setspecialCG(!stats.getspecialCG());
                OpenCareGiverSettings(player, plugin, currentPageNumber);
            } else if (clickedItem != null && clickedItem.getType().name().contains("PLAYER_HEAD")) {
                if (stats != null) {
                    String playerName = clickedItem.getItemMeta().getDisplayName();
                    // Get the UUID from the map
                    UUID targetUUID = playerHeadMap.get(playerName);

                    if (stats.isCaregiver(targetUUID)) {
                        stats.removeCaregiver(targetUUID);
                        player.sendMessage("You have removed " + playerName + " as a caregiver.");
                    } else {
                        stats.addCaregiver(targetUUID);
                        player.sendMessage("You have added " + playerName + " as a caregiver.");
                    }
                    OpenCareGiverSettings(player, plugin, currentPageNumber);
                }
            }
        }
        else if (event.getView().getTitle().equals("Incontinence Settings")) {
            if (event.getCurrentItem().getType() == Material.RED_STAINED_GLASS_PANE) {
                OpenSettings(player, plugin);
            }
            else if (stats.getHardcore()) {
                player.sendMessage("You are in " + ChatColor.RED + "Hardcore Mode" + ChatColor.WHITE + " ask a caregiver for help.");
                return;
            }
            else if (event.getCurrentItem().getType() == Material.SPONGE) {
                stats.setBladderLockIncon(!stats.getBladderLockIncon());
                IncontinenceSettings(player, plugin);
            }
            else if (event.getCurrentItem().getType() == Material.MAGMA_BLOCK) {
                stats.setBowelLockIncon(!stats.getBowelLockIncon());
                IncontinenceSettings(player, plugin);
            }
            else if (event.getCurrentItem().getType() == Material.WATER_BUCKET) {
                plugin.setAwaitingInput(player.getUniqueId(), "bladderincon");
                player.closeInventory(); // Optionally close the inventory
                player.sendMessage(ChatColor.YELLOW + "Please enter a new bladder Incontinence level:");
            }
            else if (event.getCurrentItem().getType() == Material.COCOA_BEANS) {
                plugin.setAwaitingInput(player.getUniqueId(), "bowelincon");
                player.closeInventory(); // Optionally close the inventory
                player.sendMessage(ChatColor.YELLOW + "Please enter a new bowel Incontinence level:");
            }
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
