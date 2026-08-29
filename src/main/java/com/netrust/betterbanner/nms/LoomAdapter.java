package com.netrust.betterbanner.nms;

import com.netrust.betterbanner.loom.LoomState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stable semantic interface for the server-side loom (design §12).
 *
 * <p>Every method on this interface maps to a server-side concept that is
 * stable across Minecraft versions <em>semantically</em> but not
 * <em>structurally</em>. The structural mapping is the per-version
 * adapter's job; the rest of BetterBanner only sees this interface.
 *
 * <p>Implementations are not required to be thread-safe; all calls happen on
 * the server's main thread.
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
     *         a real server-side loom (i.e. the active container is a loom
     *         and the player is not just walking past one).
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
     * Place {@code result} into the real loom output slot. Implementations
     * are responsible for the actual NMS write.
     */
    void setResult(@NotNull Player player, @NotNull ItemStack result);

    /**
     * Place {@code item} into the specified slot index of the loom
     * {@code player} is currently interacting with. Used by Strategy B
     * (vanilla-assisted calculation) to temporarily swap the banner
     * input with a shallow equivalent.
     *
     * <p>Slot indices are stable across versions: 0 = banner input,
     * 1 = dye, 2 = pattern item, 3 = result. Other indices are
     * implementation-specific (typically the player's inventory
     * follows the loom's input slots).
     *
     * <p>Implementations should silently no-op if the slot index is
     * out of range rather than throw.
     */
    void setSlot(int slotIndex, @NotNull Player player, @NotNull ItemStack item);

    /**
     * Notify the client of slot changes after {@link #setResult}. Defaults to
     * a no-op; implementations should override with the correct NMS call
     * (e.g. {@code detectAndSendChanges()} on 1.14).
     */
    default void synchronize(@NotNull Player player) {
        // no-op by default
    }

    /**
     * Dump a human-readable summary of what this adapter resolved and
     * what the live NMS classes expose. Used by the
     * {@code /betterbanner debug nms} command.
     *
     * <p>Default implementation writes a brief "no diagnostics available"
     * message; concrete adapters should override with a real NMS layout
     * dump.
     */
    default void dumpDiagnostics(@NotNull org.bukkit.command.CommandSender sender) {
        sender.sendMessage("BetterBanner NMS diagnostics: not available for this adapter ("
                + getClass().getSimpleName() + ")");
    }
}
