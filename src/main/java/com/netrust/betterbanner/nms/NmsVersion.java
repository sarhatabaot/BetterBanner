package com.netrust.betterbanner.nms;

import org.jetbrains.annotations.NotNull;

/**
 * Enumerated set of Minecraft server versions BetterBanner knows about.
 *
 * <p>Per design §48, version detection happens once at startup. The mapping
 * from a live {@code org.bukkit.craftbukkit.vX_Y_RZ} package to a
 * {@code NmsVersion} value lives in {@link NmsVersions}.
 *
 * <p>Only {@link #V1_14_R1} is wired up to a real adapter today. The rest
 * exist so that {@link NmsVersions#detect()} can be extended without
 * churn and so that the multi-version intent of the project is visible
 * in the source tree (design §49).
 */
public enum NmsVersion {

    /** Minecraft 1.14 — the initial target. */
    V1_14_R1("1.14", "v1_14_R1"),

    /** Reserved for a future 1.15 adapter. */
    V1_15_R1("1.15", "v1_15_R1"),

    /** Reserved for a future 1.16.1 adapter. */
    V1_16_R1("1.16.1", "v1_16_R1"),

    /** Reserved for a future 1.16.2 adapter. */
    V1_16_R2("1.16.2", "v1_16_R2"),

    /** Reserved for a future 1.16.3 adapter. */
    V1_16_R3("1.16.3", "v1_16_R3"),

    /** Reserved for a future 1.17 adapter. */
    V1_17_R1("1.17", "v1_17_R1"),

    /** Reserved for a future 1.18.1 adapter. */
    V1_18_R1("1.18.1", "v1_18_R1"),

    /** Reserved for a future 1.18.2 adapter. */
    V1_18_R2("1.18.2", "v1_18_R2"),

    /** Reserved for a future 1.19 adapter. */
    V1_19_R1("1.19", "v1_19_R1"),

    /** Reserved for a future 1.19.1 adapter. */
    V1_19_R2("1.19.1", "v1_19_R2"),

    /** Reserved for a future 1.19.3 adapter. */
    V1_19_R3("1.19.3", "v1_19_R3"),

    /** Reserved for a future 1.19.4 adapter. */
    V1_19_R4("1.19.4", "v1_19_R4"),

    /** Reserved for a future 1.20.1 adapter. */
    V1_20_R1("1.20.1", "v1_20_R1"),

    /** Reserved for a future 1.20.2 adapter. */
    V1_20_R2("1.20.2", "v1_20_R2"),

    /** Reserved for a future 1.20.3/1.20.4 adapter. */
    V1_20_R3("1.20.3", "v1_20_R3"),

    /** Reserved for a future 1.20.5/1.20.6 adapter. */
    V1_20_R4("1.20.5", "v1_20_R4"),

    /** Reserved for a future 1.21.1 adapter. */
    V1_21_R1("1.21.1", "v1_21_R1"),

    /** Reserved for a future 1.21.2 adapter. */
    V1_21_R2("1.21.2", "v1_21_R2"),

    /** Reserved for a future 1.21.3 adapter. */
    V1_21_R3("1.21.3", "v1_21_R3"),

    /** Reserved for a future 1.21.4 adapter. */
    V1_21_R4("1.21.4", "v1_21_R4"),

    /** Reserved for a future 1.21.5 adapter. */
    V1_21_R5("1.21.5", "v1_21_R5"),

    /** Reserved for a future 1.21.6 adapter. */
    V1_21_R6("1.21.6", "v1_21_R6"),

    /** Reserved for a future 1.22 adapter. */
    V1_22_R1("1.22", "v1_22_R1"),

    /** Reserved for a future 1.22.2 adapter. */
    V1_22_R2("1.22.2", "v1_22_R2"),

    /** Reserved for a future 1.22.3 adapter. */
    V1_22_R3("1.22.3", "v1_22_R3"),

    /** Reserved for a future 1.23.1 adapter. */
    V1_23_R1("1.23.1", "v1_23_R1"),

    /** Reserved for a future 1.24.1 adapter. */
    V1_24_R1("1.24.1", "v1_24_R1"),

    /** Reserved for a future 1.25 adapter. */
    V1_25_R1("1.25", "v1_25_R1"),

    /** Reserved for a future 1.26 adapter (or 26.x line). */
    V1_26_R1("1.26", "v1_26_R1"),

    /** Reserved for a future 26.2 adapter. */
    V26_2_R1("26.2", "v26_2_R1");

    private final String minecraftVersion;
    private final String craftBukkitPackage;

    NmsVersion(String minecraftVersion, String craftBukkitPackage) {
        this.minecraftVersion = minecraftVersion;
        this.craftBukkitPackage = craftBukkitPackage;
    }

    @NotNull
    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    @NotNull
    public String getCraftBukkitPackage() {
        return craftBukkitPackage;
    }
}
