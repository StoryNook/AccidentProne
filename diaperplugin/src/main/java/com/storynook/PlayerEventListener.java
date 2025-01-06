package com.storynook;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.storynook.items.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;


public class PlayerEventListener implements Listener {
    private Plugin plugin;
    HashMap<UUID, HashSet<NamespacedKey>> playerCraftedSpecialItems = new HashMap<>();
    // HashMap<UUID, Integer> rightclickCount = new HashMap<>();
    HashMap<UUID, Double> distanceinBlocks = new HashMap<>();
    // HashMap<UUID, Boolean> firstimeran = new HashMap<>();

    public PlayerEventListener(Plugin plugin) {
        this.plugin = plugin;
    }
    //Loads the player stats when the login, and discovers all of the custom crafting recipes
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.loadPlayerStats(event.getPlayer()); //Uses the plugin instance to load player stats
        //Discover all of the custom crafting recipes
        event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "Diaper"));
        event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "ThickDiaper"));
        event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "Pullup"));
        event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "Underwear"));
        event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "Tape"));
        // event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "DiaperPail"));
        event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "Toilet"));
        event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "DiaperStuffer"));
        // event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "Washer"));
        // event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "Laxative"));
        // event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "Diuretic"));


        
        // plugin.manageStinkEffects(event.getPlayer());
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
    // @EventHandler
    // public void onPlayerSneak(PlayerToggleSneakEvent event){
    //     Player player = event.getPlayer();
    //     plugin.setPlayerSneakStatus(player.getUniqueId(), event.isSneaking());
    // }

    //Handles the Player's consume event so they stay hydrated
    @EventHandler
    public void onPlayerDrink(PlayerItemConsumeEvent event) {
        PlayerStats stats = plugin.getPlayerStats(event.getPlayer().getUniqueId());
        if (stats != null) {
            ItemStack consumedItem = event.getItem();
            if(isHydrating(consumedItem)){
                // Increase hydration when the player drinks
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
                                plugin.CheckLittles(actor, targetStats);
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
        ItemStack itemInHand = actor.getInventory().getItemInMainHand();
        if (itemInHand != null && itemInHand.getType() != Material.AIR) {
            ItemMeta meta = itemInHand.getItemMeta();
            if (meta != null && meta.hasCustomModelData()) {
                int rightclicktimes = plugin.rightclickCount.getOrDefault(actor.getUniqueId(), 0);
                int customModelData = meta.getCustomModelData();
                if (customModelData == 626007) {
                    event.setCancelled(true); // Cancel the interaction
                    return; // Exit the method to prevent further execution
                }
                else if(isCustomItem(itemInHand) && !NotVailidUnderwear(itemInHand)){
                    if (event.getAction().name().contains("RIGHT_CLICK")) {
                        PlayerStats Stats = plugin.getPlayerStats(actor.getUniqueId());
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
                                // rightclicktimes = 1;
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

            // Logic to provide items based on wetness and fullness
            distributeUsedItems(actor, stats);

            // Reset and update
            resetAndUpdateStats(stats, customModelData);

            // Remove or decrement the item the actor is holding
            decrementItem(actor, item);

            // Feedback
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

    private void distributeUsedItems(Player actor, PlayerStats stats) {
        // Distribute items according to the target's diaper status
        // Similar to the already implemented logic

        if(stats.getDiaperFullness() > 0 && stats.getUnderwearType() > 0){actor.getInventory().addItem(ItemManager.stinkydiaper);}
        else if(stats.getDiaperWetness() > 0 && stats.getUnderwearType() == 1){actor.getInventory().addItem(ItemManager.wetpullup);}
        else if(stats.getDiaperWetness() > 0 && stats.getUnderwearType() == 2){actor.getInventory().addItem(ItemManager.wetdiaper);}
        else if(stats.getDiaperWetness() > 0 && stats.getUnderwearType() == 3){actor.getInventory().addItem(ItemManager.wetthickdiaper);}
    }

    private void resetAndUpdateStats(PlayerStats stats, int customModelData) {
        // Reset stats and set new underwear type
        // Similar to the already implemented logic
        stats.setDiaperFullness(0);
        stats.setDiaperWetness(0);
        stats.setTimeWorn(0);
        if (customModelData == 626002) {stats.setUnderwearType(0);} //Underwear
        if (customModelData == 626003) {stats.setUnderwearType(1);} //Pullup
        if (customModelData == 626009) {stats.setUnderwearType(2);} //Diaper
        if (customModelData == 626001) {stats.setUnderwearType(3);} //Thick Diaper
    }

    private void decrementItem(Player actor, ItemStack item) {
        // Decrement or remove the item from the actor's inventory
        // Similar to the already implemented logic

        int currentAmount = item.getAmount();
        if (currentAmount > 1) {
            item.setAmount(currentAmount - 1); // Decrease the amount by 1
        } else {
            actor.getInventory().setItemInMainHand(null); // Remove the item entirely if only 1 remains
        }
    }

    //When weather changes, checks for thunder
    @EventHandler
    public void onLightningStrike(LightningStrikeEvent event) {
        Location strikeLocation = event.getLightning().getLocation();
        
        // Iterate through all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isNearLightningInWorld(player.getLocation(), strikeLocation, player.getWorld())) {
                handleThunderEffect(player);
            }
        }
    }
    private boolean isNearLightningInWorld(Location location, Location lightningLocation, World world) {
        // First check if they are in the same world
        if (!location.getWorld().equals(world)) {
            return false;
        }
        
        // Check distance between the two locations within the specified radius (50 blocks)
        return location.distance(lightningLocation) <= 50;
    }

    // Handle an accident if the player is near thunder
    private void handleThunderEffect(Player player) {
        PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());
        Random random = new Random();
        if (stats != null && stats.getOptin()) {
            double maxIncontinence = Math.max(stats.getBladderIncontinence(), stats.getBowelIncontinence());
            double chance = Math.min(4, Math.max(0, maxIncontinence / 2));// 1 in 4 chance of having an accident
            if (random.nextInt(4) < chance) {
                boolean bladderAccident = stats.getBladderIncontinence() >= stats.getBowelIncontinence();
                if (bladderAccident ? stats.getBladder() > 0 : stats.getBowels() > 0) {
                    stats.handleAccident(bladderAccident, player, false);
                    player.sendMessage("You got so scared by the lightening that you had an accident!");
                }
                return;
            }
        }
    }
    //Mob attack envirorment check
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Entity damager = event.getDamager();

        // Check if it is night or the player is in a dark place
        boolean isNight = player.getWorld().getTime() > 12300 && player.getWorld().getTime() < 23850;
        int lightLevel = player.getLocation().getBlock().getLightLevel();
        boolean isDark = lightLevel < 7;

        if ((isNight || isDark) && damager instanceof Mob) {
            // Check if the mob is not on the player's screen
            Vector toEntity = damager.getLocation().toVector().subtract(player.getLocation().toVector());
            Vector direction = player.getLocation().getDirection();

            // Check if mob is outside field of view (Use a dot product for a simple "in-front" check)
            double fieldOfView = 0.5; // Adjust for larger FOV
            if (direction.dot(toEntity.normalize()) < fieldOfView) {
                handleScaryEvent(player);
            }
        }
    }
    //Mob Attack chance
    private void handleScaryEvent(Player player) {
        PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());
        Random random = new Random();
        if (stats != null && stats.getOptin()) {
            double maxIncontinence = Math.max(stats.getBladderIncontinence(), stats.getBowelIncontinence());
            double chance = Math.min(4, Math.max(0, maxIncontinence / 2));

            if (random.nextInt(4) < chance) {
                boolean bladderAccident = stats.getBladderIncontinence() >= stats.getBowelIncontinence();
                stats.handleAccident(bladderAccident, player, false);
                player.sendMessage("You got so scared by the attack that you had an accident!");
            }
        }
    }
    //Bedwetting chance
    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        // Check if world time indicates it's daytime (usually around 0 to 1,000 ticks in Minecraft)
        long worldTime = player.getWorld().getTime();
        if (worldTime >= 0 && worldTime < 1000) {
            PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());
            if (stats != null && stats.getOptin()) {
                
                if (stats.getBedwetting() == 1) {
                    double chance = Math.min(4, Math.max(0, stats.getBladderIncontinence() / 2));
                    Random random = new Random();
                    if (random.nextInt(4) < chance) {
                        if (stats.getBladder() > 10) {
                            stats.handleAccident(true, player,false);
                            player.sendMessage("Oh no! You wet the bed!");
                            return;
                        }
                    }
                }
                else if(stats.getBedwetting() == 2){
                    if (stats.getBladder() > 10) {
                        stats.handleAccident(true, player,false);
                        player.sendMessage("Oh no! You wet the bed!");
                        return;
                    }
                }
                stats.increaseBladder(40);
                if(stats.getMessing()){stats.increaseBowels(20);}
            }
        }
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
    // public void onWasherPlace(BlockPlaceEvent event) {
    //     ItemStack item = event.getItemInHand();
    
    //     if (item.hasItemMeta() && item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 626014) {
    //         // Ensure this is the washing machine block
    //         // event.setCancelled(true);  // Cancel the block placement
    //         // Location loc = event.getBlockPlaced().getLocation();
    //         // World world = loc.getWorld();
    //         Block block = event.getBlockPlaced();
    //         BlockState blockstate = block.getState();
    //         if (blockstate instanceof PersistentDataHolder) {
    //             PersistentDataContainer data = ((PersistentDataHolder) blockstate).getPersistentDataContainer();
    //             NamespacedKey key = new NamespacedKey(plugin, "washer");
    //             data.set(key, PersistentDataType.INTEGER, 1);
    //             blockstate.update();  // Important to invoke this to apply changes
    //         }
    //     }
    // }



    //Places custom item toilet
    @EventHandler
    public void onPlaceToilet(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item.hasItemMeta() && item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 626006) {

            // Get the block where the player is trying to place the toilet
            Block block = event.getBlockPlaced();
            Location loc = block.getLocation();

            // Set the cauldron at the block location
            block.setType(Material.CAULDRON);

            // Place a trapdoor on top
            Block trapdoorBlock = loc.clone().add(0, 1, 0).getBlock();
            trapdoorBlock.setType(Material.IRON_TRAPDOOR);

            // Set the trapdoor to a specific orientation (optional)
            TrapDoor trapdoor = (TrapDoor) trapdoorBlock.getBlockData();
            trapdoor.setHalf(Bisected.Half.BOTTOM); // Make the trapdoor flush with the cauldron
            BlockFace playerDirection = event.getPlayer().getFacing();
            switch (playerDirection) {
                case NORTH:
                    trapdoor.setFacing(BlockFace.SOUTH);
                    break;
                case EAST:
                    trapdoor.setFacing(BlockFace.WEST);
                    break;
                case SOUTH:
                    trapdoor.setFacing(BlockFace.NORTH);
                    break;
                case WEST:
                    trapdoor.setFacing(BlockFace.EAST);
                    break;
                default:
                    // Handle other cases if needed
                    break;
            }
            // trapdoor.setFacing(BlockFace.NORTH); // Set the trapdoor's facing direction
            trapdoor.setOpen(true);
            trapdoorBlock.setBlockData(trapdoor);
        }
    }

    
    //Toilet interaction
    @EventHandler
    public void onToiletInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null) {
                // Check if the clicked block is a cauldron or the trapdoor above it
                if (block.getType() == Material.CAULDRON) {
                    processCauldronInteraction(block, event.getPlayer());
                } else if (block.getType() == Material.IRON_TRAPDOOR) {
                    Block belowBlock = block.getLocation().subtract(0, 1, 0).getBlock();
                    if (belowBlock.getType() == Material.CAULDRON) {
                        // toggleTrapdoor(block);
                    }
                }
            }
        }
    }
    //Confrim Toilet
    private void processCauldronInteraction(Block cauldronBlock, Player player) {
        Block trapdoorBlock = cauldronBlock.getLocation().add(0, 1, 0).getBlock();
        if (trapdoorBlock.getType() == Material.IRON_TRAPDOOR) {
            // Existing code to make the player interact with the cauldron toilet
            interactWithCauldron(player, cauldronBlock, trapdoorBlock);
        }
    }

    // private void toggleTrapdoor(Block trapdoorBlock) {
    //     TrapDoor trapdoor = (TrapDoor) trapdoorBlock.getBlockData();
    //     trapdoor.setOpen(!trapdoor.isOpen());
    //     trapdoorBlock.setBlockData(trapdoor);
    // }

    //Using Toilet Action
    private void interactWithCauldron(Player player, Block cauldronBlock, Block trapdoorBlock) {
        // Set the player's position to be sitting on the cauldron
        Location cauldronLoc = cauldronBlock.getLocation();
        Location armorStandLocation = cauldronLoc.add(0.5, 1.0, 0.5);

        ArmorStand armorStand = player.getWorld().spawn(armorStandLocation, ArmorStand.class, asm -> {
            asm.setVisible(false); // Hide the armor stand
            asm.setMarker(true); // No bounding box/collision
            asm.setGravity(false); // No gravity so it stays in place
        });

        // Make the player sit on the armor stand
        armorStand.addPassenger(player);
        // Keep bladder and bowel stats at 0 while sitting
        PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());
        if (stats.getBladder() > 10) {
            stats.setBladder(0);
            if(!stats.getBladderLockIncon()){stats.decreaseBladderIncontinence(0.2);}
        }
        if (stats.getMessing() && stats.getBowels() > 10) {
            stats.setBowels(0);
            if(!stats.getBowelLockIncon()){stats.decreaseBowelIncontinence(0.2);}
        }
        if (stats.getBowelIncontinence() > 7 || stats.getBladderIncontinence() > 7) {
            player.sendMessage("Good job making it to the potty!");
        }
        else if (stats.getBowelIncontinence() >= 5 || stats.getBladderIncontinence() >= 5) {
            player.sendMessage("Potty training is going well!");
        }
        stats.setUrgeToGo(0);

        BukkitTask[] taskId = new BukkitTask[1];

        taskId[0] = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                // If the player is no longer a passenger of the armor stand
                if (!armorStand.getPassengers().contains(player)) {
                    // Play the sound
                    cauldronBlock.getWorld().playSound(cauldronLoc, "minecraft:toilet", SoundCategory.PLAYERS, 0.5f, 1.0f);
    
                    // Remove the armor stand
                    armorStand.remove();
    
                    // Cancel this task
                    taskId[0].cancel();
                }
            }
        }, 0L, 5L);
    }
    //Returns toilet on break
    @EventHandler
    public void onToiletBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block != null && block.getType() == Material.CAULDRON) {
            Block trapdoorBlock = block.getLocation().add(0, 1, 0).getBlock();
            if (trapdoorBlock.getType() == Material.IRON_TRAPDOOR) {
                Location loc = block.getLocation();

                // Remove cauldron and trapdoor
                block.setType(Material.AIR);
                loc.add(0, 1, 0).getBlock().setType(Material.AIR);

                // Drop the custom item
                block.getWorld().dropItemNaturally(loc, new ItemStack(ItemManager.toilet)); // Assuming you have a way to create the custom item
            }
        }
        if (block != null && block.getType() == Material.IRON_TRAPDOOR) {
            Block cauldronBlock = block.getLocation().add(0, -1, 0).getBlock();
            if (cauldronBlock.getType() == Material.CAULDRON) {
                Location loc = block.getLocation();

                // Remove cauldron and trapdoor
                block.setType(Material.AIR);
                loc.add(0, -1, 0).getBlock().setType(Material.AIR);

                // Drop the custom item
                block.getWorld().dropItemNaturally(loc, new ItemStack(ItemManager.toilet)); // Assuming you have a way to create the custom item
            }
        }
    }
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
                    Bukkit.getScheduler().runTask(plugin, () -> CustomInventory.OpenSettings(player, plugin));
                }
                else if ("bladderincon".equals(inputType)){
                    int newBladderincon = Integer.parseInt(message);
                    PlayerStats stats = plugin.getPlayerStats(playerUUID);
                    stats.setBladderIncontinence(newBladderincon);
                    player.sendMessage(ChatColor.GREEN + "Bladder Incontinence set to: " + newBladderincon);
                    plugin.clearAwaitingInput(playerUUID);
                    Bukkit.getScheduler().runTask(plugin, () -> CustomInventory.IncontinenceSettings(player, plugin));
                }else if ("bowelincon".equals(inputType)){
                    int newBowelincon = Integer.parseInt(message);
                    PlayerStats stats = plugin.getPlayerStats(playerUUID);
                    stats.setBowelIncontinence(newBowelincon);
                    player.sendMessage(ChatColor.GREEN + "Bowel Incontinence set to: " + newBowelincon);
                    plugin.clearAwaitingInput(playerUUID);
                    Bukkit.getScheduler().runTask(plugin, () -> CustomInventory.IncontinenceSettings(player, plugin));
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid number. Please enter a valid number.");
                Bukkit.getScheduler().runTask(plugin, () -> CustomInventory.OpenSettings(player, plugin));
            }
        }
    }
}

