package net.sarhatabaot.betterbanner.listener;

import net.sarhatabaot.betterbanner.BetterBanner;
import net.sarhatabaot.betterbanner.loom.LoomService;
import net.sarhatabaot.betterbanner.loom.LoomUpdateTask;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Per-player loom lifecycle: starts a polling task when a player opens a
 * loom and cancels it when they close it.
 */
public final class PlayerListener implements Listener {

    private final BetterBanner plugin;
    private final LoomService loomService;
    private final Map<UUID, BukkitTask> activeTasks = new HashMap<>();

    public PlayerListener(BetterBanner plugin, LoomService loomService) {
        this.plugin = plugin;
        this.loomService = loomService;
    }

    @EventHandler
    public void onInventoryOpen(@NotNull InventoryOpenEvent event) {
        if (event.getInventory().getType() != InventoryType.LOOM) {
            return;
        }
        HumanEntity who = event.getPlayer();
        if (!(who instanceof Player)) {
            return;
        }
        Player player = (Player) who;

        // Don't spawn a duplicate task if one is already running for this
        // player (e.g. the open event fired twice due to a quirk).
        UUID id = player.getUniqueId();
        BukkitTask existing = activeTasks.get(id);
        if (existing != null && !existing.isCancelled()) {
            return;
        }

        plugin.debug("PlayerListener: loom opened by " + player.getName() + ", starting polling");
        LoomUpdateTask task = new LoomUpdateTask(plugin, loomService, player);
        BukkitTask scheduled = task.scheduleRepeating();
        activeTasks.put(id, scheduled);
    }

    @EventHandler
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (event.getInventory().getType() != InventoryType.LOOM) {
            return;
        }
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getPlayer();
        BukkitTask task = activeTasks.remove(player.getUniqueId());
        if (task != null && !task.isCancelled()) {
            plugin.debug("PlayerListener: loom closed by " + player.getName() + ", cancelling polling");
            task.cancel();
        } else {
            plugin.debug("PlayerListener: loom closed by " + player.getName());
        }
    }
}

