package com.storynook.Event_Listeners;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import com.storynook.items.underwear;

public class DiaperPail implements Listener {
    private static Map<UUID, Inventory> pailInventories = new HashMap<>();

    @EventHandler
    public void OpenDiaperPail(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.BARRIER) {
            Collection<Entity> nearbyEntities = event.getClickedBlock().getWorld().getNearbyEntities(event.getClickedBlock().getLocation(), 1.5, 1.5, 1.5);
            
            for (Entity entity : nearbyEntities) {
                if (entity.getType() == EntityType.ARMOR_STAND) {
                    ArmorStand armorStand = (ArmorStand) entity;
                    String name = armorStand.getCustomName();
                    
                    if (name != null && name.startsWith("Pail_")) {
                        try {
                            UUID pailId = UUID.fromString(name.substring(6));
                            Inventory inventory = pailInventories.get(pailId);
                            
                            // Create empty inventory if it doesn't exist
                            if (inventory == null) {
                                inventory = Bukkit.createInventory(null, 27, "Diaper Pail");
                                pailInventories.put(pailId, inventory);
                            }
                            event.getPlayer().openInventory(inventory);
                            break;
                        } catch (IllegalArgumentException e) {
                            continue;
                        }
                     }
                }
            }
        }
    }
    @EventHandler
    public void CloseDiaperPail(InventoryCloseEvent event){
        if (event.getView().getTitle().equals("Diaper Pail")) {
            Inventory inventory = event.getInventory();
            boolean isValid = true;
            
            // Store original items for revert
            ItemStack[] originalContents = new ItemStack[inventory.getSize()];
            System.arraycopy(inventory.getContents(), 0, originalContents, 0, originalContents.length);

            for (ItemStack item : inventory.getContents()) {
                if (item == null || !isUsed(item)) {
                    isValid = false;
                    break;
                }
            }

            if (isValid){
                for (int i = 0; i < inventory.getSize(); i++) {
                    inventory.setItem(i, null);
                }
                // Add 9 new diaper items
                for (int i = 0; i < 9; i++) {
                    ItemStack diaper = underwear.ThickDiaper();
                    if (diaper != null) {
                        inventory.setItem(i, diaper);
                    }
                }
            } else {
                // Revert to original contents
                System.arraycopy(originalContents, 0, inventory.getContents(), 0, originalContents.length);
            }
        }
    }

    private boolean isUsed (ItemStack item){
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasCustomModelData()) {
                return meta.getCustomModelData() == 626005 || 
                meta.getCustomModelData() == 626004 ||
                meta.getCustomModelData() == 626011 ||  
                meta.getCustomModelData() == 626010;
            }
        }
        return false;
    }
    
    @SuppressWarnings("deprecation")
    @EventHandler
    public void PlaceDiaperPail(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            ItemStack itemInHand = event.getItem();

             if (itemInHand != null && itemInHand.getType() == Material.SLIME_BALL && itemInHand.hasItemMeta() && itemInHand.getItemMeta().hasCustomModelData()) {
                ItemMeta meta = itemInHand.getItemMeta();
                if (meta.getCustomModelData() == 628000) {
                    Location blockLocation = clickedBlock.getLocation();
                    BlockFace face = event.getBlockFace();
                    // Move the barrier location up by one y
                    Location frameLocation = new Location(blockLocation.getWorld(), blockLocation.getX(), blockLocation.getY() + 1, blockLocation.getZ());
                    Location armorStandLocation = clickedBlock.getRelative(face).getLocation().add(0.5, 0, 0.5);

                    Player player = event.getPlayer();
                    BlockFace playerDirection = player.getFacing();
                    
                    // Create and place item frame
                    ArmorStand armorStand = (ArmorStand) blockLocation.getWorld().spawnEntity(armorStandLocation, EntityType.ARMOR_STAND);
                    armorStand.setVisible(false);
                    
                    float yaw;
                    switch(playerDirection) {
                        case NORTH:
                            yaw = 0;
                            break;
                        case EAST:
                            yaw = 90;
                            break;
                        case SOUTH:
                            yaw = 180;
                            break;
                        case WEST:
                            yaw = 270;
                            break;
                        default:
                            // Handle any other cases if necessary
                            yaw = 0;
                            break;
                    }
                    
                    armorStand.setRotation(yaw, 0);
                    armorStand.setCanPickupItems(false);
                    armorStand.setGravity(false);
                    
                    // Place a barrier block at the same location as the armor stand
                    frameLocation.getBlock().setType(Material.BARRIER);

                    itemInHand.setAmount(itemInHand.getAmount() - 1);

                    ItemStack diaperpail = new ItemStack(Material.SLIME_BALL);
                    ItemMeta diaperpailmeta = diaperpail.getItemMeta();
                    diaperpailmeta.setCustomModelData(meta.getCustomModelData());
                    diaperpailmeta.setDisplayName("");
                    diaperpail.setItemMeta(diaperpailmeta);
                    armorStand.setHelmet(diaperpail);

                    UUID pailId = UUID.randomUUID(); // Generate unique ID for this pail
                    // Inventory inventory = Bukkit.createInventory(null, 9, "Diaper Pail");
                    // pailInventories.put(pailId, inventory); 

                    armorStand.setCustomName("Pail_" + pailId.toString()); // Store ID in custom name
                    
                }
            }
        }
    }

    @EventHandler
    public void PunchDiaperPail(PlayerInteractEvent event){
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.BARRIER) {
            Collection<Entity> nearbyEntities = event.getClickedBlock().getWorld().getNearbyEntities(event.getClickedBlock().getLocation(), 1.5, 1.5, 1.5);
            
            for (Entity entity : nearbyEntities) {
                if (entity.getType() == EntityType.ARMOR_STAND) {
                    ArmorStand armorStand = (ArmorStand) entity;
                    String name = armorStand.getCustomName();
                    
                    if (!armorStand.isVisible() && name != null && name.startsWith("Pail_")) {
                        try {
                            UUID pailId = UUID.fromString(name.substring(6));
                            Inventory inventory = pailInventories.remove(pailId);
                            
                            // Drop all items
                            Location dropLocation = armorStand.getLocation().add(0.5, 1, 0.5);
                            if (inventory != null) {
                                for (ItemStack stack : inventory.getContents()) {
                                    if (stack != null && !stack.getType().isAir()) {
                                        Item itemEntity = armorStand.getWorld().dropItem(dropLocation, stack.clone());
                                        itemEntity.setVelocity(new Vector(0, 0.2, 0));
                                    }
                                }
                            }

                            // Drop the pail itself
                            ItemStack pailItem = new ItemStack(Material.SLIME_BALL);
                            ItemMeta meta = pailItem.getItemMeta();
                            meta.setCustomModelData(628000);
                            meta.setDisplayName("Diaper Pail");
                            pailItem.setItemMeta(meta);

                            Item droppedPail = armorStand.getWorld().dropItem(dropLocation, pailItem);
                            droppedPail.setVelocity(new Vector(0, 0.2, 0));

                            armorStand.remove();
                            event.getClickedBlock().setType(Material.AIR);
                        } catch (IllegalArgumentException e) {
                            continue;
                        }
                    }
                }
            }
        }
    }
}
