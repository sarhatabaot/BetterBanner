package com.netrust.betterbanner.loom;

import com.netrust.betterbanner.banner.BannerResult;
import com.netrust.betterbanner.banner.BannerService;
import com.netrust.betterbanner.nms.LoomAdapter;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Generic orchestration layer for the loom (design sections 61, 62, 33).
 *
 * <p>Two strategies are supported for producing an over-limit result:
 * <ul>
 *   <li><b>Strategy A</b> (count &lt; 6): vanilla handles 0..5 patterns
 *       natively; BetterBanner is a no-op.</li>
 *   <li><b>Strategy B</b> (count &ge; 6): per design section 20, we
 *       build a shallow equivalent of the deep banner, write it to the
 *       input slot, let vanilla compute the next layer into the output
 *       slot, extract the new pattern, restore the original input, and
 *       write the final (deep + 1) result. This is the strategy that
 *       satisfies the user's request to "trick the server into thinking
 *       there are 5 layers instead of 6".</li>
 * </ul>
 */
public final class LoomService {

    private static final int SWAP_TIMEOUT_TICKS = 2;

    /**
     * Number of patterns written to the input slot to keep the vanilla client
     * happy while we compute an over-limit result. Must be strictly less than
     * {@link BannerService#VANILLA_MAX_PATTERNS} (6) so the client's loom
     * screen still shows the pattern selection buttons.
     */
    private static final int SHALLOW_PATTERN_COUNT = BannerService.VANILLA_MAX_PATTERNS - 1;

    private final LoomAdapter adapter;
    private final Consumer<String> debugLogger;
    private final Map<UUID, PendingShallowSwap> pendingSwaps = new HashMap<>();

    public LoomService(@NotNull LoomAdapter adapter) {
        this(adapter, msg -> { });
    }

    public LoomService(@NotNull LoomAdapter adapter, @NotNull Consumer<String> debugLogger) {
        this.adapter = adapter;
        this.debugLogger = debugLogger;
    }

    private void debug(@NotNull String msg) {
        debugLogger.accept(msg);
    }

    /**
     * Per-tick update for {@code player}. Implements the algorithm
     * from design sections 33, 61, and 20 (Strategy B for over-limit
     * banners).
     */
    public void update(@NotNull Player player) {
        if (!adapter.isLoom(player)) {
            // Player left the loom; clear any stale swap state.
            pendingSwaps.remove(player.getUniqueId());
            return;
        }

        // If we have a pending swap from a previous tick, finish it.
        if (pendingSwaps.containsKey(player.getUniqueId())) {
            finishPendingSwap(player);
            return;
        }

        LoomState state = adapter.getState(player);
        if (state == null) {
            debug("update(" + player.getName() + "): adapter.getState returned null");
            return;
        }

        ItemStack banner = state.getBanner();
        if (!BannerService.isBanner(banner)) {
            debug("update(" + player.getName() + "): no banner in loom");
            return;
        }

        int count = BannerService.getPatternCount(banner);
        debug("update(" + player.getName() + "): banner has " + count + " patterns");

        // Per design section 38: vanilla retains control while count < 6.
        if (count < BannerService.VANILLA_MAX_PATTERNS) {
            return;
        }

        // Per design section 70: when the cap is reached, mirror vanilla.
        int playerCap = BannerService.getMaxForPlayer(player);
        if (BannerService.isAtOrAboveCap(count, playerCap)) {
            debug("update(" + player.getName() + "): at cap (" + count + "/" + playerCap + "), leaving to vanilla");
            return;
        }

        if (!state.hasValidDye()) {
            debug("update(" + player.getName() + "): no dye");
            return;
        }

        // Strategy B: write a shallow (5-pattern) banner to the input slot so
        // the vanilla client shows the pattern selection buttons. Vanilla then
        // computes a 6-pattern result once a pattern is selected;
        // finishPendingSwap extracts the new pattern, restores the deep banner,
        // and writes the (deep + 1) result.
        startStrategyBSwap(player, banner, count);
    }

    /**
     * Strategy B phase 1: write a shallow banner (count - 1 patterns)
     * to the loom's input slot. Vanilla will compute a count-pattern
     * result on the next tick.
     */
    private void startStrategyBSwap(@NotNull Player player, @NotNull ItemStack original, int count) {
        ItemStack shallow = BannerService.buildShallowBanner(original, SHALLOW_PATTERN_COUNT);
        if (shallow == null) {
            debug("startStrategyBSwap(" + player.getName() + "): failed to build shallow banner");
            return;
        }
        debug("startStrategyBSwap(" + player.getName() + "): writing shallow ("
                + SHALLOW_PATTERN_COUNT + " patterns) to input slot to reveal the "
                + "pattern selection menu; vanilla should compute a "
                + BannerService.VANILLA_MAX_PATTERNS + "-pattern result once a pattern is selected");
        adapter.setSlot(LoomAdapter.BANNER_SLOT_INDEX, player, shallow);
        // Do NOT call adapter.synchronize() here — the NMS container already
        // broadcasts changes via setItem → slotsChanged → broadcastChanges.
        // An extra updateInventory() call would only produce a redundant packet
        // and make the visual flicker of the shallow swap more noticeable.
        pendingSwaps.put(player.getUniqueId(), new PendingShallowSwap(original.clone(), count));
    }



    /**
     * Strategy B phase 2: read vanilla's result, extract the new
     * (last) pattern, restore the original banner in the input slot,
     * append the new pattern to the original, and write the final
     * (deep + 1) result to the output slot.
     */
    private void finishPendingSwap(@NotNull Player player) {
        PendingShallowSwap swap = pendingSwaps.get(player.getUniqueId());
        if (swap == null) {
            return;
        }
        swap.ticksWaiting++;
        if (swap.ticksWaiting > SWAP_TIMEOUT_TICKS) {
            debug("finishPendingSwap(" + player.getName() + "): timed out after "
                    + SWAP_TIMEOUT_TICKS + " ticks; restoring original banner");
            adapter.setSlot(LoomAdapter.BANNER_SLOT_INDEX, player, swap.original);
            // synchronize() not needed — setSlot triggers broadcastChanges internally
            pendingSwaps.remove(player.getUniqueId());
            return;
        }

        LoomState state = adapter.getState(player);
        if (state == null) {
            debug("finishPendingSwap(" + player.getName() + "): state is null; will retry");
            return;
        }
        ItemStack vanillaResult = state.getResult();
        if (!BannerService.isBanner(vanillaResult)) {
            debug("finishPendingSwap(" + player.getName() + "): result slot is empty or not a banner; will retry");
            return;
        }
        int vanillaResultCount = BannerService.getPatternCount(vanillaResult);
        int expected = BannerService.VANILLA_MAX_PATTERNS;
        if (vanillaResultCount != expected) {
            debug("finishPendingSwap(" + player.getName() + "): result has " + vanillaResultCount
                    + " patterns, expected " + expected + "; will retry");
            return;
        }

        BannerMeta vanillaMeta = (BannerMeta) vanillaResult.getItemMeta();
        if (vanillaMeta == null || vanillaMeta.numberOfPatterns() < expected) {
            debug("finishPendingSwap(" + player.getName() + "): result meta has fewer patterns than expected");
            return;
        }
        // The shallow banner holds SHALLOW_PATTERN_COUNT (5) patterns, so the
        // pattern vanilla appended is at index 5 (the last one).
        Pattern newPattern = vanillaMeta.getPattern(expected - 1);
        if (newPattern == null) {
            debug("finishPendingSwap(" + player.getName() + "): extracted newPattern is null");
            return;
        }

        // Restore the original deep banner in the input slot.
        adapter.setSlot(LoomAdapter.BANNER_SLOT_INDEX, player, swap.original);
        // Build the final result: original with the new pattern appended.
        ItemStack finalResult = BannerService.appendPattern(swap.original, newPattern);
        if (finalResult == null) {
            debug("finishPendingSwap(" + player.getName() + "): appendPattern returned null; restoring input only");
            // setSlot above already triggered broadcastChanges; synchronize() is redundant
            pendingSwaps.remove(player.getUniqueId());
            return;
        }
        adapter.setResult(player, finalResult);
        adapter.synchronize(player);
        debug("finishPendingSwap(" + player.getName() + "): Strategy B done; wrote "
                + BannerService.getPatternCount(finalResult) + "-pattern result (original had "
                + swap.count + "), restored input");
        pendingSwaps.remove(player.getUniqueId());
    }

    /**
     * Resolve a {@link Pattern} for the player's current loom selection.
     * Returns {@code null} if the index is out of range for the standard
     * {@link PatternType} set.
     */
    @Nullable
    private Pattern resolvePattern(@NotNull LoomState state) {
        PatternType[] values = PatternType.values();
        int idx = state.getSelectedPattern();
        if (idx < 0 || idx >= values.length) {
            return null;
        }
        DyeColor color = guessDyeColor(state.getDye() != null ? state.getDye().getType() : null);
        return new Pattern(color, values[idx]);
    }

    /**
     * Best-effort mapping from a Bukkit {@link Material} (dye item) to a
     * {@link DyeColor}. Returns {@code WHITE} for unknown materials.
     */
    @NotNull
    private static DyeColor guessDyeColor(@Nullable Material dyeMaterial) {
        if (dyeMaterial == null) {
            return DyeColor.WHITE;
        }
        String name = dyeMaterial.name();
        if (name.endsWith("_DYE")) {
            String color = name.substring(0, name.length() - "_DYE".length());
            try {
                return DyeColor.valueOf(color);
            } catch (IllegalArgumentException ignored) {
                return DyeColor.WHITE;
            }
        }
        return DyeColor.WHITE;
    }

    /**
     * Build a {@link BannerResult} for the supplied state and pattern.
     * Exposed for tests and future tools.
     */
    @NotNull
    public BannerResult computeResult(@NotNull LoomState state, @NotNull Pattern pattern) {
        ItemStack banner = state.getBanner();
        ItemStack newBanner = BannerService.appendPattern(banner, pattern);
        if (newBanner == null) {
            return BannerResult.empty();
        }
        return BannerResult.of(newBanner);
    }

    /**
     * The full standard {@link PatternType} set, exposed for diagnostics
     * and tests.
     */
    @NotNull
    public List<Pattern> knownPatterns() {
        PatternType[] values = PatternType.values();
        List<Pattern> out = new ArrayList<>(values.length);
        for (PatternType t : values) {
            out.add(new Pattern(DyeColor.WHITE, t));
        }
        return out;
    }

    /** Server bukkit version, exposed for diagnostics. */
    @NotNull
    public String serverBukkitVersion() {
        return Bukkit.getServer().getBukkitVersion();
    }

    /**
     * Returns {@code true} when a Strategy B shallow-banner swap is in
     * progress for the given player. While a swap is pending, the result
     * slot carries a vanilla-computed banner that is NOT the final result;
     * clicks on that slot must be blocked until the swap completes.
     */
    public boolean hasPendingSwap(@NotNull UUID playerId) {
        return pendingSwaps.containsKey(playerId);
    }

    /**
     * Mutable per-player record for a pending Strategy B swap.
     * Tracks the original deep banner and the expected result count.
     */
    private static final class PendingShallowSwap {
        final ItemStack original;
        final int count;
        int ticksWaiting;

        PendingShallowSwap(ItemStack original, int count) {
            this.original = original;
            this.count = count;
        }
    }
}
