package com.netrust.betterbanner;

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

        Metrics metrics = new Metrics(this, 3884);

    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String @NotNull [] args) {
        if (args.length < 1) {
            sender.sendMessage("Try /betterbanner < reload | debug | ver >");
        } else if (args[0].equalsIgnoreCase("ver")) {
            sender.sendMessage("BetterBanner version " + this.getDescription().getVersion() + " by " + this.getDescription().getAuthors());
        } else if (args[0].equalsIgnoreCase("reload") && sender.isOp()) {
            Config.load(this);
            sender.sendMessage("BetterBanner config reloaded");
        } else if (args[0].equalsIgnoreCase("debug") && sender.isOp()) {
            this.debugMode = !this.debugMode;
            sender.sendMessage("BetterBanner debug is now " + this.debugMode);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        ArrayList<String> commands = new ArrayList<>();
        if(command.getName().equalsIgnoreCase("betterbanner")){
            if(sender.isOp()) {
                commands.add("reload");
                commands.add("debug");
            }
            commands.add("ver");
        }
        return commands;
    }

    public boolean isMyOutput(Inventory wbInventory) {
        ItemStack outputStack = wbInventory.getItem(0);
        if (outputStack != null && BannerUtil.isBanner(outputStack.getType())) {
            this.debug("isMyOutput found a banner with " + ((BannerMeta)outputStack.getItemMeta()).numberOfPatterns() + " layers in crafting result");
            return ((BannerMeta)outputStack.getItemMeta()).numberOfPatterns() > 6;
        } else {
            this.debug("isMyOutput did not find a banner in the crafting result");
            return false;
        }
    }

    public void debug(String msg) {
        if (this.debugMode) {
            getLogger().info("DEBUG" + msg);
        }

    }
}
