package com.storynook.Event_Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.storynook.PlayerStats;
import com.storynook.Plugin;

public class Laxative implements Listener{
     private final Plugin plugin;
    public Laxative(Plugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == 626099) {
                // Apply laxative effect
                PlayerStats stats = plugin.getPlayerStats(player.getUniqueId());
                if(stats.getMessing()){
                    if (stats.getEffectDuration() == 0) {
                        stats.setBowelFillRate(stats.getBowelFillRate() * ((Math.random() * 7) + 3));
                    }
                    stats.increaseEffectDuration(30);
                }
            }
        }
    }
}
