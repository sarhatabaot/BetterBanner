package com.netrust.betterbanner;

import com.netrust.betterbanner.nms.BukkitLoomAdapter;
import com.netrust.betterbanner.nms.LoomAdapter;
import com.netrust.betterbanner.nms.ProtocolLibLoomAdapter;
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
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command,
                             final @NotNull String label, final @NotNull String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("ver")) {
            return this.onVersion(sender);
        }
        if (args[0].equalsIgnoreCase("debug")) {
            return this.onDebug(sender, args);
        }
        if (args[0].equalsIgnoreCase("reload")) {
            return this.onReload(sender);
        }

        sender.sendMessage(ChatColor.RED + "Usage: /" + label + " [version|debug [nms|adapter]|reload]");
        return true;
    }

    private boolean onVersion(final @NotNull CommandSender sender) {
        if (!sender.hasPermission(Permissions.COMMAND_VERSION)) {
            return noPermission(sender);
        }

        sender.sendMessage("BetterBanner version " + plugin.getDescription().getVersion() + " by " + plugin.getDescription().getAuthors());
        return true;
    }

    private boolean onDebug(final @NotNull CommandSender sender, final @NotNull String[] args) {
        if (!sender.hasPermission(Permissions.COMMAND_DEBUG)) {
            return noPermission(sender);
        }

        if (args.length >= 2 && args[1].equalsIgnoreCase("nms")) {
            return this.onDebugNms(sender);
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase("adapter")) {
            return this.onDebugAdapter(sender);
        }

        plugin.setDebugMode(!plugin.isDebugMode());
        sender.sendMessage("BetterBanner debug is now " + plugin.isDebugMode());
        return true;
    }

    private boolean onDebugNms(final @NotNull CommandSender sender) {
        if (!sender.hasPermission(Permissions.COMMAND_DEBUG_NMS)) {
            return noPermission(sender);
        }
        sender.sendMessage(ChatColor.AQUA + "Dumping adapter diagnostics...");
        LoomAdapter adapter = plugin.getLoomAdapter();
        if (adapter == null) {
            sender.sendMessage(ChatColor.RED + "No LoomAdapter available (plugin not enabled?)");
            return true;
        }
        adapter.dumpDiagnostics(sender);
        sender.sendMessage(ChatColor.AQUA + "End of diagnostics.");
        return true;
    }

    private boolean onDebugAdapter(final @NotNull CommandSender sender) {
        if (!sender.hasPermission(Permissions.COMMAND_DEBUG_NMS)) {
            return noPermission(sender);
        }
        LoomAdapter adapter = plugin.getLoomAdapter();
        if (adapter == null) {
            sender.sendMessage(ChatColor.RED + "No LoomAdapter available (plugin not enabled?)");
            return true;
        }
        sender.sendMessage(ChatColor.AQUA + "Active LoomAdapter: " + ChatColor.WHITE + adapter.getClass().getName());
        sender.sendMessage(ChatColor.AQUA + "Server bukkit version: " + ChatColor.WHITE
                + plugin.getLoomService().serverBukkitVersion());
        if (adapter instanceof BukkitLoomAdapter && !(adapter instanceof ProtocolLibLoomAdapter)) {
            sender.sendMessage(ChatColor.YELLOW + "Adapter is a basic Bukkit adapter (ProtocolLib not detected).");
            sender.sendMessage(ChatColor.YELLOW + "Run /betterbanner debug nms for adapter diagnostics.");
        } else {
            sender.sendMessage(ChatColor.AQUA + "Run /betterbanner debug nms for a class layout dump.");
        }
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
