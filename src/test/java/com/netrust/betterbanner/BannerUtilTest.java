package com.netrust.betterbanner;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author sarhatabaot
 */
class BannerUtilTest {

    private static List<Material> banners = createBannerList();

    private static Inventory trueInventory;
    private static Inventory falseInventory;
    private static ItemStack[] trueContents = {new ItemStack(Material.BLACK_BANNER), new ItemStack(Material.DIRT)};
    private static ItemStack[] falseContents = {new ItemStack(Material.PUMPKIN), new ItemStack(Material.DIRT)};

    static {
        trueInventory.setContents(trueContents);
        falseInventory.setContents(falseContents);
    }

    @Test
    void isBannerInInventory() {
        assertTrue(BannerUtil.isBannerInInventory(trueInventory));
        assertFalse(BannerUtil.isBannerInInventory(falseInventory));
    }

    @Test
    void isBanner() {
        assertTrue(BannerUtil.isBanner(Material.CYAN_BANNER));
        assertFalse(BannerUtil.isBanner(Material.DIRT));
    }

    private static List<Material> createBannerList(){
        ArrayList<Material> list = new ArrayList<>();
        list.add(Material.BLACK_BANNER);
        list.add(Material.BLUE_BANNER);
        list.add(Material.BROWN_BANNER);
        list.add(Material.CYAN_BANNER);
        list.add(Material.GRAY_BANNER);
        list.add(Material.GREEN_BANNER);
        list.add(Material.LIGHT_BLUE_BANNER);
        list.add(Material.LIGHT_GRAY_BANNER);
        list.add(Material.LIME_BANNER);
        list.add(Material.MAGENTA_BANNER);
        list.add(Material.ORANGE_BANNER);
        list.add(Material.PINK_BANNER);
        list.add(Material.PURPLE_BANNER);
        list.add(Material.RED_BANNER);
        list.add(Material.WHITE_BANNER);
        list.add(Material.YELLOW_BANNER);
        return list;
    }


}