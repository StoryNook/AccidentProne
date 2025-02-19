package com.storynook.Event_Listeners;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import com.storynook.PlayerStats;
import com.storynook.Plugin;
import com.storynook.UpdateStats;
import com.storynook.items.ItemManager;

public class Toilet implements Listener{
    private final Plugin plugin;
    public Toilet(Plugin plugin) {
        this.plugin = plugin;
    }
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
        UpdateStats.playerSecondsLeftMap.put(player.getUniqueId(), 0);
        UpdateStats.playerWarningsMap.put(player.getUniqueId(), false);
        stats.setUrgeToGo(0);

        BukkitTask[] taskId = new BukkitTask[1];

        taskId[0] = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                // If the player is no longer a passenger of the armor stand
                if (!armorStand.getPassengers().contains(player)) {
                    // Play the sound

                    Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
                        
                    // Iterate through each online player to check their distance
                    for (Player targetPlayer : onlinePlayers) {
                        if (targetPlayer != null) {  // Ensure we have a valid player reference
                            if (targetPlayer.getLocation().getWorld() == player.getLocation().getWorld()) {
                                double distance = targetPlayer.getLocation().distance(cauldronLoc);
                                
                                // Check if the player is within the specified radius of 5 blocks
                                if (distance <= 10.0) {
                                    // Calculate volume based on distance, with max at 1.0f and min at 0.2f
                                    float maxVolume = 0.5f;
                                    float minVolume = 0.05f; // Minimum volume to still hear the sound
                                    float volume = (float) ((10 - distance) / 10 * (maxVolume - minVolume)) + minVolume;
                                    
                                    targetPlayer.playSound(targetPlayer.getLocation(), 
                                        "minecraft:toilet", // Updated sound reference using Sound enum
                                        SoundCategory.PLAYERS, 
                                        volume, // Volume decreases with distance
                                        1.0f);   // Keep pitch constant at normal speed
                                }
                            }
                        }
                    }
                                

                    // cauldronBlock.getWorld().playSound(cauldronLoc, "minecraft:toilet", SoundCategory.PLAYERS, 0.5f, 1.0f);
    
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
}
