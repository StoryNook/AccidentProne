package com.storynook;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.storynook.items.ItemManager;
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
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
// import org.bukkit.material.Directional;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;


public class PlayerEventListener implements Listener {
    private static Plugin plugin;
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
        event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "Diaper"));
        event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "ThickDiaper"));
        event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "Pullup"));
        event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "Underwear"));
        event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "Tape"));
        event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "CleanPants"));
        // event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "DiaperPail"));
        event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "Toilet"));
        event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "DiaperStuffer"));
        event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "Washer"));
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
            PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());
            // The player location is ascending from the last event call and they were on the ground
            Double currentMultiplier = plugin.activityMultiplier.getOrDefault(playerUUID, 1.0);
            // double diaperFullness = stats.getDiaperFullness();
    
            // // Check if diaper is at a threshold level
            // if (diaperFullness == 25 || diaperFullness == 50 || diaperFullness == 75 || diaperFullness == 100) {
            //     // Reduce jump height by halving the vertical movement delta
            //     double reducedJumpHeight = event.getDelta().y() * 0.5;
            //     event.setDelta(new Vector(event.getDelta().x(), (float)reducedJumpHeight, event.getDelta().z()));
            // }
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
        if (customModelData == 626002) {stats.setUnderwearType(0);} //Underwear
        if (customModelData == 626003) {stats.setUnderwearType(1);} //Pullup
        if (customModelData == 626009) {stats.setUnderwearType(2);} //Diaper
        if (customModelData == 626001) {stats.setUnderwearType(3);} //Thick Diaper
        Justchanged.put(target.getUniqueId(), true);
        equipDiaperArmor(target, true, false);
        plugin.manageParticleEffects(target);
    }
        
    public static void equipDiaperArmor(Player target, boolean changed, boolean accident) {
        PlayerStats stats = plugin.getPlayerStats(target.getUniqueId());
        PlayerInventory inventory = target.getInventory();
        if (inventory.getLeggings() != null) {
            ItemStack leggings = target.getInventory().getLeggings();
            if (isDiaper(leggings) && !stats.getvisableUnderwear()) {
                    target.getInventory().setLeggings(null);
                    return;
            }
            else if (isDiaper(leggings) && (changed || accident)) {
                target.getInventory().setLeggings(null);
            }
        }
        if (stats.getvisableUnderwear() && inventory.getLeggings() == null) {
            ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);

            LeatherArmorMeta meta = (LeatherArmorMeta) leggings.getItemMeta();

            Color color = Color.fromRGB(Integer.parseInt("F7FFF4", 16));
            meta.setColor(color);
            switch (stats.getUnderwearType()) {
                case 0:
                if (stats.getDiaperWetness() > 0 && stats.getDiaperFullness() == 0) {
                    meta.setCustomModelData(626031);
                }
                else if (stats.getDiaperFullness() > 0 && stats.getDiaperWetness() == 0) {
                    meta.setCustomModelData(626032);
                }
                else if (stats.getDiaperFullness() > 0 && stats.getDiaperWetness() > 0){
                    meta.setCustomModelData(626033);
                }
                else{
                    meta.setCustomModelData(626002);
                }
                    meta.setDisplayName("Underwear");
                    break;
                case 1:
                if (stats.getDiaperWetness() > 0 && stats.getDiaperFullness() == 0) {
                    meta.setCustomModelData(626028);
                }
                else if (stats.getDiaperFullness() > 0 && stats.getDiaperWetness() == 0) {
                    meta.setCustomModelData(626029);
                }
                else if (stats.getDiaperFullness() > 0 && stats.getDiaperWetness() > 0){
                    meta.setCustomModelData(626030);
                }
                else{
                    meta.setCustomModelData(626003);
                }
                    meta.setDisplayName("Pullup");
                    break;
                case 2:
                if (stats.getDiaperWetness() > 0 && stats.getDiaperFullness() == 0) {
                    meta.setCustomModelData(626022);
                }
                else if (stats.getDiaperFullness() > 0 && stats.getDiaperWetness() == 0) {
                    meta.setCustomModelData(626023);
                }
                else if (stats.getDiaperFullness() > 0 && stats.getDiaperWetness() > 0){
                    meta.setCustomModelData(626024);
                }
                else{
                    meta.setCustomModelData(626009);
                }
                meta.setDisplayName("Diaper");
                    break;
                case 3:
                if (stats.getDiaperWetness() > 0 && stats.getDiaperFullness() == 0) {
                    meta.setCustomModelData(626025);
                }
                else if (stats.getDiaperFullness() > 0 && stats.getDiaperWetness() == 0) {
                    meta.setCustomModelData(626026);
                }
                else if (stats.getDiaperFullness() > 0 && stats.getDiaperWetness() > 0){
                    meta.setCustomModelData(626027);
                }
                else{
                    meta.setCustomModelData(626001);
                }
                    meta.setDisplayName("Thick Diaper");
                    break;
                default:
                    break;
            }

            meta.setUnbreakable(true);

            meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
            leggings.setItemMeta(meta);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS,ItemFlag.HIDE_UNBREAKABLE,ItemFlag.HIDE_ATTRIBUTES);
            AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "generic.armor", 0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.LEGS);
            meta.addAttributeModifier(Attribute.GENERIC_ARMOR, modifier);

            leggings.setItemMeta(meta);
            inventory.setLeggings(leggings);
        }
    }
                
    @EventHandler
    public void EquipLeggings(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        ItemStack leggings = player.getInventory().getLeggings();

        if (itemInHand != null) {
            if (isLeggings(itemInHand)) { 
                LeatherArmorMeta leggingsmeta = (LeatherArmorMeta) leggings.getItemMeta();
                if(leggingsmeta != null && leggingsmeta.hasCustomModelData()){
                    if (isDiaper(leggings)) {
                        inventory.setLeggings(null);
                        // if (!isCustomPants(itemInHand)) {
                        //     inventory.setLeggings(itemInHand);
                        //     inventory.removeItem(itemInHand);
                        // }
                    }
                }
            }
            else return;
        }
    }
                    
                
    @EventHandler
    public void onLeggingsBreak(PlayerItemBreakEvent event) {
        ItemStack brokenItem = event.getBrokenItem();
        if (isLeggings(brokenItem)) {
            Player player = event.getPlayer();
            Bukkit.getScheduler().runTaskLater(plugin, () -> equipDiaperArmor(player, false, false), 1L);
        }
    }
                
    @EventHandler
    public void onLeggingsClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        InventoryType.SlotType slotType = event.getSlotType();
        ItemStack currentItem = event.getCurrentItem();  // Item in the slot
        ItemStack cursorItem = event.getCursor();        // Item on the cursor

        // 1. Handle normal clicks on the leggings slot
        if (slotType == InventoryType.SlotType.ARMOR && event.getSlot() == 36) {
            // If the player is wearing the diaper leggings
            if (isDiaper(currentItem)) {
                // If they're trying to equip ANY other leggings
                if (cursorItem != null && isLeggings(cursorItem) && !isDiaper(cursorItem)) {
                    player.getInventory().setLeggings(null);  // Remove the diaper
                }
            }
        }

        // 2. Handle SHIFT-CLICK equipping leggings
        if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
            ItemStack shiftItem = event.getCurrentItem();

            if (shiftItem != null && isLeggings(shiftItem) && !isDiaper(shiftItem)) {
                ItemStack equippedLeggings = player.getInventory().getLeggings();

                // If the player is wearing diaper leggings, remove them
                if (isDiaper(equippedLeggings)) {
                    player.getInventory().setLeggings(null);
                }
            }
        }

        // 3. Handle number key hotbar swapping
        if (event.getClick() == ClickType.NUMBER_KEY) {
            int hotbarButton = event.getHotbarButton();
            ItemStack hotbarItem = player.getInventory().getItem(hotbarButton);

            if (hotbarItem != null && isLeggings(hotbarItem) && !isDiaper(hotbarItem)) {
                ItemStack equippedLeggings = player.getInventory().getLeggings();

                if (isDiaper(equippedLeggings)) {
                    player.getInventory().setLeggings(null);
                }
            }
        }
    }
                
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        ItemStack leggings = player.getInventory().getLeggings();

        // Check if the leggings are empty
        if (leggings == null || leggings.getType().isAir()) {
            equipDiaperArmor(player, false, false);
        }
    }
    private static boolean isDiaper(ItemStack item) {
        if (item == null || item.getType() != Material.LEATHER_LEGGINGS) {
            return false;
        }
        if (!item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta.hasCustomModelData()) {
            int modelData = meta.getCustomModelData();
            Set<Integer> diaperModels = new HashSet<>(Arrays.asList(626001, 626002, 626003, 626009,
                    626022, 626023, 626024, 626025, 626026, 626027, 626028, 626029,
                    626030, 626031, 626032, 626033));
            return diaperModels.contains(modelData);
        }
        // ItemMeta meta = item.getItemMeta();
        // if(meta.hasCustomModelData() && (
        //        meta.getCustomModelData() == 626001 ||  // Thick Diaper
        //        meta.getCustomModelData() == 626002 ||  // Underwear
        //        meta.getCustomModelData() == 626003 ||  // Pullup
        //        meta.getCustomModelData() == 626009 ||  // Diaper
        //        meta.getCustomModelData() == 626022 ||
        //        meta.getCustomModelData() == 626023 ||
        //        meta.getCustomModelData() == 626024 ||
        //        meta.getCustomModelData() == 626025 ||
        //        meta.getCustomModelData() == 626026 ||
        //        meta.getCustomModelData() == 626027 ||
        //        meta.getCustomModelData() == 626028 ||
        //        meta.getCustomModelData() == 626029 ||
        //        meta.getCustomModelData() == 626030 ||
        //        meta.getCustomModelData() == 626031 ||
        //        meta.getCustomModelData() == 626032 ||
        //        meta.getCustomModelData() == 626033
        // )) return true;
        return false;
    }
            
    private static boolean isCustomPants(ItemStack item) {
        if (item == null || item.getType() != Material.LEATHER_LEGGINGS) {
            return false;
        }
        if (!item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if(meta.hasCustomModelData() && (
               meta.getCustomModelData() == 626015 ||  // Pants
               meta.getCustomModelData() == 626016 ||  // Pants Wet
               meta.getCustomModelData() == 626017 ||  // Pants Dirt
               meta.getCustomModelData() == 626018     // Pants Wet & Dirty
        )) return true;
        return false;
    }
    

    private boolean isLeggings(ItemStack item) {
        if (item == null) return false;
        return item.getType() == Material.LEATHER_LEGGINGS ||
               item.getType() == Material.CHAINMAIL_LEGGINGS ||
               item.getType() == Material.IRON_LEGGINGS ||
               item.getType() == Material.GOLDEN_LEGGINGS ||
               item.getType() == Material.DIAMOND_LEGGINGS|| 
               isNetheriteLeggings(item.getType());
    }
    private boolean isNetheriteLeggings(Material material){
        try{
            return material == Material.valueOf("NETHERITE_LEGGINGS");
        } catch (IllegalArgumentException e){
            return false;
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
                if (stats.getBladder() >= 10 || stats.getBowels() >= 10) {
                    boolean bladderAccident = stats.getBladderIncontinence() >= stats.getBowelIncontinence();
                    stats.handleAccident(bladderAccident, player, false);
                    player.sendMessage("You got so scared by the attack that you had an accident!");
                }
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
        else if (stats.getBowelIncontinence() >= 4 || stats.getBladderIncontinence() >= 4) {
            // if (stats.getBladderLockIncon() && stats.getBowelLockIncon()) {
            //     player.sendMessage("Good job pretending");
            // }
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

