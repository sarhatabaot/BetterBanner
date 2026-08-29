package com.netrust.betterbanner.listener;

import com.netrust.betterbanner.BetterBanner;
import com.netrust.betterbanner.loom.LoomService;
import com.netrust.betterbanner.nms.LoomAdapter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Guards the loom result slot while a Strategy B shallow-banner swap is
 * in progress.
 *
 * <p>When BetterBanner temporarily replaces the input banner with a
 * 5-pattern shallow for the vanilla pattern-selection menu, vanilla
 * computes a 6-pattern result into the output slot. If the player takes
 * that result <em>before</em> the polling task restores the deep banner
 * and writes the real (deep&nbsp;+&nbsp;1) result, a duplication bug
 * occurs: vanilla's {@code onTake} consumes the shallow banner, and the
 * restored deep banner ends up as a second item in the player's
 * inventory.
 *
 * <p>Blocking result-slot clicks while the swap is pending eliminates
 * this race entirely.
 */
public final class LoomInventoryListener implements Listener {

    private final BetterBanner plugin;
    private final LoomService loomService;

    public LoomInventoryListener(BetterBanner plugin, LoomService loomService) {
        this.plugin = plugin;
        this.loomService = loomService;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!isLoom(event.getInventory().getType())) {
            return;
        }
        HumanEntity who = event.getWhoClicked();
        if (!(who instanceof Player)) {
            return;
        }
        Player player = (Player) who;

        // Block clicks / shift-clicks on the result slot while a shallow-banner
        // swap is pending. Without this guard, the player can take the
        // vanilla-computed intermediate result, causing a duplication bug.
        if (loomService.hasPendingSwap(player.getUniqueId())
                && event.getRawSlot() == LoomAdapter.RESULT_SLOT_INDEX) {
            plugin.debug("LoomInventoryListener: blocking result-slot click (swap pending) for "
                    + player.getName());
            event.setCancelled(true);
            return;
        }

        plugin.debug("LoomInventoryListener: click by " + player.getName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryDrag(@NotNull InventoryDragEvent event) {
        if (!isLoom(event.getInventory().getType())) {
            return;
        }
        HumanEntity who = event.getWhoClicked();
        if (!(who instanceof Player)) {
            return;
        }
        Player player = (Player) who;
        // Block drags that include the result slot while a swap is pending.
        if (loomService.hasPendingSwap(player.getUniqueId())
                && event.getRawSlots().contains(LoomAdapter.RESULT_SLOT_INDEX)) {
            plugin.debug("LoomInventoryListener: blocking drag over result slot (swap pending) for "
                    + player.getName());
            event.setCancelled(true);
            return;
        }

        plugin.debug("LoomInventoryListener: drag by " + player.getName());
    }

    /** Identifies loom inventory type. */
    private static boolean isLoom(@Nullable InventoryType type) {
        return type == InventoryType.LOOM;
    }
}



