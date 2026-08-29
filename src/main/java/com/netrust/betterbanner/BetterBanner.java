package com.netrust.betterbanner;

import com.netrust.betterbanner.listener.LoomInventoryListener;
import com.netrust.betterbanner.listener.PlayerListener;
import com.netrust.betterbanner.loom.LoomService;
import com.netrust.betterbanner.nms.LoomAdapter;
import com.netrust.betterbanner.nms.NmsVersions;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author sarhatabaot
 */
public class BetterBanner extends JavaPlugin {
    private boolean debugMode = false;
    private LoomService loomService;
    private LoomAdapter loomAdapter;

    @Override
    public void onEnable() {
        Config.load(this);

        // Version detection once, at startup. Per design section 48:
        // unsupported versions log a warning and disable the loom
        // feature; the plugin still loads.
        this.loomAdapter = NmsVersions.detect();
        this.loomService = new LoomService(loomAdapter, this::debug);
        getLogger().info("BetterBanner NMS adapter: "
                + loomAdapter.getClass().getSimpleName()
                + " (server: " + loomService.serverBukkitVersion() + ")");

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new LoomInventoryListener(this, loomService), this);
        pluginManager.registerEvents(new PlayerListener(this, loomService), this);

        if (getCommand("betterbanner") != null) {
            getCommand("betterbanner").setExecutor(new BetterBannerCommand(this));
        } else {
            getLogger().warning("betterbanner command not defined in plugin.yml");
        }

        Metrics metrics = new Metrics(this, 3884);
    }


    public LoomService getLoomService() {
        return loomService;
    }

    public LoomAdapter getLoomAdapter() {
        return loomAdapter;
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

