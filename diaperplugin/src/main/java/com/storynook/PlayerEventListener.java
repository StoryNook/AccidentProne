package com.storynook;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.storynook.items.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.TrapDoor;
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
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;


public class PlayerEventListener implements Listener {
    private Plugin plugin;
    HashMap<UUID, HashSet<NamespacedKey>> playerCraftedSpecialItems = new HashMap<>();

    public PlayerEventListener(Plugin plugin) {
        this.plugin = plugin;
    }
    //Loads the player stats when the login, and discovers all of the custom crafting recipes
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.loadPlayerStats(event.getPlayer()); //Uses the plugin instance to load player stats
        //Discover all of the custom crafting recipes
        NamespacedKey diaperkey = new NamespacedKey(plugin, "Diaper");
        NamespacedKey thickdiaperkey = new NamespacedKey(plugin, "ThickDiaper");
        NamespacedKey pullupkey = new NamespacedKey(plugin, "Pullup");
        NamespacedKey underwearkey = new NamespacedKey(plugin, "Underwear");
        NamespacedKey tapekey = new NamespacedKey(plugin, "Tape");
        NamespacedKey diaperpailkey = new NamespacedKey(plugin, "DiaperPail");
        NamespacedKey laxkey = new NamespacedKey(plugin, "Laxative");
        NamespacedKey durkey = new NamespacedKey(plugin, "Diuretic");
        event.getPlayer().discoverRecipe(diaperkey);
        event.getPlayer().discoverRecipe(thickdiaperkey);
        event.getPlayer().discoverRecipe(pullupkey);
        event.getPlayer().discoverRecipe(underwearkey);
        event.getPlayer().discoverRecipe(tapekey);
        event.getPlayer().discoverRecipe(diaperpailkey);
        event.getPlayer().discoverRecipe(laxkey);
        event.getPlayer().discoverRecipe(durkey);
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
    public void onPlayerDrink(PlayerItemConsumeEvent event) {
        PlayerStats stats = plugin.getPlayerStats(event.getPlayer().getUniqueId());
        if (stats != null) {
            // Increase hydration when the player drinks
            stats.increaseHydration(20);  // Adjust this amount as needed
        }
    }
    //Allows the player to change themselves
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Get the action (e.g., right-click, left-click)
        Action action = event.getAction();
        PlayerStats stats = plugin.getPlayerStats(event.getPlayer().getUniqueId());

        // Check if the action is a right-click (block or air)
        if (action == Action.RIGHT_CLICK_AIR) {
            // Get the item in the player's hand
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();

            // Check if the item is your custom item
            if (isCustomItem(item) && !stats.getHardcore()) {
                int customModelData = item.getItemMeta().getCustomModelData();

                //Give the player their used diaper.
                if(stats.getDiaperFullness() > 0 && stats.getUnderwearType() > 0){player.getInventory().addItem(ItemManager.stinkydiaper);}
                if(stats.getDiaperWetness() > 0 && stats.getUnderwearType() == 1){player.getInventory().addItem(ItemManager.wetpullup);}
                if(stats.getDiaperWetness() > 0 && stats.getUnderwearType() == 2){player.getInventory().addItem(ItemManager.wetdiaper);}
                if(stats.getDiaperWetness() > 0 && stats.getUnderwearType() == 3){player.getInventory().addItem(ItemManager.wetthickdiaper);}
                // if(stats.getDiaperWetness() > 0 && stats.getUnderwearType() == 0){player.getInventory().addItem(ItemManager.wetundies);}
                // if(stats.getDiaperWetness() > 0 && stats.getUnderwearType() == 0){player.getInventory().addItem(ItemManager.messyundies);}

                //Reset the underwear status, and assign a type (Underwear, Pullup, Diaper, Think Diaper)
                stats.setDiaperFullness(0);
                stats.setDiaperWetness(0);
                stats.setTimeWorn(0);
                if (customModelData == 626002) {stats.setUnderwearType(0);} //Underwear
                if (customModelData == 626003) {stats.setUnderwearType(1);} //Pullup
                if (customModelData == 626009) {stats.setUnderwearType(2);} //Diaper
                if (customModelData == 626001) {stats.setUnderwearType(3);} //Thick Diaper
                int currentAmount = item.getAmount();
                if (currentAmount > 1) {
                    item.setAmount(currentAmount - 1); // Decrease the amount by 1
                } else {
                    player.getInventory().setItemInMainHand(null); // Remove the item entirely if only 1 remains
                }
                // Optional: Send feedback to the player
                player.sendMessage(ChatColor.GREEN + "You got changed and cleaned!");
                
            }
        }
    }
    //When weather changes, checks for thunder
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        // Check if the weather is changing to thunder
        if (event.toWeatherState()) {
            // Iterate through all players
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (isNearThunder(player.getLocation())) {
                    handleThunderEffect(player);
                }
            }
        }
    }
    //Check thunder proximity
    private boolean isNearThunder(Location location) {
        // Example check for nearby thunder (you can adjust the radius as needed)
        Location thunderLocation = location.getWorld().getHighestBlockAt(location).getLocation();
        return location.distance(thunderLocation) <= 50; // Check within 50 blocks
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
                stats.handleAccident(bladderAccident, player, false);
                player.sendMessage("You got so scared by the thunder that you had an accident!");
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
                double chance = Math.min(4, Math.max(0, stats.getBladderIncontinence() / 2));
                Random random = new Random();
                if (random.nextInt(4) < chance) {
                    stats.handleAccident(true, player,false);
                    player.sendMessage("Oh no! You wet the bed!");
                }
            }
        }
    }
    //Checks to see if the item used in crafting is a custom item. (ID based)
    private boolean isCustomItem(ItemStack item) {
        if (item == null || !item.getItemMeta().hasCustomModelData()) {
            return false;
        }
        int customModelData = item.getItemMeta().getCustomModelData();
        List<Integer> CustomItemIDs = Arrays.asList(626009, 626001, 626002, 626003, 626004,626005, 626011, 626010);
        
        // Check for custom emerald items defined in ItemManager
        if (CustomItemIDs.contains(customModelData)) {
            return true;
        }

        return false;
    }

    // private boolean isUsed (ItemStack item) {
    //     if (item == null || !item.hasItemMeta()) {
    //     return false;
    //     }
    //     ItemMeta meta = item.getItemMeta();
    //     if (!meta.hasCustomModelData()) {
    //         return false;
    //     }

    //     int customModelData = meta.getCustomModelData();
    //     List<Integer> CustomItemIDs = Arrays.asList(626005, 626011, 626010, 626004);

    //     return CustomItemIDs.contains(customModelData);
    // }
    //Prevents crafting with custom items
    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();
        ItemStack[] matrix = inventory.getMatrix();

        boolean blockCrafting = false;
        boolean hasLaxative = false;
        ItemStack foodItem = null;
        Material foodItemType = null;

        for (ItemStack item : matrix) {
            if (item == null) continue;

            if (isCustomItem(item)) {
                blockCrafting = true; // Only block crafting if it's not intended
            }

            if (isLaxative(item)) {
                hasLaxative = true;
            } else if (item.getType().isEdible()) {
                if (foodItemType == null) {
                    foodItemType = item.getType();
                    foodItem = item;
                } else if (foodItemType != item.getType()){
                    inventory.setResult(null);
                    return;
                }
            }
        }
        if (blockCrafting) {
            inventory.setResult(null); // Cancel crafting if custom items are involved inappropriately
        }else if (hasLaxative && foodItem != null) {
            // Logic for creating laxative-imbued food items
            ItemStack result = foodItem.clone();
            ItemMeta meta = result.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(
                    new NamespacedKey(plugin, "laxative_effect"),
                    PersistentDataType.BYTE,
                    (byte) 1
                );
                result.setItemMeta(meta);
            }
            result.setAmount(1);
            inventory.setResult(result);
        }
        else {
            // inventory.setResult(result); // Make sure other recipes can work
        }
    }


    @EventHandler
    public void onCraft(CraftItemEvent event) {
        // Ensure the crafted item has the laxative effect
        ItemStack result = event.getRecipe().getResult();
        if (result.hasItemMeta() && result.getItemMeta().getPersistentDataContainer().has(
                new NamespacedKey(plugin, "laxative_effect"), PersistentDataType.BYTE)) {
            
            // Ensure this is a player crafting
            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player) event.getWhoClicked();
            CraftingInventory inventory = event.getInventory();
            ItemStack[] matrix = inventory.getMatrix();

            // Use flags to prevent reprocessing the same crafting event
            boolean foodReduced = false;
            boolean laxativeReduced = false;

            // Reduce items in the crafting matrix
            for (int i = 0; i < matrix.length; i++) {
                ItemStack item = matrix[i];
                if (item == null) continue;

                if (isLaxative(item) && !laxativeReduced) {
                    item.setAmount(item.getAmount() - 1);
                    if (item.getAmount() <= 0) matrix[i] = null; // Remove empty slot
                    laxativeReduced = true;
                } else if (item.getType().isEdible() && !foodReduced) {
                    item.setAmount(item.getAmount() - 1);
                    if (item.getAmount() <= 0) matrix[i] = null; // Remove empty slot
                    foodReduced = true;
                }

                // Stop when both reductions are complete
                if (laxativeReduced && foodReduced) break;
            }

            // Use a delayed task to update the inventory safely
            Bukkit.getScheduler().runTask(plugin, () -> inventory.setMatrix(matrix));

            // Prevent shift-click issues
            if (event.isShiftClick()) {
                event.setCancelled(true);
                player.getInventory().addItem(result);
            }
        }
    }

    // @EventHandler
    // public void onCraft(CraftItemEvent event) {
    //     // Ensure the crafted item has the laxative effect
    //     ItemStack result = event.getRecipe().getResult();
    //     if (result.hasItemMeta() && result.getItemMeta().getPersistentDataContainer().has(
    //             new NamespacedKey(plugin, "laxative_effect"), PersistentDataType.BYTE)) {
    
    //         // Ensure this is a player crafting
    //         if (!(event.getWhoClicked() instanceof Player)) return;
    //         Player player = (Player) event.getWhoClicked();
    
    //         CraftingInventory inventory = event.getInventory();
    //         ItemStack[] matrix = inventory.getMatrix();
    
    //         // Use a flag to ensure we only process once
    //         boolean foodReduced = false;
    //         boolean laxativeReduced = false;
    
    //         // Loop through the crafting matrix and reduce items
    //         for (int i = 0; i < matrix.length; i++) {
    //             ItemStack item = matrix[i];
    //             if (item == null) continue;
    
    //             if (isLaxative(item) && !laxativeReduced) {
    //                 item.setAmount(item.getAmount() - 1);
    //                 if (item.getAmount() <= 0) matrix[i] = null; // Remove empty slot
    //                 laxativeReduced = true;
    //             } else if (item.getType().isEdible() && !foodReduced) {
    //                 item.setAmount(item.getAmount() - 1);
    //                 if (item.getAmount() <= 0) matrix[i] = null; // Remove empty slot
    //                 foodReduced = true;
    //             }
    
    //             // Stop when both reductions are complete
    //             if (laxativeReduced && foodReduced) break;
    //         }
    
    //         // Update the inventory matrix
    //         inventory.setMatrix(matrix);
    
    //         // Prevent shift-click from causing issues
    //         if (event.isShiftClick()) {
    //             event.setCancelled(true);
    //             player.getInventory().addItem(result);
    //         }
    //     }
    // }



    // @EventHandler
    // public void onCraft(CraftItemEvent event) {
    //     // Handle ingredient reduction after crafting
    //     CraftingInventory inventory = event.getInventory();
    //     ItemStack result = event.getRecipe().getResult();

    //     if (result.getItemMeta() != null &&
    //         result.getItemMeta().getPersistentDataContainer().has(
    //             new NamespacedKey(plugin, "laxative_effect"), PersistentDataType.BYTE)) {

    //         ItemStack[] matrix = inventory.getMatrix();
    //         // Remove laxative and food item from the crafting matrix
    //         for (int i = 0; i < matrix.length; i++) {
    //             ItemStack item = matrix[i];
    //             if (item == null) continue;
    //             if(isLaxative(item))
    //             {
    //                 item.setAmount(item.getAmount() - 1);
    //                 if (item.getAmount() <= 0) {
    //                     matrix[i] = null;
    //                 }
    //             } else if(item.getType().isEdible()){
    //                 item.setAmount(item.getAmount() - 1);
    //                 if(item.getAmount() <= 0){
    //                     matrix[i] = null;
    //                 }
    //             }
    //         }
    //         inventory.setMatrix(matrix);
    //     }
    // }

    private boolean isLaxative(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.hasCustomModelData() && meta.getCustomModelData() == 626012;
    }

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

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.getPersistentDataContainer().has(
                new NamespacedKey(plugin, "laxative_effect"),
                PersistentDataType.BYTE
            )) {
                // Apply laxative effect
                PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());
                if(stats.getMessing()){
                    if (stats.getEffectDuration() == 0) {
                        stats.setBowelFillRate(stats.getBowelFillRate() * 2);
                    }
                    stats.increaseEffectDuration(30);
                    player.sendMessage(ChatColor.RED + "You feel the effects of the laxative...");
                }
            }
        }
    }

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
        stats.setBladder(0);
        stats.decreaseBladderIncontinence(0.2);
        stats.setBowels(0);
        stats.decreaseBowelIncontinence(0.2);
        stats.setUrgeToGo(0);

        BukkitTask[] taskId = new BukkitTask[1];

        taskId[0] = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                // If the player is no longer a passenger of the armor stand
                if (!armorStand.getPassengers().contains(player)) {
                    // Play the sound
                    cauldronBlock.getWorld().playSound(cauldronLoc, "minecraft:toilet", SoundCategory.PLAYERS, 1.0f, 1.0f);
    
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
}

