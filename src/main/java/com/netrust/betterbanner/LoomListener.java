package com.netrust.betterbanner;

import com.github.sarhatabaot.loomapi.nms.v1_14_R1.Loom;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author sarhatabaot
 */
public class LoomListener implements Listener {
    public LoomListener(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onLoomOpen(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if(clickedBlock == null || clickedBlock.getType() != Material.LOOM)
            return;

        Loom loom = new Loom(clickedBlock);
        loom.forceUpdatePattern();
        loom.forceLoadPatterns();
    }
}
