package com.netrust.betterbanner.nms;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Registry and factory for {@link LoomAdapter} instances.
 *
 * <p>Per design §48, version detection happens once at plugin startup.
 * Unsupported versions produce a {@link NoOpLoomAdapter} and a clear
 * warning rather than crashing unpredictably.
 *
 * <p>Today, only {@link NmsVersion#V1_14_R1} is wired to a real adapter.
 * The other enum values are reserved for future implementations; attempting
 * to use them yields a {@link NoOpLoomAdapter}.
 */
public final class NmsVersions {

    private NmsVersions() {
        throw new UnsupportedOperationException();
    }

    /**
     * Detect the running server's NMS version and return the matching
     * {@link LoomAdapter}.
     *
     * <p>The detection order is:
     * <ol>
     *   <li>Read {@code Bukkit.getServer().getBukkitVersion()} and try to
     *       find an exact match in the registered enum values.</li>
     *   <li>Fall back to scanning the loaded {@code org.bukkit.craftbukkit.vX_Y_RZ}
     *       package name on the classpath.</li>
     *   <li>If nothing matches, log a warning and return a
     *       {@link NoOpLoomAdapter} (the plugin still loads; the loom
     *       feature is just disabled).</li>
     * </ol>
     */
    @NotNull
    public static LoomAdapter detect() {
        String bukkitVersion = Bukkit.getServer().getBukkitVersion();
        NmsVersion detected = identify(bukkitVersion);
        if (detected == null) {
            detected = identifyFromClasspath();
        }
        if (detected == null) {
            Bukkit.getLogger().info("[BetterBanner] Could not identify server version ("
                    + bukkitVersion + "); using the version-agnostic Bukkit loom adapter.");
        } else {
            Bukkit.getLogger().info("[BetterBanner] Detected server version " + detected
                    + "; using the version-agnostic Bukkit loom adapter.");
        }
        return adapterFor(detected);
    }

    /**
     * Map an {@link NmsVersion} to its concrete adapter.
     *
     * <p>BetterBanner now uses a single {@link BukkitLoomAdapter} that relies
     * only on the public Bukkit {@link org.bukkit.inventory.Inventory} API, so
     * it works across every Minecraft version without per-version NMS code.
     */
    @NotNull
    public static LoomAdapter adapterFor(@Nullable NmsVersion version) {
        return new BukkitLoomAdapter();
    }

    /**
     * Match a Bukkit version string (e.g. {@code 1.14.4-R0.1-SNAPSHOT})
     * to a known {@link NmsVersion}. Returns {@code null} if no match.
     *
     * <p>The match is by trailing-segment equality rather than raw
     * {@code startsWith}, so {@code 1.20.6-R0.1-SNAPSHOT} does not
     * accidentally match a {@code 1.20} value. Iteration order is
     * longest-Minecraft-version first so {@code 1.20.5} wins over
     * {@code 1.20}.
     */
    @Nullable
    static NmsVersion identify(@NotNull String bukkitVersion) {
        // bukkitVersion looks like "1.20.6-R0.1-SNAPSHOT"; the first
        // dotted segment group is the MC version.
        int dash = bukkitVersion.indexOf('-');
        String mcHead = dash >= 0 ? bukkitVersion.substring(0, dash) : bukkitVersion;
        NmsVersion[] sorted = sortedByVersionStringLengthDesc();
        for (NmsVersion v : sorted) {
            if (mcHead.equals(v.getMinecraftVersion())) {
                return v;
            }
        }
        return null;
    }

    @NotNull
    private static NmsVersion[] sortedByVersionStringLengthDesc() {
        NmsVersion[] all = NmsVersion.values();
        java.util.Arrays.sort(all, (a, b) ->
                Integer.compare(b.getMinecraftVersion().length(), a.getMinecraftVersion().length()));
        return all;
    }

    /**
     * Last-resort detection by scanning the classpath for a known
     * {@code org.bukkit.craftbukkit.vX_Y_RZ} package. This is the
     * mechanism design §48 explicitly recommends.
     */
    @Nullable
    static NmsVersion identifyFromClasspath() {
        for (NmsVersion v : NmsVersion.values()) {
            String probe = "org.bukkit.craftbukkit." + v.getCraftBukkitPackage() + ".CraftServer";
            try {
                Class.forName(probe);
                return v;
            } catch (ClassNotFoundException ignored) {
                // try next
            }
        }
        return null;
    }
}
