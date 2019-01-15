package com.netrust.betterbanner;

import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author sarhatabaot
 */
public class BetterBannerRunnable extends BukkitRunnable {
    private final Player player;
    private final BetterBanner plugin;

    public BetterBannerRunnable(BetterBanner plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void run() {
        this.plugin.debug("Max copy: " + Config.copyMax(this.player));
        this.plugin.debug("Max Create: " + Config.createMax(this.player));
        this.plugin.debug("Runnable running");
        if (this.player.isOnline()) {
            InventoryView playerOpenInv = this.player.getOpenInventory();
            if (playerOpenInv != null && (playerOpenInv.getType() == InventoryType.WORKBENCH || playerOpenInv.getType() == InventoryType.CRAFTING)) {
                CraftingInventory wbInventory = (CraftingInventory)playerOpenInv.getTopInventory();
                if (wbInventory != null) {
                    Integer craftingSize = wbInventory.getSize();
                    if (craftingSize == 5 || craftingSize == 10) {
                        Integer deepBannerSlot = 0;
                        int banners = 0;
                        int nonBanners = 0;

                        ItemStack isSingleBanner;
                        for(Integer i = 1; i < craftingSize; i = i + 1) {
                            isSingleBanner = wbInventory.getItem(i);
                            if (isSingleBanner != null && BannerUtil.isBanner(isSingleBanner.getType())) {
                                banners = banners + 1;
                                if (((BannerMeta)isSingleBanner.getItemMeta()).numberOfPatterns() > 5) {
                                    deepBannerSlot = i;
                                }
                            } else if (isSingleBanner != null && isSingleBanner.getType() != Material.AIR) {
                                nonBanners = nonBanners + 1;
                            }
                        }

                        if (banners > 1) {
                            if (deepBannerSlot > 0 && ((BannerMeta)wbInventory.getItem(deepBannerSlot).getItemMeta()).numberOfPatterns() > Config.copyMax(this.player)) {
                                this.plugin.debug("Stopping copy above allowed player limit");
                                wbInventory.clear(0);
                                this.player.updateInventory();
                            }

                        } else if (nonBanners != 0 && deepBannerSlot != 0) {
                            if (((BannerMeta)wbInventory.getItem(deepBannerSlot).getItemMeta()).numberOfPatterns() >= Config.createMax(this.player)) {
                                this.plugin.debug("Stopping (possible) create banner greater than player limit");
                            } else {
                                this.plugin.debug("valid output to handle");
                                ItemStack isDeepBanner = wbInventory.getItem(deepBannerSlot);
                                wbInventory.setItem(deepBannerSlot, new ItemStack(Material.WHITE_BANNER, 1));
                                isSingleBanner = wbInventory.getResult();
                                wbInventory.setItem(deepBannerSlot, isDeepBanner);
                                if (isSingleBanner != null && BannerUtil.isBanner(isSingleBanner.getType())) {
                                    BannerMeta metaSingleBanner = (BannerMeta)isSingleBanner.getItemMeta();
                                    BannerMeta metaDeepBanner = (BannerMeta)isDeepBanner.getItemMeta();
                                    Pattern patternToAdd = metaSingleBanner.getPattern(0);
                                    metaDeepBanner.addPattern(patternToAdd);
                                    isDeepBanner.setItemMeta(metaDeepBanner);
                                    isDeepBanner.setAmount(1);
                                    wbInventory.setItem(0, isDeepBanner);
                                    this.player.updateInventory();
                                    this.plugin.debug("Created a new deeper banner");
                                } else {
                                    this.plugin.debug("Nothing to output, should be empty");
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}
