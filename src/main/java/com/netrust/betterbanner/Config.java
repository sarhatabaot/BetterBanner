package com.netrust.betterbanner;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Per-tier banner pattern caps, loaded from {@code config.yml} once at
 * startup and on {@code /betterbanner reload}.
 */
public final class Config {

    private static final int UNLIMITED = 999;
    private static final int VANILLA_MIN = 6;

    private static int maxDefault = VANILLA_MIN;
    private static int maxBasic = 8;
    private static int maxIntermediate = 11;
    private static int maxAdvanced = 15;
    private static boolean disableMetrics;

    private Config() {
        throw new UnsupportedOperationException();
    }

    /**
     * Load (or reload) configuration from disk.
     */
    public static void load(@NotNull BetterBanner plugin) {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();

        maxDefault = clampFloor(config.getInt("default", VANILLA_MIN), VANILLA_MIN);
        maxBasic = clampFloor(config.getInt("basic", 8), maxDefault + 1);
        maxIntermediate = clampFloor(config.getInt("intermediate", 11), maxBasic);
        maxAdvanced = clampFloor(config.getInt("advanced", 15), maxIntermediate);
        disableMetrics = config.getBoolean("disable-metrics", false);

        plugin.getLogger().info("Config loaded: default=" + maxDefault
                + " basic=" + maxBasic + " intermediate=" + maxIntermediate
                + " advanced=" + maxAdvanced);
    }

    // --- getters ---

    public static int maxDefault() { return maxDefault; }
    public static int maxBasic() { return maxBasic; }
    public static int maxIntermediate() { return maxIntermediate; }
    public static int maxAdvanced() { return maxAdvanced; }

    public static boolean isDisableMetrics() { return disableMetrics; }

    /**
     * Return the per-player pattern cap based on their permission nodes.
     *
     * <p>Checks {@code betterbanner.unlimited} first, then
     * {@code betterbanner.advanced}, {@code betterbanner.intermediate},
     * {@code betterbanner.basic}, falling back to the default cap.
     */
    public static int maxForPlayer(@NotNull Player player) {
        if (player.hasPermission(Permissions.UNLIMITED)) {
            return UNLIMITED;
        }
        if (player.hasPermission(Permissions.ADVANCED)) {
            return maxAdvanced;
        }
        if (player.hasPermission(Permissions.INTERMEDIATE)) {
            return maxIntermediate;
        }
        if (player.hasPermission(Permissions.BASIC)) {
            return maxBasic;
        }
        return maxDefault;
    }

    private static int clampFloor(int value, int floor) {
        return Math.max(value, floor);
    }
}
