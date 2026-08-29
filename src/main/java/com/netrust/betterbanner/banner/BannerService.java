package com.netrust.betterbanner.banner;

import com.netrust.betterbanner.BannerUtil;
import com.netrust.betterbanner.Config;
import com.netrust.betterbanner.Permissions;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic, version-agnostic banner logic. Per design §11 / §62, this is the
 * only place that knows how a banner works; NMS code is isolated in
 * {@code com.netrust.betterbanner.nms}.
 *
 * <p>All methods on this class operate on Bukkit's public API. NMS access
 * lives elsewhere and feeds us {@link ItemStack}s through {@link
 * com.netrust.betterbanner.loom.LoomState}.
 */
public final class BannerService {

    /** Vanilla limit. Per design §38: "5 → 6 remains vanilla". */
    public static final int VANILLA_MAX_PATTERNS = 6;

    private BannerService() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return {@code true} if {@code stack} is a banner item of any base color.
     */
    public static boolean isBanner(@Nullable ItemStack stack) {
        return stack != null && BannerUtil.isBanner(stack.getType());
    }

    /**
     * @return the number of existing patterns on the banner, or 0 if the item
     *         is not a banner.
     */
    public static int getPatternCount(@Nullable ItemStack stack) {
        if (!isBanner(stack)) {
            return 0;
        }
        return ((BannerMeta) stack.getItemMeta()).numberOfPatterns();
    }

    /**
     * Produce a deep-clone of the banner that preserves all metadata (display
     * name, lore, item flags, custom NBT, etc.) — per design §36, only the
     * pattern list is allowed to change.
     */
    @Nullable
    public static ItemStack cloneBanner(@Nullable ItemStack stack) {
        if (!isBanner(stack)) {
            return null;
        }
        return stack.clone();
    }

    /**
     * Build a new banner identical to {@code original} but with {@code pattern}
     * appended to the end of the pattern list. Returns {@code null} if the
     * input is not a banner.
     */
    @Nullable
    public static ItemStack appendPattern(@Nullable ItemStack original, @NotNull Pattern pattern) {
        ItemStack clone = cloneBanner(original);
        if (clone == null) {
            return null;
        }
        BannerMeta meta = (BannerMeta) clone.getItemMeta();
        meta.addPattern(pattern);
        clone.setItemMeta(meta);
        clone.setAmount(1);
        return clone;
    }

    /**
     * Per-player pattern cap, preserving the per-tier configuration that
     * existed in the pre-1.14 BetterBanner. Unchanged in semantics from the
     * old {@code Config.createMax}.
     */
    public static int getMaxForPlayer(@NotNull Player player) {
        if (player.hasPermission(Permissions.UNLIMITED)) {
            return 999;
        }
        if (player.hasPermission(Permissions.ADVANCED)) {
            return Config.maxAdvanced();
        }
        if (player.hasPermission(Permissions.INTERMEDIATE)) {
            return Config.maxIntermediate();
        }
        if (player.hasPermission(Permissions.BASIC)) {
            return Config.maxBasic();
        }
        return Config.maxDefault();
    }

    /**
     * Returns {@code true} if {@code currentCount} is at or beyond
     * {@code maxForPlayer}. Per design §70, reaching the cap mirrors vanilla
     * behavior at the limit: no further valid output.
     */
    public static boolean isAtOrAboveCap(final int currentCount, final int maxForPlayer) {
        return currentCount >= maxForPlayer;
    }

    /**
     * Produce a shallow copy of {@code original} that preserves only the first
     * {@code patternCount} patterns. All other metadata (display name, lore,
     * item flags) is carried forward unchanged.
     *
     * <p>Used by {@code LoomService} for both Strategy B (writing a fake
     * 5-pattern banner to the input slot) and by {@code ProtocolLibLoomAdapter}
     * (spoofing the NBT in outgoing packets so the client never sees 6+
     * patterns).
     *
     * @return a new {@link ItemStack} with at most {@code patternCount}
     *         patterns, or {@code null} if the input is not a banner or has
     *         fewer patterns than requested.
     */
    @Nullable
    public static ItemStack buildShallowBanner(@Nullable ItemStack original, int patternCount) {
        if (!isBanner(original)) {
            return null;
        }
        BannerMeta originalMeta = (BannerMeta) original.getItemMeta();
        if (originalMeta == null || originalMeta.numberOfPatterns() < patternCount) {
            return null;
        }
        ItemStack shallow = original.clone();
        BannerMeta shallowMeta = (BannerMeta) shallow.getItemMeta();
        List<Pattern> keep = new ArrayList<>(patternCount);
        for (int i = 0; i < patternCount; i++) {
            keep.add(originalMeta.getPattern(i));
        }
        shallowMeta.setPatterns(keep);
        shallow.setItemMeta(shallowMeta);
        return shallow;
    }
}
