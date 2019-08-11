package com.netrust.betterbanner;

import net.minecraft.server.v1_14_R1.ContainerLoom;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.plugin.java.JavaPlugin;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author sarhatabaot
 */
public class BetterBanner extends JavaPlugin implements CommandExecutor {
    private Logger logger;
    private boolean debugMode = false;
    private static BetterBanner instance;

    @Override
    public void onEnable() {
        this.logger = this.getLogger();
        Config.load(this);
        instance = this;
        new BetterBannerListener(this);
        new LoomListener(this);

        Metrics metrics = new Metrics(this);

        this.debug("BetterBanner is started");
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
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

    public static BetterBanner getInstance() {
        return instance;
    }

    public void debug(String msg) {
        if (this.debugMode) {
            this.logger.info(msg);
        }
    }
}
