package com.netrust.betterbanner.nms;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Registry and factory for {@link LoomAdapter} instances.
 *
 * <p>Per design §48, version detection happens once at plugin startup.
 * Unsupported versions produce a graceful fallback rather than crashing.
 *
 * <p><b>ProtocolLib auto-detection</b>: if ProtocolLib is present on the
 * server, {@link #detect(Plugin)} returns a {@link ProtocolLibLoomAdapter}
 * that smoothes out the visual flicker through packet interception.
 * Otherwise a plain {@link BukkitLoomAdapter} is returned.
 */
public final class NmsVersions {

    private NmsVersions() {
        throw new UnsupportedOperationException();
    }

    /**
     * Detect the running server's environment and return the best available
     * {@link LoomAdapter}.
     *
     * <p>The detection order is:
     * <ol>
     *   <li>If ProtocolLib is on the classpath and a {@code ProtocolManager}
     *       can be obtained, return a {@link ProtocolLibLoomAdapter}.</li>
     *   <li>Otherwise return the vanilla {@link BukkitLoomAdapter}.</li>
     * </ol>
     *
     * @param plugin the owning plugin, used by ProtocolLib for packet-
     *               listener registration scope
     */
    @NotNull
    public static LoomAdapter detect(@NotNull Plugin plugin) {
        if (isProtocolLibAvailable()) {
            Bukkit.getLogger().info("[BetterBanner] ProtocolLib detected — "
                    + "enabling seamless packet-level loom experience.");
            return new ProtocolLibLoomAdapter(plugin);
        }
        Bukkit.getLogger().info("[BetterBanner] ProtocolLib not found — "
                + "using Bukkit-only loom adapter (minor flicker on 6+ pattern banners).");
        return new BukkitLoomAdapter();
    }

    /**
     * Attempt to load ProtocolLib's main class. If the class is on the
     * classpath and the ProtocolManager can be obtained without error,
     * ProtocolLib is available.
     */
    private static boolean isProtocolLibAvailable() {
        try {
            Class.forName("com.comphenix.protocol.ProtocolLibrary");
            // Touch ProtocolManager to confirm the plugin is fully loaded.
            @SuppressWarnings("unused")
            Object unused = com.comphenix.protocol.ProtocolLibrary.getProtocolManager();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
