package com.netrust.betterbanner.nms;

import com.netrust.betterbanner.loom.LoomState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stable interface for reading and writing the server-side loom.
 *
 * <p>Implementations talk to the real loom container. Every method
 * maps to a server-side concept that is stable across Minecraft
 * versions. The rest of BetterBanner only sees this interface.
 */
public interface LoomAdapter {

    /** Slot index of the banner input. Stable across versions. */
    int BANNER_SLOT_INDEX = 0;

    /** Slot index of the dye input. Stable across versions. */
    int DYE_SLOT_INDEX = 1;

    /** Slot index of the pattern-item input. Stable across versions. */
    int PATTERN_ITEM_SLOT_INDEX = 2;

    /** Slot index of the loom's result/output slot. Stable across versions. */
    int RESULT_SLOT_INDEX = 3;

    /**
     * @return {@code true} if {@code player} is currently interacting with
     *         a loom (the active container is a loom).
     */
    boolean isLoom(@NotNull Player player);

    /**
     * Read the current state of the loom {@code player} is interacting with.
     *
     * @return a {@link LoomState} snapshot, or {@code null} if the player is
     *         not currently in a loom.
     */
    @Nullable
    LoomState getState(@NotNull Player player);

    /**
     * Place {@code result} into the loom output slot.
     */
    void setResult(@NotNull Player player, @NotNull ItemStack result);

    /**
     * Place {@code item} into the specified slot.
     *
     * <p>Slot indices: 0 = banner, 1 = dye, 2 = pattern item,
     * 3 = result.
     */
    void setSlot(int slotIndex, @NotNull Player player, @NotNull ItemStack item);

    /**
     * Notify the client of slot changes.
     */
    default void synchronize(@NotNull Player player) {
        // no-op by default
    }

    /**
     * Dump diagnostic information about this adapter.
     */
    default void dumpDiagnostics(@NotNull org.bukkit.command.CommandSender sender) {
        sender.sendMessage("BetterBanner NMS diagnostics: not available for this adapter ("
                + getClass().getSimpleName() + ")");
    }
}
