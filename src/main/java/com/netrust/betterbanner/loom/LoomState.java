package com.netrust.betterbanner.loom;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable snapshot of the real server-side loom's relevant state.
 *
 * <p>Per design §12, this exposes stable semantic information; generic
 * BetterBanner code never needs to know that 1.14 calls the selected-pattern
 * getter {@code e()}.
 *
 * <p>All four item fields are nullable: any of them may be empty. {@code
 * selectedPattern} is the loom's currently selected pattern index (Bukkit
 * {@code Banner.Pattern} ordinal), or {@code -1} if none is selected.
 */
public final class LoomState {

    private final ItemStack banner;
    private final ItemStack dye;
    private final ItemStack patternItem;
    private final ItemStack result;
    private final int selectedPattern;

    public LoomState(@Nullable ItemStack banner,
                     @Nullable ItemStack dye,
                     @Nullable ItemStack patternItem,
                     @Nullable ItemStack result,
                     final int selectedPattern) {
        this.banner = banner;
        this.dye = dye;
        this.patternItem = patternItem;
        this.result = result;
        this.selectedPattern = selectedPattern;
    }

    /**
     * Backwards-compatible constructor that omits the result slot (treated as
     * {@code null}). Kept so adapters that only read the three input slots can
     * still build a state snapshot.
     */
    public LoomState(@Nullable ItemStack banner,
                     @Nullable ItemStack dye,
                     @Nullable ItemStack patternItem,
                     final int selectedPattern) {
        this(banner, dye, patternItem, null, selectedPattern);
    }

    @Nullable
    public ItemStack getBanner() {
        return banner;
    }

    @Nullable
    public ItemStack getDye() {
        return dye;
    }

    @Nullable
    public ItemStack getPatternItem() {
        return patternItem;
    }

    @Nullable
    public ItemStack getResult() {
        return result;
    }

    public int getSelectedPattern() {
        return selectedPattern;
    }

    public boolean hasValidDye() {
        return dye != null && dye.getType() != Material.AIR;
    }

    public boolean hasValidSelection() {
        return selectedPattern >= 0;
    }

    @NotNull
    public static LoomState empty() {
        return new LoomState(null, null, null, null, -1);
    }
}
