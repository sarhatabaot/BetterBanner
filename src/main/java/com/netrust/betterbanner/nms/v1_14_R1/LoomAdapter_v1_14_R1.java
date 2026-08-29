package com.netrust.betterbanner.nms.v1_14_R1;

import com.netrust.betterbanner.loom.LoomState;
import com.netrust.betterbanner.nms.LoomAdapter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;

/**
 * 1.14 implementation of {@link LoomAdapter}.
 *
 * <p>No compile-time dependency on any
 * {@code org.bukkit.craftbukkit.v1_14_R1.*} class. All NMS touch points
 * are reached reflectively.
 *
 * <p>Per design section 13, reflection is intentionally narrow: it is
 * the smallest bridge that lets generic BetterBanner code treat the loom
 * uniformly. It is not a universal reflection engine.
 *
 * <p>If a critical piece of NMS state cannot be located, the
 * constructor logs a detailed diagnostic dump and returns a
 * partially-functional adapter: the plugin still loads, but loom
 * intervention is disabled. The same diagnostic can be triggered at
 * runtime via {@code /betterbanner debug nms} - see
 * {@link #dumpDiagnostics(CommandSender)}.
 */
public final class LoomAdapter_v1_14_R1 implements LoomAdapter {

    /**
     * Cached declared {@code int} field on {@code ContainerLoom} that
     * holds the player's selected pattern index. {@code null} if no such
     * field could be located; the loom feature is disabled in that case.
     */
    @Nullable
    private final Field containerLoomSelectedPatternField;

    /**
     * Cached {@code Slot[]} (or {@code IInventory[]}) field on
     * {@code Container} (the slots array). {@code null} if not located.
     */
    @Nullable
    private final Field containerSlotsField;

    /**
     * Cached {@code Slot} component class. Used to identify the right
     * field by component type.
     */
    @Nullable
    private final Class<?> slotArrayComponentType;

    /**
     * A list of well-known {@code CraftItemStack} static methods that
     * convert an NMS {@code ItemStack} to a Bukkit {@link ItemStack}.
     * The first one that returns a non-null result that casts cleanly
     * to Bukkit {@code ItemStack} is used. On real Spigot 1.14.4 the
     * primary {@code asBukkitCopy} method can throw at runtime
     * (different obfuscation or class-loader state); having fallbacks
     * lets us recover without a code change.
     */
    private final java.util.List<Method> asBukkitCopyAlternatives = new java.util.ArrayList<>();
    @Nullable
    private final Method craftItemStackAsNMSCopy;
    @Nullable
    private final Method craftPlayerGetHandle;

    /**
     * Optional debug consumer; if non-null, the adapter emits per-read
     * trace messages (slot lengths, per-slot null/non-null status) here
     * so the plugin can log them. Set via {@link #setDebugLogger(Consumer)}.
     */
    @Nullable
    private Consumer<String> debugLogger;

    private static final int BANNER_SLOT = 0;
    private static final int DYE_SLOT = 1;
    private static final int PATTERN_ITEM_SLOT = 2;
    private static final int RESULT_SLOT = 3;

    public LoomAdapter_v1_14_R1() {
        Field selectedPatternField = null;
        Field slotsField = null;
        Method asNMSCopy = null;
        Method getHandle = null;
        Class<?> slotComponent = null;

        try {
            // Resolve NMS classes through Spigot's class loader, not our
            // plugin's. The NMS class loader is reachable via any NMS
            // class (e.g. ContainerLoom) since those are loaded by
            // Spigot. This is critical for the CraftItemStack helpers:
            // CraftItemStack.asBukkitCopy takes a Spigot-loaded
            // net.minecraft.server.v1_14_R1.ItemStack parameter, and if
            // we resolve that parameter type via our plugin's class
            // loader, Method.invoke throws IllegalArgumentException at
            // runtime because the two ItemStack classes don't match.
            Class<?> containerLoom = Class.forName("net.minecraft.server.v1_14_R1.ContainerLoom");
            ClassLoader spigotCl = containerLoom.getClassLoader();

            Class<?> container = Class.forName("net.minecraft.server.v1_14_R1.Container", true, spigotCl);
            Class<?> nmsItemStack = Class.forName("net.minecraft.server.v1_14_R1.ItemStack", true, spigotCl);

            slotsField = findListField(container);
            if (slotsField == null) {
                // Fallback: on builds where Container.slots is still a
                // Slot[] or IInventory[] array (older / variant
                // mappings), use the array-based scan.
                slotsField = findArrayFieldByComponentClassName(container,
                        "net.minecraft.server.v1_14_R1.Slot");
                if (slotsField == null) {
                    slotsField = findArrayFieldByComponentClassName(container,
                            "net.minecraft.server.v1_14_R1.IInventory");
                }
            }
            if (slotsField != null) {
                slotsField.setAccessible(true);
                slotComponent = slotsField.getType().getComponentType();
            }

            selectedPatternField = findIntField(containerLoom);
            if (selectedPatternField != null) {
                selectedPatternField.setAccessible(true);
            }

            try {
                // Resolve CraftItemStack via Spigot's class loader too,
                // so its as*Copy methods see Spigot-loaded ItemStack.
                Class<?> craftItemStack = Class.forName("org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack", true, spigotCl);
                // Cache asBukkitCopy (and a couple of fallbacks). The
                // first one that returns a non-null result that casts
                // cleanly to Bukkit ItemStack is used.
                for (String name : new String[] { "asBukkitCopy", "asCraftMirror", "asNewCraftStack", "asCraftCopy" }) {
                    try {
                        Method m = craftItemStack.getMethod(name, nmsItemStack);
                        asBukkitCopyAlternatives.add(m);
                    } catch (NoSuchMethodException ignored) {
                        // not present on this build; try the next
                    }
                }
                // asNMSCopy takes a Bukkit ItemStack. Our plugin's
                // class loader is fine for the parameter type because
                // ItemStack is also a Bukkit class (not a Spigot one).
                asNMSCopy = craftItemStack.getMethod("asNMSCopy", ItemStack.class);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("BetterBanner: 1.14 NMS classes not found on classpath", e);
            } catch (NoSuchMethodException e) {
                logNmsClassUnavailable("CraftItemStack", e);
            }

            // CraftPlayer is also a Spigot re-spigoted class; resolve
            // through Spigot's class loader for consistency.
            Class<?> craftPlayer = Class.forName("org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer", true, spigotCl);
            getHandle = craftPlayer.getMethod("getHandle");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("BetterBanner: 1.14 NMS classes not found on classpath", e);
        } catch (NoSuchMethodException e) {
            logNmsClassUnavailable("CraftPlayer#getHandle", e);
        }

        this.containerLoomSelectedPatternField = selectedPatternField;
        this.containerSlotsField = slotsField;
        this.slotArrayComponentType = (slotComponent != null) ? slotComponent : guessSlotComponent();
        this.craftItemStackAsNMSCopy = asNMSCopy;
        this.craftPlayerGetHandle = getHandle;
    }

    @Override
    public boolean isLoom(@NotNull Player player) {
        try {
            Object handle = nmsHandleOf(player);
            if (handle == null) {
                return false;
            }
            Object activeContainer = activeContainerOf(handle);
            return activeContainer != null
                    && activeContainer.getClass().getName().equals("net.minecraft.server.v1_14_R1.ContainerLoom");
        } catch (Throwable t) {
            return false;
        }
    }

    @Nullable
    @Override
    public LoomState getState(@NotNull Player player) {
        try {
            Object handle = nmsHandleOf(player);
            if (handle == null) {
                return null;
            }
            Object activeContainer = activeContainerOf(handle);
            if (activeContainer == null
                    || !activeContainer.getClass().getName().equals("net.minecraft.server.v1_14_R1.ContainerLoom")) {
                return null;
            }
            Object[] slots = readSlotsField(activeContainer);
            if (slots == null || slots.length <= RESULT_SLOT) {
                return null;
            }

            ItemStack banner = readSlot(slots[BANNER_SLOT]);
            ItemStack dye = readSlot(slots[DYE_SLOT]);
            ItemStack patternItem = readSlot(slots[PATTERN_ITEM_SLOT]);
            ItemStack result = readSlot(slots[RESULT_SLOT]);

            int selected = readSelectedPatternField(activeContainer);

            return new LoomState(banner, dye, patternItem, result, selected);
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    public void setResult(@NotNull Player player, @NotNull ItemStack result) {
        writeSlot(player, RESULT_SLOT, result);
    }

    @Override
    public void setSlot(int slotIndex, @NotNull Player player, @NotNull ItemStack item) {
        if (slotIndex < 0) {
            return;
        }
        writeSlot(player, slotIndex, item);
    }

    /**
     * Convert a Bukkit {@link ItemStack} to an NMS {@code ItemStack} and
     * write it into the specified slot of the player's current loom
     * container. The same low-level path is used for both
     * {@link #setResult} and {@link #setSlot}.
     */
    private void writeSlot(@NotNull Player player, int slotIndex, @NotNull ItemStack item) {
        try {
            Object handle = nmsHandleOf(player);
            if (handle == null) {
                return;
            }
            Object activeContainer = activeContainerOf(handle);
            if (activeContainer == null
                    || !activeContainer.getClass().getName().equals("net.minecraft.server.v1_14_R1.ContainerLoom")) {
                return;
            }
            Object[] slots = readSlotsField(activeContainer);
            if (slots == null || slotIndex >= slots.length) {
                return;
            }
            Object targetSlot = slots[slotIndex];
            if (targetSlot == null) {
                return;
            }
            Object nmsStack;
            if (craftItemStackAsNMSCopy != null) {
                try {
                    nmsStack = craftItemStackAsNMSCopy.invoke(null, item);
                } catch (ReflectiveOperationException roe) {
                    return;
                }
            } else {
                return;
            }
            if (nmsStack == null) {
                return;
            }
            // Use the Slot#set method. The parameter type is the
            // Spigot-loaded NMS ItemStack, which we resolved through
            // Spigot's class loader in the constructor; the lookup
            // here will find a method whose parameter type matches
            // the actual NMS stack class.
            Method slotSet = targetSlot.getClass().getMethod("set", nmsStack.getClass());
            slotSet.invoke(targetSlot, nmsStack);
        } catch (Throwable t) {
            // best-effort
        }
    }

    @Override
    public void synchronize(@NotNull Player player) {
        try {
            Object handle = nmsHandleOf(player);
            if (handle == null) {
                return;
            }
            Object activeContainer = activeContainerOf(handle);
            if (activeContainer == null) {
                return;
            }
            Method detect = activeContainer.getClass().getMethod("detectAndSendChanges");
            detect.invoke(activeContainer);
        } catch (Throwable t) {
            // best-effort
        }
    }


    // ----- debug / diagnostics -----

    /**
     * Dump a human-readable summary of what this adapter resolved and
     * what the live NMS classes (ContainerLoom, Container) expose. Used
     * by the {@code /betterbanner debug nms} command.
    /**
     * Install a debug consumer. The adapter will emit per-read trace
     * messages (slot lengths, per-slot null/non-null status, NMS write
     * outcomes) through this consumer. Pass {@code null} to silence
     * debug output.
     */
    public void setDebugLogger(@Nullable Consumer<String> debugLogger) {
        this.debugLogger = debugLogger;
    }

    private void debug(@NotNull String msg) {
        if (debugLogger != null) {
            debugLogger.accept(msg);
        }
    }


    public void dumpDiagnostics(@NotNull CommandSender sender) {
        sender.sendMessage("BetterBanner NMS diagnostics (1.14 adapter):");
        sender.sendMessage("  Adapter class: " + getClass().getName());
        sender.sendMessage("  Resolved CraftPlayer#getHandle: " + (craftPlayerGetHandle != null));
        sender.sendMessage("  Resolved CraftItemStack NMS->Bukkit methods: " + asBukkitCopyAlternatives.size()
                + " (" + methodNames(asBukkitCopyAlternatives) + ")");
        sender.sendMessage("  Resolved CraftItemStack#asNMSCopy: " + (craftItemStackAsNMSCopy != null));
        sender.sendMessage("  Resolved Container#slots field: " + describeField(containerSlotsField));
        sender.sendMessage("  Resolved ContainerLoom#selectedPattern field: " + describeField(containerLoomSelectedPatternField));
        sender.sendMessage("  Resolved slots array component type: " +
                (slotArrayComponentType != null ? slotArrayComponentType.getName() : "null"));

        sender.sendMessage("  Declared fields of ContainerLoom (and superclasses):");
        try {
            Class<?> containerLoom = Class.forName("net.minecraft.server.v1_14_R1.ContainerLoom");
            for (Class<?> klass = containerLoom; klass != null && klass != Object.class; klass = klass.getSuperclass()) {
                sender.sendMessage("    --- " + klass.getName() + " ---");
                for (Field f : klass.getDeclaredFields()) {
                    sender.sendMessage("      " + f);
                }
            }
        } catch (ClassNotFoundException e) {
            sender.sendMessage("    (ContainerLoom not on classpath)");
        }

        sender.sendMessage("  Declared methods of ContainerLoom (and superclasses):");
        try {
            Class<?> containerLoom = Class.forName("net.minecraft.server.v1_14_R1.ContainerLoom");
            for (Class<?> klass = containerLoom; klass != null && klass != Object.class; klass = klass.getSuperclass()) {
                sender.sendMessage("    --- " + klass.getName() + " ---");
                for (Method m : klass.getDeclaredMethods()) {
                    sender.sendMessage("      " + m);
                }
            }
        } catch (ClassNotFoundException e) {
            sender.sendMessage("    (ContainerLoom not on classpath)");
        }

        sender.sendMessage("  Declared fields of Container (and superclasses):");
        try {
            Class<?> container = Class.forName("net.minecraft.server.v1_14_R1.Container");
            for (Class<?> klass = container; klass != null && klass != Object.class; klass = klass.getSuperclass()) {
                sender.sendMessage("    --- " + klass.getName() + " ---");
                for (Field f : klass.getDeclaredFields()) {
                    sender.sendMessage("      " + f);
                }
            }
        } catch (ClassNotFoundException e) {
            sender.sendMessage("    (Container not on classpath)");
        }
    }


    // ----- helpers -----

    @Nullable
    private Object nmsHandleOf(@NotNull Player player) {
        if (craftPlayerGetHandle == null) {
            return null;
        }
        try {
            return craftPlayerGetHandle.invoke(player);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @Nullable
    private static Object activeContainerOf(@NotNull Object entityPlayerHandle) {
        try {
            Field f = entityPlayerHandle.getClass().getField("activeContainer");
            return f.get(entityPlayerHandle);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @Nullable
    private Object[] readSlotsField(@NotNull Object container) {
        if (containerSlotsField == null) {
            debug("readSlotsField: containerSlotsField is null");
            return null;
        }
        try {
            Object value = containerSlotsField.get(container);
            if (value == null) {
                debug("readSlotsField: field value is null on " + container.getClass().getSimpleName());
                return null;
            }
            // Spigot 1.14.4 stores Container.slots as a java.util.List
            // (specifically a NonNullList<Slot> at runtime). Older or
            // variant builds may use a plain Slot[]. Handle both.
            if (value instanceof java.util.List) {
                java.util.List<?> list = (java.util.List<?>) value;
                Object[] arr = list.toArray();
                debug("readSlotsField: list size=" + arr.length
                        + " slot0=" + (arr.length > 0 ? String.valueOf(arr[0]) : "n/a")
                        + " slot1=" + (arr.length > 1 ? String.valueOf(arr[1]) : "n/a")
                        + " slot2=" + (arr.length > 2 ? String.valueOf(arr[2]) : "n/a")
                        + " slot3=" + (arr.length > 3 ? String.valueOf(arr[3]) : "n/a")
                        + " last=" + (arr.length > 0 ? String.valueOf(arr[arr.length - 1]) : "n/a"));
                return arr;
            }
            if (value instanceof Object[]) {
                Object[] arr = (Object[]) value;
                debug("readSlotsField: array size=" + arr.length);
                return arr;
            }
            debug("readSlotsField: value is " + value.getClass().getName() + " (not List or Object[])");
        } catch (ReflectiveOperationException e) {
            debug("readSlotsField: reflection failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        return null;
    }

    private int readSelectedPatternField(@NotNull Object containerLoomInstance) {
        if (containerLoomSelectedPatternField == null) {
            return -1;
        }
        try {
            Object value = containerLoomSelectedPatternField.get(containerLoomInstance);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return -1;
    }

    @Nullable
    private static Field findArrayFieldByComponentClassName(@NotNull Class<?> owner, @NotNull String
            componentClassName) {
        for (Class<?> klass = owner; klass != null && klass != Object.class; klass = klass.getSuperclass()) {
            for (Field f : klass.getDeclaredFields()) {
                Class<?> type = f.getType();
                if (type.isArray() && type.getComponentType().getName().equals(componentClassName)) {
                    return f;
                }
            }
        }
        // Diagnostic: dump every array field, every field named "slots"
        // (a common name in Mojang code regardless of the actual storage
        // type), and a complete one-line summary of every field in the
        // hierarchy. The user explicitly asked to "see what's available"
        // so we err on the side of too much information.
        StringBuilder dump = new StringBuilder();
        dump.append("[BetterBanner] Could not find an array field of component type ")
                .append(componentClassName)
                .append(" on ").append(owner.getName())
                .append(" (or any of its superclasses).");

        // 1. Any field literally named "slots", regardless of type.
        dump.append("\n  Fields named 'slots' in the hierarchy:");
        boolean anySlots = false;
        for (Class<?> klass = owner; klass != null && klass != Object.class; klass = klass.getSuperclass()) {
            for (Field f : klass.getDeclaredFields()) {
                if (f.getName().equals("slots")) {
                    anySlots = true;
                    dump.append("\n    [").append(klass.getSimpleName()).append("] ")
                            .append(f).append(" (declaring type ").append(f.getType().getName()).append(")");
                }
            }
        }
        if (!anySlots) {
            dump.append("\n    (none)");
        }

        // 2. Every declared array field, with its component type.
        dump.append("\n  All declared array fields in the hierarchy:");
        boolean any = false;
        for (Class<?> klass = owner; klass != null && klass != Object.class; klass = klass.getSuperclass()) {
            for (Field f : klass.getDeclaredFields()) {
                if (f.getType().isArray()) {
                    any = true;
                    dump.append("\n    [").append(klass.getSimpleName()).append("] ")
                            .append(f).append(" component=")
                            .append(f.getType().getComponentType().getName());
                }
            }
        }
        if (!any) {
            dump.append("\n    (none)");
        }

        // 3. One-line summary of every field in the hierarchy, grouped
        // by declaring class. This is the most direct answer to "see
        // what's available" — every field, every type.
        dump.append("\n  Every declared field in the hierarchy:");
        for (Class<?> klass = owner; klass != null && klass != Object.class; klass = klass.getSuperclass()) {
            dump.append("\n    --- ").append(klass.getName()).append(" ---");
            for (Field f : klass.getDeclaredFields()) {
                dump.append("\n      ").append(f);
            }
        }
        Bukkit.getLogger().warning(dump.toString());
        return null;
    }

    /**
     * Find a declared field on {@code owner} (or any superclass) whose
     * type is a {@link java.util.List}. On Spigot 1.14.4, {@code Container.slots}
     * is a {@code public java.util.List} (declared as raw {@code List}; at
     * runtime it's a {@code NonNullList<Slot>}). Older or variant Spigot
     * builds may use a plain array instead, which is why this helper is
     * the primary path and {@link #findArrayFieldByComponentClassName} is
     * a fallback.
     *
     * <p>Returns the first matching field found, or {@code null} if
     * nothing matches. Logs no diagnostic on success; on failure the
     * caller is expected to fall back to the array-based scan, which
     * has its own diagnostic dump.
     */
    @Nullable
    private static Field findListField(@NotNull Class<?> owner) {
        for (Class<?> klass = owner; klass != null && klass != Object.class; klass = klass.getSuperclass()) {
            for (Field f : klass.getDeclaredFields()) {
                if (java.util.List.class.isAssignableFrom(f.getType())) {
                    return f;
                }
            }
        }
        return null;
    }



    @Nullable
    private static Field findIntField(@NotNull Class<?> owner) {
        for (Class<?> klass = owner; klass != null && klass != Object.class; klass = klass.getSuperclass()) {
            for (Field f : klass.getDeclaredFields()) {
                if (f.getType() == int.class && !Modifier.isStatic(f.getModifiers())) {
                    return f;
                }
            }
        }
        // Diagnostic: dump every int/Integer field across the hierarchy,
        // plus every ContainerProperty field (the likely actual storage
        // type on Spigot 1.14.x per the access$4 NMS diagnostic), plus
        // a one-line summary of every declared field's type for context.
        StringBuilder dump = new StringBuilder();
        dump.append("[BetterBanner] Could not find a non-static int field on ")
                .append(owner.getName())
                .append(" (or any of its superclasses).");
        boolean any = false;
        for (Class<?> klass = owner; klass != null && klass != Object.class; klass = klass.getSuperclass()) {
            for (Field f : klass.getDeclaredFields()) {
                if (f.getType() == int.class || f.getType() == Integer.class) {
                    any = true;
                    dump.append("\n    [").append(klass.getSimpleName()).append("] int/Integer ")
                            .append(f).append(" static=")
                            .append(Modifier.isStatic(f.getModifiers()));
                }
            }
        }
        if (!any) {
            dump.append("\n    (no int/Integer fields in hierarchy)");
        }
        // Also list ContainerProperty fields (a likely candidate for
        // the loom's "selected pattern" storage on Spigot 1.14.x).
        try {
            Class<?> containerProperty = Class.forName("net.minecraft.server.v1_14_R1.ContainerProperty");
            boolean propAny = false;
            dump.append("\n  ContainerProperty fields (loom selection state):");
            for (Class<?> klass = owner; klass != null && klass != Object.class; klass = klass.getSuperclass()) {
                for (Field f : klass.getDeclaredFields()) {
                    if (containerProperty.isAssignableFrom(f.getType())) {
                        propAny = true;
                        dump.append("\n    [").append(klass.getSimpleName()).append("] ")
                                .append(f).append(" type=").append(f.getType().getName());
                    }
                }
            }
            if (!propAny) {
                dump.append("\n    (none)");
            }
        } catch (ClassNotFoundException ignored) {
            // ContainerProperty not on this classpath; skip that section.
        }
        Bukkit.getLogger().warning(dump.toString());
        return null;
    }

    @Nullable
    private ItemStack readSlot(@Nullable Object slot) {
        if (slot == null) {
            debug("readSlot: slot is null");
            return null;
        }
        try {
            Method getItem = slot.getClass().getMethod("getItem");
            Object nmsStack = getItem.invoke(slot);
            if (nmsStack == null) {
                debug("readSlot: slot " + slot.getClass().getSimpleName()
                        + " returned null ItemStack (slot is empty)");
                return null;
            }
            // On Spigot 1.14.4, an empty slot returns an NMS ItemStack
            // whose runtime class is ItemAir (the empty singleton).
            // CraftItemStack.asBukkitCopy on this build rejects ItemAir
            // with a ClassCastException, so short-circuit empty slots.
            String nmsClassName = nmsStack.getClass().getName();
            if (nmsClassName.endsWith(".ItemAir") || nmsClassName.equals("net.minecraft.server.v1_14_R1.ItemAir")) {
                return null;
            }
            if (asBukkitCopyAlternatives.isEmpty()) {
                debug("readSlot: no CraftItemStack NMS->Bukkit methods available");
                return null;
            }
            // Try each alternative in order. The first one that returns a
            // non-null result that casts cleanly to Bukkit ItemStack wins.
            // On real Spigot 1.14.4 the primary asBukkitCopy can throw at
            // runtime (different class-loader state or signature drift);
            // having fallbacks avoids a hard failure.
            for (Method m : asBukkitCopyAlternatives) {
                try {
                    Object bukkit = m.invoke(null, nmsStack);
                    if (bukkit != null) {
                        return (ItemStack) bukkit;
                    }
                    debug("readSlot: " + m.getName() + " returned null");
                } catch (Throwable inner) {
                    debug("readSlot: " + m.getName() + " failed: "
                            + inner.getClass().getSimpleName() + ": " + inner.getMessage());
                    // try the next alternative
                }
            }
            debug("readSlot: all CraftItemStack methods failed for nmsStack of type " + nmsClassName);
            return null;
        } catch (Throwable t) {
            debug("readSlot: " + t.getClass().getSimpleName() + ": " + t.getMessage());
            return null;
        }
    }

    @Nullable
    private static Class<?> guessSlotComponent() {
        try {
            return Class.forName("net.minecraft.server.v1_14_R1.Slot");
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static String describeField(@Nullable Field f) {
        if (f == null) {
            return "null";
        }
        return f.getDeclaringClass().getName() + "#" + f.getName() + " : " + f.getType().getName();
    }

    /** Comma-separated method names of a list of {@link Method}s, for diagnostics. */
    @NotNull
    private static String methodNames(@NotNull java.util.List<Method> methods) {
        if (methods.isEmpty()) {
            return "none";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < methods.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(methods.get(i).getName());
        }
        return sb.toString();
    }

    private static void logNmsClassUnavailable(@NotNull String what, @NotNull Throwable cause) {
        Bukkit.getLogger().warning("[BetterBanner] " + what + " not available: " + cause.getMessage());
    }
}

