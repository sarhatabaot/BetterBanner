package com.github.sarhatabaot.loomapi.nms.v1_14_R1;

import com.github.sarhatabaot.loomapi.LoomWrapper;
import net.minecraft.server.v1_14_R1.ContainerLoom;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Furnace;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.LoomInventory;

/**
 * @author sarhatabaot
 */
public class Loom implements LoomWrapper {
    private Block block;
    public Loom(Block block) {
        this.block = block;
    }

    @Override
    public void forceUpdatePattern() {
        Container container = (Container) block.getState();
        ContainerLoom containerLoom = (ContainerLoom) container;
        containerLoom.c();
    }

    @Override
    public void addPattern(Pattern pattern) {

    }

    @Override
    public void forceLoadPatterns() {

    }
}
