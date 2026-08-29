package com.netrust.betterbanner.nms;

import com.netrust.betterbanner.loom.LoomState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Version-agnostic {@link LoomAdapter} that talks to the real loom purely
 * through the public Bukkit {@link Inventory} API.
 *
 * <p>The loom's top inventory is backed by CraftBukkit's
 * {@code CraftInventoryLoom}, which maps indices 0..3 to the banner, dye,
 * pattern-item, and result slots respectively. Reading and writing through
 * {@link Inventory#getItem(int)} / {@link Inventory#setItem(int, ItemStack)}
 * routes to the NMS container on our behalf, which means:
 * <ul>
 *   <li>writing the banner slot triggers vanilla's {@code slotsChanged()},
 *       which recomputes the result slot; and</li>
 *   <li>no per-version NMS reflection or obfuscation-mapping tables are
 *       required.</li>
 * </ul>
 *
 * <p>Because there is no public Bukkit API to read the loom's selected
 * pattern index, {@link #getState(Player)} reports {@code selectedPattern}
 * as {@code -1}. Generic BetterBanner code detects a completed selection by
 * observing the result slot instead of relying on that index.
 */
public final class BukkitLoomAdapter implements LoomAdapter {

    @Override
    public boolean isLoom(@NotNull Player player) {
        return player.getOpenInventory().getTopInventory().getType() == InventoryType.LOOM;
    }

    @Nullable
    @Override
    public LoomState getState(@NotNull Player player) {
        if (!isLoom(player)) {
            return null;
        }
        Inventory top = player.getOpenInventory().getTopInventory();
        ItemStack banner = top.getItem(BANNER_SLOT_INDEX);
        ItemStack dye = top.getItem(DYE_SLOT_INDEX);
        ItemStack patternItem = top.getItem(PATTERN_ITEM_SLOT_INDEX);
        ItemStack result = top.getItem(RESULT_SLOT_INDEX);
        return new LoomState(banner, dye, patternItem, result, -1);
    }

    @Override
    public void setResult(@NotNull Player player, @NotNull ItemStack result) {
        setSlot(RESULT_SLOT_INDEX, player, result);
    }

    @Override
    public void setSlot(int slotIndex, @NotNull Player player, @NotNull ItemStack item) {
        if (!isLoom(player)) {
            return;
        }
        Inventory top = player.getOpenInventory().getTopInventory();
        if (slotIndex < 0 || slotIndex >= top.getSize()) {
            return;
        }
        top.setItem(slotIndex, item);
    }

    @Override
    public void synchronize(@NotNull Player player) {
        player.updateInventory();
    }

    @Override
    public void dumpDiagnostics(@NotNull CommandSender sender) {
        sender.sendMessage("BetterBanner adapter: " + getClass().getName()
                + " (version-agnostic Bukkit Inventory API; no NMS reflection)");
    }
}
