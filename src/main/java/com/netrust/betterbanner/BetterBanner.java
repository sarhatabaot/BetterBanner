package com.netrust.betterbanner;

import co.aikar.commands.PaperCommandManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author sarhatabaot
 */
public class BetterBanner extends JavaPlugin {
    private boolean debugMode = false;

    @Override
    public void onEnable() {
        Config.load(this);

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new BetterBannerListener(this), this);

        PaperCommandManager paperCommandManager = new PaperCommandManager(this);
        paperCommandManager.registerCommand(new BetterBannerCommand(this));

        Metrics metrics = new Metrics(this, 3884);
    }


    public boolean isMyOutput(@NotNull Inventory wbInventory) {
        ItemStack outputStack = wbInventory.getItem(0);
        if (outputStack != null && BannerUtil.isBanner(outputStack.getType())) {
            this.debug("isMyOutput found a banner with " + ((BannerMeta) outputStack.getItemMeta()).numberOfPatterns() + " layers in crafting result");
            return ((BannerMeta) outputStack.getItemMeta()).numberOfPatterns() > 6;
        }

        this.debug("isMyOutput did not find a banner in the crafting result");
        return false;
    }

    public void debug(String msg) {
        if (this.debugMode) {
            getLogger().info("DEBUG" + msg);
        }

    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(final boolean debugMode) {
        this.debugMode = debugMode;
    }
}
