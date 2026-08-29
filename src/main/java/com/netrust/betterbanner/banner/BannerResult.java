package com.netrust.betterbanner.banner;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable wrapper for a computed BetterBanner loom output.
 *
 * <p>The {@code result} is the actual item that should be placed in the loom
 * output slot. The {@code capExceeded} flag is set when the player attempted
 * to create a banner beyond their configured pattern cap.
 */
public final class BannerResult {

    private final ItemStack result;
    private final boolean capExceeded;

    public BannerResult(@Nullable ItemStack result, final boolean capExceeded) {
        this.result = result;
        this.capExceeded = capExceeded;
    }

    public static BannerResult empty() {
        return new BannerResult(null, false);
    }

    public static BannerResult capExceeded() {
        return new BannerResult(null, true);
    }

    public static BannerResult of(@NotNull ItemStack result) {
        return new BannerResult(result, false);
    }

    @Nullable
    public ItemStack getResult() {
        return result;
    }

    public boolean isCapExceeded() {
        return capExceeded;
    }

    public boolean isPresent() {
        return result != null;
    }
}
