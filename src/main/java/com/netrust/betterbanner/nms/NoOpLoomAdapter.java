package com.netrust.betterbanner.nms;

import com.netrust.betterbanner.loom.LoomState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * The graceful-disable adapter (design §48: "Unsupported versions should
 * produce a clear warning and disable the incompatible feature rather than
 * crash unpredictably.").
 *
 * <p>Every method is a no-op or a constant {@code false}. The plugin still
 * loads, listeners still register, but no loom intervention happens.
 */
public final class NoOpLoomAdapter implements LoomAdapter {

    public NoOpLoomAdapter() {
    }

    @Override
    public boolean isLoom(@NotNull Player player) {
        return false;
    }

    @Override
    public LoomState getState(@NotNull Player player) {
        return LoomState.empty();
    }

    @Override
    public void setResult(@NotNull Player player, @NotNull ItemStack result) {
        // intentionally empty
    }

    @Override
    public void setSlot(int slotIndex, @NotNull Player player, @NotNull ItemStack item) {
        // intentionally empty
    }

    @Override
    public void synchronize(@NotNull Player player) {
        // intentionally empty
    }
}
