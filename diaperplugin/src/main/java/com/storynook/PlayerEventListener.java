package com.storynook;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.storynook.Event_Listeners.PantsCrafting;
import com.storynook.items.pants;
import com.storynook.items.underwear;
import com.storynook.menus.IncontinenceMenu;
import com.storynook.menus.SettingsMenu;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.SoundCategory;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;


public class PlayerEventListener implements Listener {
    private static Plugin plugin;
    private Set<UUID> cooldown = new HashSet<>();
    HashMap<UUID, HashSet<NamespacedKey>> playerCraftedSpecialItems = new HashMap<>();
    HashMap<UUID, Double> distanceinBlocks = new HashMap<>();
    static HashMap<UUID, Boolean> Justchanged = new HashMap<>();
        
    public PlayerEventListener(Plugin plugin) {
        this.plugin = plugin;
    }
                //Loads the player stats when the login, and discovers all of the custom crafting recipes
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.loadPlayerStats(event.getPlayer()); //Uses the plugin instance to load player stats
        //Discover all of the custom crafting recipes

        String[] recipes = {
            "Diaper",
            "ThickDiaper", 
            "Pullup",
            "Underwear",
            "Tape",
            "CleanPants",
            "Toilet",
            "DiaperStuffer",
            "Washer"
        };
        
        for (String recipe : recipes) {
            event.getPlayer().discoverRecipe(new NamespacedKey(plugin, recipe));
        }
        // event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "DiaperPail"));
        // event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "Laxative"));
        // event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "Diuretic"));


        
        // plugin.manageParticleEffects(event.getPlayer());
    }
            
    //Updates stats and world events when the player leaves
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.getVehicle() instanceof ArmorStand) {
            player.leaveVehicle();
            ArmorStand armorStand = (ArmorStand) player.getVehicle();
            armorStand.remove();
        }
        plugin.savePlayerStats(event.getPlayer()); // Uses the plugin instance to save player stats
    }
        
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // Determine if the player is sprinting or walking normally
        if (player.isSprinting()) {
            plugin.activityMultiplier.put(playerUUID, 1.5); // Increase for sprinting
        } else {
            plugin.activityMultiplier.put(playerUUID, 1.0); // Normal walking or standing still
        }

        // Check for jumping specifically
        if (player.isOnGround() && player.getLocation().getY() > event.getFrom().getY()) {
            // The player location is ascending from the last event call and they were on the ground
            Double currentMultiplier = plugin.activityMultiplier.getOrDefault(playerUUID, 1.0);
            plugin.activityMultiplier.put(playerUUID, currentMultiplier + 0.5);
        }
    }
        
            
    //Handles the Player's consume event so they stay hydrated
    @EventHandler
    public void onPlayerDrink(PlayerItemConsumeEvent event) {
        PlayerStats stats = plugin.getPlayerStats(event.getPlayer().getUniqueId());
        if (stats != null) {
            ItemStack consumedItem = event.getItem();
            if(isHydrating(consumedItem)){
                // Increase hydration when the player drinks
                plugin.HydrationSpike.put(event.getPlayer().getUniqueId(), 10);
                stats.increaseHydration(40); 
            }
        }
    }
    private boolean isHydrating(ItemStack item){
        if (item.getType() == Material.POTION) {
            return true;
        }
        return false;
    }
            
    //Checks to see if the item used in crafting is a custom item. (ID based)
    private boolean isCustomItem(ItemStack item) {
        if (item == null) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasCustomModelData()) {
            return false;
        }
        int customModelData = item.getItemMeta().getCustomModelData();
        List<Integer> CustomItemIDs = Arrays.asList(626009, 626001, 626002, 626003, 626004, 626005, 626011, 626010);
        
        return CustomItemIDs.contains(customModelData);
    }
            
    private boolean NotVailidUnderwear (ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) {
            return false;
        }

        int customModelData = meta.getCustomModelData();
        List<Integer> CustomItemIDs = Arrays.asList(626005, 626011, 626010, 626004, 626007, 626008, 626006);

        return CustomItemIDs.contains(customModelData);
    }  
            
    @EventHandler
    public void onPlayerInteractWithEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {

            Player actor = event.getPlayer();
            Player target = (Player) event.getRightClicked();
        
            if (target instanceof Player){
                int rightclicktimes = plugin.rightclickCount.getOrDefault(actor.getUniqueId(), 0);
                PlayerStats targetStats = plugin.getPlayerStats(target.getUniqueId());
                ItemStack item = actor.getInventory().getItemInMainHand();
                if (targetStats != null && targetStats.getOptin()) {
                    if (targetStats != null && targetStats.isCaregiver(actor.getUniqueId())) {
                        if (item != null && item.getType() != Material.AIR) {
                            ItemMeta meta = item.getItemMeta();
                            if (meta != null && meta.hasCustomModelData()) {
                                int customModelData = item.getItemMeta().getCustomModelData();
                                if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) {
                                    return;
                                }
                                rightclicktimes++;
                                if (rightclicktimes > 1) {
                                    // rightclicktimes = 1;
                                    plugin.rightclickCount.put(actor.getUniqueId(), rightclicktimes);
                                    return;
                                }
                                else if (rightclicktimes == 1){
                                    plugin.firstimeran.put(actor.getUniqueId(), true);
                                    plugin.rightclickCount.put(actor.getUniqueId(), rightclicktimes);
                                    playAudio(actor, customModelData, targetStats.getUnderwearType());
                                    handleRightClickHold(actor, target, true, customModelData, targetStats.getUnderwearType());
                                }
                                else {
                                    return;
                                }
                            }
                        }
                        if (item != null && item.getType() == Material.AIR && actor.isSneaking()) {
                            rightclicktimes++;
                            if (rightclicktimes > 1) {
                                // rightclicktimes = 1;
                                plugin.rightclickCount.put(actor.getUniqueId(), rightclicktimes);
                                return;
                            }
                            else if (rightclicktimes == 1){
                                plugin.firstimeran.put(actor.getUniqueId(), true);
                                plugin.rightclickCount.put(actor.getUniqueId(), rightclicktimes);
                                plugin.CheckLittles(actor, targetStats, target);
                            }
                            else {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
            
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player actor = event.getPlayer();
        PlayerStats Stats = plugin.getPlayerStats(actor.getUniqueId());
        ItemStack itemInHand = actor.getInventory().getItemInMainHand();
        if (itemInHand != null && itemInHand.getType() != Material.AIR) {
            ItemMeta meta = itemInHand.getItemMeta();
            if (meta != null && meta.hasCustomModelData()) {
                int rightclicktimes = plugin.rightclickCount.getOrDefault(actor.getUniqueId(), 0);
                int customModelData = meta.getCustomModelData();
                if (customModelData == 626007) {
                    event.setCancelled(true); // Cancel the interaction
                    if (Stats.getOptin() && Stats.getLayers() < 4) {
                        int maxLayers = 0;
                        switch(Stats.getUnderwearType()) {
                            case 0: maxLayers = 1; break;
                            case 1: maxLayers = 2; break;
                            case 2: maxLayers = 3; break;
                            case 3: maxLayers = 4; break;
                            default: return;
                        }
                        if (Stats.getLayers() >= maxLayers) {
                            actor.sendMessage(ChatColor.RED + "You cannot add more layers with your current underwear.");
                            return;
                        }
                        if (Stats.getDiaperWetness() >= 100) {
                            actor.sendMessage(ChatColor.RED + "It's a little too late for that, don't you think?");
                            return;
                        }
                        Stats.setLayers(Stats.getLayers() + 1);
                        actor.sendMessage(ChatColor.GREEN + "Added a layer! Current layers: " + Stats.getLayers());

                        if (itemInHand.getAmount() > 1) {
                            itemInHand.setAmount(itemInHand.getAmount() - 1);
                        } else {
                            actor.getInventory().setItemInMainHand(null);
                        }
                    
                        // Apply cooldown (5 seconds)
                        cooldown.add(actor.getUniqueId());
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> cooldown.remove(actor.getUniqueId()), 20 * 5);
                    }
                    return; // Exit the method to prevent further execution
                }
                else if(isCustomItem(itemInHand) && !NotVailidUnderwear(itemInHand)){
                    if (event.getAction().name().contains("RIGHT_CLICK")) {
                        if (itemInHand == null || !itemInHand.hasItemMeta() || !itemInHand.getItemMeta().hasCustomModelData()) {
                            plugin.rightclickCount.put(actor.getUniqueId(), 0);
                            plugin.firstimeran.put(actor.getUniqueId(), false);
                            return;
                        } else if (Stats.getHardcore()) {
                            actor.sendMessage("You are in HardCore mode. You should ask a caregiver for help.");
                            return;
                        }
                        else if (!Stats.getOptin()) {
                            return;
                        }
                        else {
                            rightclicktimes++;
                            if (rightclicktimes > 1) {
                                plugin.rightclickCount.put(actor.getUniqueId(), rightclicktimes);
                                return;
                            }
                            else if (rightclicktimes == 1){
                                plugin.firstimeran.put(actor.getUniqueId(), true);
                                plugin.rightclickCount.put(actor.getUniqueId(), rightclicktimes);
                                playAudio(actor, customModelData, Stats.getUnderwearType());
                                handleRightClickHold(actor, null, false, customModelData, Stats.getUnderwearType());
                            }
                            else {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
            
    private void handleRightClickHold(Player actor, Player target, boolean isCaregiverInteraction, int totype, int fromtype) {

        if (plugin.rightclickCount.get(actor.getUniqueId()) > 0 && plugin.firstimeran.get(actor.getUniqueId())) {
            plugin.firstimeran.put(actor.getUniqueId(), false);
            BossBar bossBar = Bukkit.createBossBar(ChatColor.GREEN + "Changing", BarColor.BLUE, BarStyle.SOLID);
            bossBar.addPlayer(actor);
            bossBar.setProgress(0.0); // Start with progress 0 (empty)

            int timeLeft = 5; // Time in seconds

            BukkitRunnable task = new BukkitRunnable() {
                private int ticksLeft = 20 * timeLeft; // Convert seconds to ticks
                @Override
                public void run() {
                    if (isCaregiverInteraction && target != null) {
                        Player caregiver = (Player) actor;
                        Location senderLocation = caregiver.getLocation();
                        Location targetLocation = target.getLocation();
                
                        // Check the distance between the sender and the target player
                        double distance = senderLocation.distance(targetLocation);
                        distanceinBlocks.put(actor.getUniqueId(), distance);
                    }
                    if (ticksLeft <= 0) {
                        bossBar.removePlayer(actor);
                        handleInteraction(actor, target, isCaregiverInteraction);
                        stopAudio(actor, totype, fromtype);
                        plugin.rightclickCount.put(actor.getUniqueId(), 0);
                        this.cancel();
                    } else {
                        ItemStack item = actor.getInventory().getItemInMainHand();
                        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasCustomModelData() || (isCaregiverInteraction && distanceinBlocks.get(actor.getUniqueId()) > 3)) {
                            plugin.rightclickCount.put(actor.getUniqueId(), 0);
                            bossBar.removePlayer(actor);
                            stopAudio(actor,totype, fromtype);
                            this.cancel();
                        }
                        ticksLeft--;
                        double progress = (double) ticksLeft / (20 * timeLeft);
                        bossBar.setProgress(progress);
                    }
                }
            };

            task.runTaskTimer(plugin, 0L, 1L); // Run every tick
        }
    }

    private void playAudio(Player player, int totype, int fromtype) {
        if ((totype == 626002 && fromtype != 0) || fromtype != 0) {
            player.playSound(player.getLocation(), "minecraft:diaperchange", SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
    }

    private void stopAudio(Player player, int totype, int fromtype) {
        if ((totype == 626002 && fromtype != 0) || fromtype !=0) {
            player.stopSound("minecraft:diaperchange", SoundCategory.PLAYERS);
        }
    }
    private void handleInteraction(Player actor, Player target, boolean isCaregiverInteraction) {
        if (!isCaregiverInteraction) {
            target = actor;
        }
        PlayerStats stats = plugin.getPlayerStats(target.getUniqueId());
        ItemStack item = actor.getInventory().getItemInMainHand();

        if (isCustomItem(item) && !NotVailidUnderwear(item)) {
            int customModelData = item.getItemMeta().getCustomModelData();
            
            // Remove or decrement the item the actor is holding
            decrementItem(actor, item);
            // Logic to provide items based on wetness and fullness
            distributeUsedItems(actor, target, stats);

            // Reset and update
            resetAndUpdateStats(stats, customModelData, target);

            if (actor == target) {
                actor.sendMessage(ChatColor.GREEN + "You got changed and cleaned!");
            }
            else if(actor != target){
                actor.sendMessage(ChatColor.GREEN + "You changed: " + target.getName());
                target.sendMessage(ChatColor.GREEN + "You were changed by: " + actor.getName() + " Be sure to thank them!");
            }
        } else{
            if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) {
                return;
            }
        }
    }
            
    private void distributeUsedItems(Player actor, Player target, PlayerStats stats) {
        // Distribute items according to the target's diaper status

        if(stats.getDiaperFullness() > 0 && stats.getUnderwearType() > 0){ItemStack stinkydiaper = underwear.createStinkyDiaper(target, (int)stats.getDiaperWetness(),(int)stats.getDiaperFullness(),stats.getTimeWorn());actor.getInventory().addItem(stinkydiaper);}
        else if(stats.getDiaperWetness() > 0 && stats.getUnderwearType() == 1){ItemStack wetpullup = underwear.createWetPullup(target, (int)stats.getDiaperWetness(),(int)stats.getDiaperFullness(),stats.getTimeWorn()); actor.getInventory().addItem(wetpullup);}
        else if(stats.getDiaperWetness() > 0 && stats.getUnderwearType() == 2){ItemStack wetdiaper = underwear.createWetDiaper(target, (int)stats.getDiaperWetness(),(int)stats.getDiaperFullness(),stats.getTimeWorn());actor.getInventory().addItem(wetdiaper);}
        else if(stats.getDiaperWetness() > 0 && stats.getUnderwearType() == 3){ItemStack wetthickdiaper = underwear.createWetThickDiaper(target, (int)stats.getDiaperWetness(),(int)stats.getDiaperFullness(),stats.getTimeWorn());actor.getInventory().addItem(wetthickdiaper);}
        else if (stats.getDiaperWetness() >= 100 && stats.getDiaperFullness() >= 100){
            ItemStack wetANDdirtyunderwear = underwear.createWetANDDirtyUndies(target, (int)stats.getDiaperWetness(),(int)stats.getDiaperFullness(),stats.getTimeWorn());actor.getInventory().addItem(wetANDdirtyunderwear);
        }
        else if(stats.getDiaperWetness() >= 100 && stats.getUnderwearType() == 0){ItemStack wetunderwear = underwear.createWetUndies(target, (int)stats.getDiaperWetness(),(int)stats.getDiaperFullness(),stats.getTimeWorn());actor.getInventory().addItem(wetunderwear);}
        else if(stats.getDiaperFullness() >= 100 && stats.getUnderwearType() == 0){ItemStack dirtyunderwear = underwear.createDirtyUndies(target, (int)stats.getDiaperWetness(),(int)stats.getDiaperFullness(),stats.getTimeWorn());actor.getInventory().addItem(dirtyunderwear);}
        else if (stats.getDiaperFullness() == 0 && stats.getDiaperWetness() == 0) {
            switch (stats.getUnderwearType()) {
                case 0:
                    actor.getInventory().addItem(underwear.Underwear());
                    break;
                case 1:
                    actor.getInventory().addItem(underwear.Pullup());
                    break;
                case 2:
                    actor.getInventory().addItem(underwear.Diaper());
                    break;
                case 3:
                    actor.getInventory().addItem(underwear.ThickDiaper());
                    break;
                default:
                    break;
            }
        }
    }
            
    private void decrementItem(Player actor, ItemStack item) {
        // Decrement or remove the item from the actor's inventory

        int currentAmount = item.getAmount();
        if (currentAmount > 1) {
            item.setAmount(currentAmount - 1);
        } else {
            actor.getInventory().setItemInMainHand(null);
        }
    }

    private void resetAndUpdateStats(PlayerStats stats, int customModelData, Player target) {
        // Reset stats and set new underwear type
        stats.setDiaperFullness(0);
        stats.setDiaperWetness(0);
        stats.setTimeWorn(0);
        stats.setLayers(0);
        if (customModelData == 626002) {stats.setUnderwearType(0);} //Underwear
        if (customModelData == 626003) {stats.setUnderwearType(1);} //Pullup
        if (customModelData == 626009) {stats.setUnderwearType(2);} //Diaper
        if (customModelData == 626001) {stats.setUnderwearType(3);} //Thick Diaper
        Justchanged.put(target.getUniqueId(), true);
        PantsCrafting.equipDiaperArmor(target, true, false);
        plugin.manageParticleEffects(target);
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        ItemStack[] matrix = inventory.getMatrix();

        boolean blockCrafting = false;
        boolean hasLaxative = false;
        ItemStack foodItem = null;
        Material foodItemType = null;
        int foodItemCount = 0;

        if (event.getRecipe() == null || event.getInventory() == null) return;

            ItemStack result = event.getRecipe().getResult();
            if (result.getType() != Material.LEATHER_LEGGINGS) return;

            Material woolColor = null;

            // Check wool colors in the grid
            for (ItemStack item : matrix) {
                if (item != null && item.getType() == Material.LEATHER) {
                    return;
                }
                else if (item != null && item.getType().toString().endsWith("_WOOL")) {
                    if (woolColor == null) {
                        woolColor = item.getType(); // Set initial color
                    } else if (!woolColor.equals(item.getType())) {
                        event.getInventory().setResult(new ItemStack(Material.AIR)); // Mismatched colors cancel craft
                        return;
                    }
                }
            }

            if (woolColor != null) {

                Color color = pants.getColorFromWool(woolColor);
                if (color == null) {
                    // Cancel crafting if color mapping fails
                    event.getInventory().setResult(new ItemStack(Material.AIR));
                    return;
                }
                event.getInventory().setResult(pants.createPants(woolColor));
            }
            else {
                event.getInventory().setResult(new ItemStack(Material.AIR)); // Ensure no item is crafted if no wool is found
            }

        for (ItemStack item : matrix) {
            if (item == null) continue;

            if (isCustomItem(item)) {
                blockCrafting = true; // Only block crafting if it's not intended
            }

            // if (isLaxative(item)) {
            //     hasLaxative = true;
            // } else if (item.getType().isEdible()) {
            //     foodItemCount++;
            //     if (foodItemType == null) {
            //         foodItemType = item.getType();
            //         foodItem = item;
            //     } else if (foodItemType != item.getType()){
            //         inventory.setResult(null);
            //         return;
            //     }
            // }
        }
        if (blockCrafting) {
            inventory.setResult(null); // Cancel crafting if custom items are involved inappropriately
        }
        // else if (hasLaxative && foodItem != null && foodItemCount == 1) {
        //     // Logic for creating laxative-imbued food items
        //     ItemStack lacedResult = foodItem.clone();
        //     ItemMeta meta = lacedResult.getItemMeta();
        //     if (meta != null) {
        //         meta.getPersistentDataContainer().set(
        //             new NamespacedKey(plugin, "laxative_effect"),
        //             PersistentDataType.BYTE,
        //             (byte) 1
        //         );
        //         meta.setCustomModelData(626100);
        //         lacedResult.setItemMeta(meta);
        //     }
        //     lacedResult.setAmount(1);
        //     inventory.setResult(lacedResult);
        // }
        else {
            // inventory.setResult(result); // Make sure other recipes can work
        }
    }

    // private void clearCraftingGrid(CraftingInventory inventory) {
    //     ItemStack[] emptyMatrix = new ItemStack[inventory.getSize()];
    //     inventory.setMatrix(emptyMatrix);
    // }

    // @EventHandler
    // public void onCraftItem(CraftItemEvent event) {
    //     if (!(event.getWhoClicked() instanceof Player)) return;
    //     Player player = (Player) event.getWhoClicked();
    //     CraftingInventory inventory = (CraftingInventory) event.getInventory();

    //     ItemStack result = event.getRecipe().getResult();

    //     if (result.hasItemMeta()) {
    //         ItemMeta meta = result.getItemMeta();
    //         int customModelData = result.getItemMeta().getCustomModelData();
    //         if (meta != null && (meta.getPersistentDataContainer().has(
    //             new NamespacedKey(plugin, "laxative_effect"),
    //             PersistentDataType.BYTE
    //         ) || customModelData == 626100)) {
    //             clearCraftingGrid(inventory);
    //         }
    //     }

    //     // // Cancel the default crafting to handle it manually
    //     // event.setCancelled(true);

    //     // // Prepare to decrement items
    //     // ItemStack[] matrix = inventory.getMatrix();
    //     // boolean foodReduced = false;
    //     // boolean laxativeReduced = false;

    //     // for (int i = 0; i < matrix.length; i++) {
    //     //     ItemStack item = matrix[i];
    //     //     if (item == null) continue;

    //     //     if (isLaxative(item) && !laxativeReduced) {
    //     //         item.setAmount(item.getAmount() - 1);
    //     //         if (item.getAmount() <= 0) matrix[i] = null;
    //     //         laxativeReduced = true;
    //     //     } else if (item.getType().isEdible() && !foodReduced) {
    //     //         item.setAmount(item.getAmount() - 1);
    //     //         if (item.getAmount() <= 0) matrix[i] = null;
    //     //         foodReduced = true;
    //     //     }

    //     //     if (laxativeReduced && foodReduced) break;
    //     // }

    //     // // Update the crafting matrix after a small delay to avoid conflicts
    //     // Bukkit.getScheduler().runTask(plugin, () -> inventory.setMatrix(matrix));

    //     // // Add the result to the player's inventory
    //     // if (event.isShiftClick()) {
    //     //     HashMap<Integer, ItemStack> excess = player.getInventory().addItem(result.clone());
    //     //     // Drop excess items if inventory is full
    //     //     for (ItemStack leftover : excess.values()) {
    //     //         player.getWorld().dropItemNaturally(player.getLocation(), leftover);
    //     //     }
    //     // } else {
    //     //     player.setItemOnCursor(result.clone());
    //     // }
    // }

    






    // private boolean isLaxative(ItemStack item) {
    //     if (item == null || !item.hasItemMeta()) return false;
    //     ItemMeta meta = item.getItemMeta();
    //     return meta.hasCustomModelData() && meta.getCustomModelData() == 626012;
    // }

    // @EventHandler
    // public void onItemPickup(EntityPickupItemEvent  event) {
    //     if (!(event.getEntity() instanceof Player)) {
    //         return; // Ensure that the entity is a player
    //     }
    
    //     Player player = (Player) event.getEntity();
    //     ItemStack item = event.getItem().getItemStack();
    //     ItemMeta meta = item.getItemMeta();
    
    //     if (meta != null && meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "laxative_effect"), PersistentDataType.BYTE)) {
    //         UUID playerUUID = player.getUniqueId();
    //         playerCraftedSpecialItems.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(new NamespacedKey(plugin, Integer.toString(item.getItemMeta().getCustomModelData())));
    //     }
    // }

    // @EventHandler
    // public void onInventoryClick(InventoryClickEvent event) {
    //     if (event.isShiftClick()) {
    //         // Add same logic check to avoid duplications
    //         ItemStack currentItem = event.getCurrentItem();
    //         if (currentItem != null && currentItem.hasItemMeta()) {
    //             ItemMeta meta = currentItem.getItemMeta();
    //             if (meta != null && meta.getPersistentDataContainer().has(
    //                 new NamespacedKey(plugin, "laxative_effect"),
    //                 PersistentDataType.BYTE
    //             )) {
    //                 // log or debug; manage how items are handled further.
    //             }
    //         }
    //     }
    //     if (!(event.getWhoClicked() instanceof Player)) return;

    //     Player player = (Player) event.getWhoClicked();
    //     ItemStack item = event.getCurrentItem();

    //     if (item != null && item.hasItemMeta()) {
    //         ItemMeta meta = item.getItemMeta();
    //         if (meta != null && meta.getPersistentDataContainer().has(
    //             new NamespacedKey(plugin, "laxative_effect"),
    //             PersistentDataType.BYTE
    //         )) {
    //             // Add custom lore visible only to this player
    //             updateLoreForPlayer(item, player);
    //         }
    //     }
    // }

    // private void updateLoreForPlayer(ItemStack item, Player player) {
    //     if (item == null || !item.hasItemMeta()) return;
    
    //     ItemMeta meta = item.getItemMeta();
    //     NamespacedKey laxativeKey = new NamespacedKey(plugin, "laxative_effect");
    //     if (meta.getPersistentDataContainer().has(laxativeKey, PersistentDataType.BYTE)) {
    //         HashSet<NamespacedKey> craftedItems = playerCraftedSpecialItems.get(player.getUniqueId());
    //         if (craftedItems != null && craftedItems.contains(item.getItemMeta().getCustomModelData())) {
    //             meta.setLore(Collections.singletonList(ChatColor.RED + "Contains a laxative effect"));
    //         } else {
    //             meta.setLore(null);
    //         }
    //         item.setItemMeta(meta);
    //     }
    // }

    // @EventHandler
    // public void onPlayerConsume(PlayerItemConsumeEvent event) {
    //     Player player = event.getPlayer();
    //     ItemStack item = event.getItem();

    //     if (item.hasItemMeta()) {
    //         ItemMeta meta = item.getItemMeta();
    //         if (meta != null && meta.getPersistentDataContainer().has(
    //             new NamespacedKey(plugin, "laxative_effect"),
    //             PersistentDataType.BYTE
    //         )) {
    //             // Apply laxative effect
    //             PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());
    //             if(stats.getMessing()){
    //                 if (stats.getEffectDuration() == 0) {
    //                     stats.setBowelFillRate(stats.getBowelFillRate() * 4);
    //                 }
    //                 stats.increaseEffectDuration(30);
    //             }
    //         }
    //     }
    // }

    // @EventHandler
    // public void onPotionEffect(EntityPotionEffectEvent event) {
    //     if (event.getEntity() instanceof Player) {
    //         Player player = (Player) event.getEntity();
    //         PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());

    //         if (stats != null && event.getNewEffect() != null) {
    //             PotionEffectType effectType = event.getNewEffect().getType();
                
    //             if (effectType.equals(PotionEffectType.SPEED)) {
    //                 int amplifier = event.getNewEffect().getAmplifier();
    //                 stats.setBladderFillRate(1 + (int) (0.2 * (amplifier + 1))); // Increase Bladder and bowel fill rate because of speed potion
    //                 stats.setBowelFillRate(1 + (int) (0.2 * (amplifier + 1)));
    //             }
    //         }
    //     }
    // }

    // @EventHandler
    // public void onInventoryClick(InventoryClickEvent event) {
    //     Inventory clickedInventory = event.getClickedInventory();
    //     ItemStack currentItem = event.getCurrentItem();

    //     // Null check
    //     if (clickedInventory == null || currentItem == null || currentItem.getType() == Material.AIR) {
    //         return;
    //     }

    //     // Allow item removal from the player's inventory
    //     if (clickedInventory.equals(event.getView().getBottomInventory())) {
    //         return;
    //     }

    //     // Check if the item is one of your custom items
    //     if (isUsed(currentItem)) {
    //         boolean isAllowedInventory = isDiaperPailInventory(clickedInventory);

    //         // If the inventory is not a diaper pail, cancel the event
    //         if (!isAllowedInventory) {
    //             event.setCancelled(true);
    //         }
    //     }
    // }

    // @EventHandler
    // public void onInventoryDrag(InventoryDragEvent event) {
    //     Inventory destinationInventory = event.getInventory();

    //     // Ensure the player isn't interacting with their own inventory
    //     if (!destinationInventory.equals(event.getView().getBottomInventory())) {
    //         for (Integer slot : event.getRawSlots()) {
    //             // Check if the slot belongs to the clicked inventory
    //             if (slot < event.getView().getTopInventory().getSize()) {
    //                 ItemStack item = event.getNewItems().get(slot);
    //                 if (isUsed(item) && !isDiaperPailInventory(destinationInventory)) {
    //                     event.setCancelled(true);
    //                     break;
    //                 }
    //             }
    //         }
    //     }
    // }

    // // Ensure the inventory is recognized as a diaperpail
    // private boolean isDiaperPailInventory(Inventory inventory, InventoryClickEvent event) {
    //     return inventory != null && "Diaper Pail".equals(inventory.getName());
    // }
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (plugin.isAwaitingInput(playerUUID)) {
            event.setCancelled(true); // Prevents the chat from showing to other players
            String inputType = plugin.getAwaitingInputType(playerUUID);
            String message = event.getMessage().trim();
            try {
                if ("minFill".equals(inputType)) {
                    int newMinFill = Integer.parseInt(message);
                    PlayerStats stats = plugin.getPlayerStats(playerUUID);
                    stats.setMinFill(newMinFill);
                    player.sendMessage(ChatColor.GREEN + "Minimum fill set to: " + newMinFill);
                    plugin.clearAwaitingInput(playerUUID);
                    Bukkit.getScheduler().runTask(plugin, () -> SettingsMenu.OpenSettings(player, plugin));
                }
                else if ("bladderincon".equals(inputType)){
                    int newBladderincon = Integer.parseInt(message);
                    PlayerStats stats = plugin.getPlayerStats(playerUUID);
                    stats.setBladderIncontinence(newBladderincon);
                    player.sendMessage(ChatColor.GREEN + "Bladder Incontinence set to: " + newBladderincon);
                    plugin.clearAwaitingInput(playerUUID);
                    Bukkit.getScheduler().runTask(plugin, () -> IncontinenceMenu.IncontinenceSettings(player, plugin));
                }else if ("bowelincon".equals(inputType)){
                    int newBowelincon = Integer.parseInt(message);
                    PlayerStats stats = plugin.getPlayerStats(playerUUID);
                    stats.setBowelIncontinence(newBowelincon);
                    player.sendMessage(ChatColor.GREEN + "Bowel Incontinence set to: " + newBowelincon);
                    plugin.clearAwaitingInput(playerUUID);
                    Bukkit.getScheduler().runTask(plugin, () ->  IncontinenceMenu.IncontinenceSettings(player, plugin));
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid number. Please enter a valid number.");
                Bukkit.getScheduler().runTask(plugin, () -> SettingsMenu.OpenSettings(player, plugin));
            }
        }
    }
}

