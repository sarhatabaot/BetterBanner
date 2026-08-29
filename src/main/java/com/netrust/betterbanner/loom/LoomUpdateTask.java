package com.netrust.betterbanner.loom;

import com.netrust.betterbanner.BetterBanner;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

/**
 * Per-tick polling task for a single player with an open loom.
 *
 * <p>Per design section 32, polling is the preferred prototype strategy:
 * it sidesteps the timing questions around packet interception and NMS
 * method hooks while still answering the central question of whether a
 * plugin can inject a valid over-limit result into the real loom.
 *
 * <p>The task self-reschedules via {@link #runTaskTimer(org.bukkit.plugin.Plugin, long, long)}
 * so the loom is re-evaluated every tick for as long as the player has
 * it open. It self-cancels the moment the player closes the loom (i.e.
 * when their top inventory is no longer of type {@link InventoryType#LOOM}).
 */
public class LoomUpdateTask extends BukkitRunnable {

    /** Polling interval in ticks. 1 tick = 50ms on a vanilla server. */
    private static final long PERIOD_TICKS = 1L;

    private final BetterBanner plugin;
    private final LoomService loomService;
    private final org.bukkit.entity.Player player;

    public LoomUpdateTask(BetterBanner plugin, LoomService loomService, org.bukkit.entity.Player player) {
        this.plugin = plugin;
        this.loomService = loomService;
        this.player = player;
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            cancel();
            return;
        }
        // Cancel the polling loop the moment the player is no longer
        // looking at a loom. This is the equivalent of a per-player
        // InventoryCloseEvent hook.
        if (player.getOpenInventory().getType() != InventoryType.LOOM) {
            plugin.debug("LoomUpdateTask: " + player.getName() + " no longer has a loom open, cancelling");
            cancel();
            return;
        }
        plugin.debug("LoomUpdateTask tick for " + player.getName());
        loomService.update(player);
    }

    /**
     * Schedule this task to run on the next tick and then every
     * {@link #PERIOD_TICKS} ticks thereafter. Returns the
     * {@link BukkitTask} handle that the caller can use to cancel the
     * task later (e.g. when the player closes the loom). The task runs
     * on the server's main thread.
     */
    @NotNull
    public BukkitTask scheduleRepeating() {
        // runTaskTimer(plugin, delay, period). delay = 0 means "start
        // on the next tick"; PERIOD_TICKS = 1L means "every tick".
        return this.runTaskTimer(plugin, 0L, PERIOD_TICKS);
    }
}

