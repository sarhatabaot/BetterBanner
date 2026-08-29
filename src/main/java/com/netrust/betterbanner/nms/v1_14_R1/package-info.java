/**
 * Minecraft 1.14 NMS adapter for BetterBanner.
 *
 * <p>This is the only class in the project that touches obfuscated server
 * internals. Per design §14, reflection is used here <em>selectively</em> —
 * to reach private members of {@code ContainerLoom} that have no public
 * API on the server side — and only because Bukkit does not expose the
 * server-side loom's internal state.
 *
 * <p>The methods map directly to design §12 and §13:
 * <ul>
 *   <li>{@code isLoom}        → active container is a {@code ContainerLoom}</li>
 *   <li>{@code getState}      → reads banner/dye/pattern/selection slots</li>
 *   <li>{@code setResult}     → writes the output slot directly</li>
 *   <li>{@code synchronize}   → calls {@code detectAndSendChanges()} so the
 *       client receives the slot update</li>
 * </ul>
 *
 * <p>On 1.14, the obfuscated getters are {@code e()}, {@code f()}, {@code g()},
 * {@code h()}, and {@code i()}. Their semantics (banner / dye / pattern item /
 * result / selected pattern index) are documented in the reflection lookups
 * below and are an intentional one-time mapping per version, not a generic
 * engine.
 */
package com.netrust.betterbanner.nms.v1_14_R1;
