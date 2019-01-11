package com.netrust.betterbanner;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


import java.util.Iterator;
import java.util.Set;

/**
 * @author sarhatabaot
 */
public class BetterBannerListener implements Listener {
    private BetterBanner plugin;

    public BetterBannerListener(BetterBanner plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    @EventHandler
    public void bbInventoryClickEvent(InventoryClickEvent event) {
        Integer runnableDelay = 1;
        if (!event.isCancelled()) {
            Inventory clickedInv = event.getClickedInventory();
            if (clickedInv instanceof CraftingInventory) {
                int invSize = clickedInv.getSize();
                if (event.getSlot() == 0) {
                    this.plugin.debug("-------- Player clicked the output slot of a crafting window");
                    if (!this.plugin.isMyOutput(clickedInv)) {
                        this.plugin.debug("Not our banner ... let the system handle");
                        return;
                    }

                    int banners = 0;

                    int i;
                    for (i = 1; i < invSize; i = i + 1) {
                        if (clickedInv.getItem(i) != null && BannerUtil.isBanner(clickedInv.getItem(i).getType())) {
                            banners = banners + 1;
                        }
                    }

                    if (banners > 1) {
                        this.plugin.debug("This is a copy of a deep banner ... let system handle it");
                        return;
                    }

                    if (invSize != 10) {
                        this.plugin.debug("**This should not happen, 2x2 crafting and our banner?");
                        return;
                    }

                    this.plugin.debug("The output clicked is our creation");
                    if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                        this.plugin.debug("Remove cancelled, player's cursor is not empty");
                        event.setCancelled(true);
                        return;
                    }

                    this.plugin.debug("We are handling this craft...item->cursor, crafting->each-1");
                    event.setCursor(clickedInv.getItem(0)); //TODO
                    clickedInv.clear(0);

                    for (i = 1; i < 10; i = i + 1) {
                        ItemStack oneStack = clickedInv.getItem(i);
                        if (oneStack != null && oneStack.getAmount() > 0) {
                            if (oneStack.getAmount() > 1) {
                                oneStack.setAmount(oneStack.getAmount() - 1);
                            } else {
                                clickedInv.clear(i);
                            }
                        }
                    }

                    event.setCancelled(true);
                    runnableDelay = 3;
                } else if (invSize != 10) {
                    this.plugin.debug("2x2 entry ... place something, lets continue");
                }

                if (event.getClick() == ClickType.NUMBER_KEY) {
                    this.bbHandleInventory(event, clickedInv, event.getWhoClicked().getInventory().getItem(event.getHotbarButton()), runnableDelay);
                } else {
                    this.bbHandleInventory(event, clickedInv, event.getCursor(), runnableDelay);
                }

            }
        }
    }

    @EventHandler
    public void bbInventoryDragEvent(InventoryDragEvent event) {
        if (!event.isCancelled()) {
            Inventory topInventory = event.getInventory();
            if (topInventory != null
                    && (topInventory.getType() == InventoryType.WORKBENCH || topInventory.getType() == InventoryType.CRAFTING)) {

                Integer craftingSize = topInventory.getSize();
                if (craftingSize == 10 || craftingSize == 5) {
                    Set<Integer> slotsEffected = event.getRawSlots();
                    boolean alteredCrafting = false;
                    Iterator var7 = slotsEffected.iterator();

                    while (var7.hasNext()) {
                        Integer a = (Integer) var7.next();
                        if (a < craftingSize) {
                            alteredCrafting = true;
                            break;
                        }
                    }

                    if (alteredCrafting) {
                        this.bbHandleInventory(event, topInventory, event.getOldCursor(), 1);
                    }
                }
            }

        }
    }

    public void bbHandleInventory(InventoryInteractEvent event, Inventory craftInventory, ItemStack isCursor, Integer runnableDelay) {
        HumanEntity hePlayer = event.getWhoClicked();
        if (hePlayer instanceof Player
                && (BannerUtil.isBannerInInventory(craftInventory) || isCursor != null && BannerUtil.isBanner(isCursor.getType()))) {
                this.plugin.debug("Calling Runnnable delay: " + runnableDelay);
                (new BetterBannerRunnable(this.plugin, (Player) event.getWhoClicked())).runTaskLater(this.plugin, (long) runnableDelay);
        }
    }
}
