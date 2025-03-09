package com.storynook.Event_Listeners;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.storynook.Plugin;

public class CribPlacement implements Listener{
    @SuppressWarnings("unused")
    private JavaPlugin plugin;
    public CribPlacement(Plugin plugin) {
        this.plugin = plugin;
    }
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            ItemStack itemInHand = event.getItem();

             if (itemInHand != null && itemInHand.getType() == Material.SLIME_BALL && itemInHand.hasItemMeta() && itemInHand.getItemMeta().hasCustomModelData()) {
                ItemMeta meta = itemInHand.getItemMeta();
                if (meta.getCustomModelData() == 627000) {
                    Location blockLocation = clickedBlock.getLocation();
                    BlockFace face = event.getBlockFace();
                    Location frameLocation = clickedBlock.getRelative(face).getLocation().add(0.5, 0, 0.5);
                    
                    // Create and place item frame
                    ArmorStand armorStand = (ArmorStand) blockLocation.getWorld().spawnEntity(frameLocation, EntityType.ARMOR_STAND);
                    
                    armorStand.setVisible(false);
                    armorStand.setCanPickupItems(false);
                    armorStand.setGravity(false);

                    itemInHand.setAmount(itemInHand.getAmount() - 1);

                    ItemStack crib = new ItemStack(Material.SLIME_BALL);
                    ItemMeta cribmeta = crib.getItemMeta();
                    cribmeta.setCustomModelData(meta.getCustomModelData());
                    cribmeta.setDisplayName("");
                    crib.setItemMeta(cribmeta);
                    armorStand.setHelmet(crib);
                }
            }
        }
    }
    @EventHandler
    public void PunchCrib (PlayerInteractEvent event){
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            Location blockLocation = clickedBlock.getLocation();

            Collection<Entity> nearbyEntities = clickedBlock.getWorld().getNearbyEntities(blockLocation, 1.5, 1.5, 1.5);
        
            // Loop through each entity to find an invisible armor stand
            for (Entity entity : nearbyEntities) {
                if (entity.getType() == EntityType.ARMOR_STAND) {
                    ArmorStand armorStand = (ArmorStand) entity;
                    if (!armorStand.isVisible()) {
                        ItemStack helmet = armorStand.getHelmet();
                        if (helmet != null && !helmet.getType().isAir()) {
                            ItemStack cribItem = helmet.clone();
                            ItemMeta meta = cribItem.getItemMeta();
                            
                            // Set the custom name
                            if (meta != null) {
                                meta.setDisplayName("Crib");
                                cribItem.setItemMeta(meta);
                            }
                            Item droppedHelmet = armorStand.getWorld().dropItem(armorStand.getLocation(), cribItem);
                            droppedHelmet.setVelocity(new Vector(0, 0.2, 0)); // Add slight upward velocity
                        }
                        armorStand.remove();
                        break;
                    }
                }
            }
        }
    }
}
