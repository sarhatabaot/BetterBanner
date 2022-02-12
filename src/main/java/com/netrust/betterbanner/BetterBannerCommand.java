package com.netrust.betterbanner;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * @author sarhatabaot
 */

@CommandAlias("betterbanner")
@Description("Better banner base command.")
public class BetterBannerCommand extends BaseCommand {
    private final BetterBanner plugin;

    public BetterBannerCommand(final BetterBanner plugin) {
        this.plugin = plugin;
    }

    @Default
    @Subcommand("version")
    @Description("Show version information")
    @CommandPermission(Permissions.COMMAND_VERSION)
    public void onVersion(final @NotNull CommandSender sender) {
        sender.sendMessage("BetterBanner version " + plugin.getDescription().getVersion() + " by " + plugin.getDescription().getAuthors());
    }

    @Subcommand("debug")
    @Description("Toggle debug mode.")
    @CommandPermission(Permissions.COMMAND_DEBUG)
    public void onDebug(final @NotNull CommandSender sender) {
        plugin.setDebugMode(!plugin.isDebugMode());
        sender.sendMessage("BetterBanner debug is now " + plugin.isDebugMode());
    }

    @Subcommand("reload")
    @Description("Reload the configuration.")
    @CommandPermission(Permissions.COMMAND_RELOAD)
    public void onReload(final @NotNull CommandSender sender) {
        Config.load(plugin);
        sender.sendMessage("BetterBanner config reloaded");
    }
}
