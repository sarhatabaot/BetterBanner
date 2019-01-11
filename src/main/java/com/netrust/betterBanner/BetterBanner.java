package com.netrust.betterBanner;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author sarhatabaot
 */
public class BetterBanner extends JavaPlugin {
    Logger logger;
    boolean debugMode = false;

    public BetterBanner() {
    }

    public void onEnable() {
        this.logger = this.getLogger();
        Config.load(this);
        new BetterBannerListener(this);
        this.debug("BetterBanner is started");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("try /betterbanner < reload | debug | ver >");
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
        ArrayList<String> commands = new ArrayList<String>();
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
            this.logger.info(msg);
        }

    }
}
