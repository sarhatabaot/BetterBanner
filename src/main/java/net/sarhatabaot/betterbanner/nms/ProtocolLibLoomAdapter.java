package net.sarhatabaot.betterbanner.nms;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.sarhatabaot.betterbanner.banner.BannerService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * {@link LoomAdapter} that wraps {@link BukkitLoomAdapter} with ProtocolLib
 * packet interception to eliminate the visual flicker of Strategy B.
 *
 * <p><b>What it does</b>: registers a {@link PacketAdapter} for
 * {@link PacketType.Play.Server#SET_SLOT} and
 * {@link PacketType.Play.Server#WINDOW_ITEMS}. When the server sends a
 * banner with 6+ patterns to a player who is currently in a loom, this
 * adapter rewrites the outgoing item so the client sees a shallow copy
 * with only 5 patterns. The server-side state is completely untouched.
 *
 * <p>Because the client always sees 5 patterns, {@code hasMaxPatterns} is
 * always {@code false} on the client side — the pattern-selection grid
 * never hides, and there is zero visual disruption from Strategy B's
 * shallow-banner swap.
 *
 * <p><b>Fallback</b>: if ProtocolLib is not installed on the server,
 * {@link NmsVersions#detect(Plugin)} returns a plain
 * {@link BukkitLoomAdapter} instead.
 */
public final class ProtocolLibLoomAdapter extends BukkitLoomAdapter {

    /** */
    public ProtocolLibLoomAdapter(@NotNull Plugin plugin) {
        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
        pm.addPacketListener(new PacketAdapter(plugin,
                PacketType.Play.Server.SET_SLOT,
                PacketType.Play.Server.WINDOW_ITEMS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                handlePacket(event);
            }
        });
    }

    private void handlePacket(@NotNull PacketEvent event) {
        Player player = event.getPlayer();
        if (player == null || !player.isOnline()) {
            return;
        }

        // Only intercept when the player is interacting with a loom.
        if (player.getOpenInventory().getTopInventory().getType() != InventoryType.LOOM) {
            return;
        }

        PacketType type = event.getPacketType();
        PacketContainer packet = event.getPacket();
        ItemStack original;
        ItemStack spoofed;

        if (type == PacketType.Play.Server.SET_SLOT) {
            // SET_SLOT: integers = [windowId, slotIndex], itemModifier = [item]
            int slotIndex = packet.getIntegers().read(1);
            if (slotIndex != BANNER_SLOT_INDEX) {
                return;
            }
            original = packet.getItemModifier().read(0);
            spoofed = spoofIfNeeded(original);
            if (spoofed != original) {
                packet.getItemModifier().write(0, spoofed);
            }
        } else if (type == PacketType.Play.Server.WINDOW_ITEMS) {
            // WINDOW_ITEMS: itemListModifier = [items]
            List<ItemStack> items = packet.getItemListModifier().read(0);
            if (items == null || items.size() <= BANNER_SLOT_INDEX) {
                return;
            }
            original = items.get(BANNER_SLOT_INDEX);
            spoofed = spoofIfNeeded(original);
            if (spoofed != original) {
                items.set(BANNER_SLOT_INDEX, spoofed);
                packet.getItemListModifier().write(0, items);
            }
        }
    }

    /**
     * If {@code stack} is a banner with 6+ patterns, return a shallow
     * clone showing only 5 patterns. Otherwise return the input unchanged.
     */
    @Nullable
    private static ItemStack spoofIfNeeded(@Nullable ItemStack stack) {
        int count = BannerService.getPatternCount(stack);
        if (count < BannerService.VANILLA_MAX_PATTERNS) {
            return stack;
        }
        return BannerService.buildShallowBanner(stack, BannerService.VANILLA_MAX_PATTERNS - 1);
    }

    @Override
    public void dumpDiagnostics(@NotNull CommandSender sender) {
        sender.sendMessage("BetterBanner adapter: " + getClass().getName()
                + " (ProtocolLib packet interception ACTIVE — seamless loom experience)");
    }
}