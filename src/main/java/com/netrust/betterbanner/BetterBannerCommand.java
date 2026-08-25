package com.netrust.betterbanner;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * @author sarhatabaot
 */
public class BetterBannerCommand implements CommandExecutor {
    private final BetterBanner plugin;

    public BetterBannerCommand(final BetterBanner plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label,
                             final @NotNull String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("ver")) {
            return this.onVersion(sender);
        }
        if (args[0].equalsIgnoreCase("debug")) {
            return this.onDebug(sender);
        }
        if (args[0].equalsIgnoreCase("reload")) {
            return this.onReload(sender);
        }

        sender.sendMessage(ChatColor.RED + "Usage: /" + label + " [version|debug|reload]");
        return true;
    }

    private boolean onVersion(final @NotNull CommandSender sender) {
        if (!sender.hasPermission(Permissions.COMMAND_VERSION)) {
            return noPermission(sender);
        }

        sender.sendMessage("BetterBanner version " + plugin.getDescription().getVersion() + " by " + plugin.getDescription().getAuthors());
        return true;
    }

    private boolean onDebug(final @NotNull CommandSender sender) {
        if (!sender.hasPermission(Permissions.COMMAND_DEBUG)) {
            return noPermission(sender);
        }

        plugin.setDebugMode(!plugin.isDebugMode());
        sender.sendMessage("BetterBanner debug is now " + plugin.isDebugMode());
        return true;
    }

    private boolean onReload(final @NotNull CommandSender sender) {
        if (!sender.hasPermission(Permissions.COMMAND_RELOAD)) {
            return noPermission(sender);
        }

        Config.load(plugin);
        sender.sendMessage("BetterBanner config reloaded");
        return true;
    }

    private boolean noPermission(final @NotNull CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
        return true;
    }
}
