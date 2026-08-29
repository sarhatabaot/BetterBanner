/**
 * Minecraft 1.21.3 NMS adapter package for BetterBanner.
 *
 * <p>STUB. Per design section 49 ("Future Version Strategy"), adapters are
 * created only for Minecraft versions whose {@code ContainerLoom} semantics
 * diverge materially from 1.14. When that happens for 1.21.3, drop a
 * {@code LoomAdapter_v1_21_R3} class here implementing
 * {@link com.netrust.betterbanner.nms.LoomAdapter}, and wire it up in
 * {@link com.netrust.betterbanner.nms.NmsVersions#adapterFor(com.netrust.betterbanner.nms.NmsVersion)}.
 *
 * <p>Until then, {@link com.netrust.betterbanner.nms.NmsVersions#detect()}
 * maps this version to {@link com.netrust.betterbanner.nms.NoOpLoomAdapter},
 * so the plugin still loads and only the loom feature is disabled.
 */
package com.netrust.betterbanner.nms.v1_21_R3;
