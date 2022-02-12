package com.netrust.betterbanner;


import com.google.common.collect.ImmutableList;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

/**
 * @author sarhatabaot
 */
public class BannerUtil {
    private static final ImmutableList<Material> banners = ImmutableList.of(
            Material.BLACK_BANNER,
            Material.BLUE_BANNER,
            Material.BROWN_BANNER,
            Material.CYAN_BANNER,
            Material.GRAY_BANNER,
            Material.GREEN_BANNER,
            Material.LIGHT_BLUE_BANNER,
            Material.LIGHT_GRAY_BANNER,
            Material.LIME_BANNER,
            Material.MAGENTA_BANNER,
            Material.ORANGE_BANNER,
            Material.PINK_BANNER,
            Material.PURPLE_BANNER,
            Material.RED_BANNER,
            Material.WHITE_BANNER,
            Material.YELLOW_BANNER
    );

    private BannerUtil() {
        throw new UnsupportedOperationException();
    }



    public static boolean isBannerInInventory(Inventory inventory) {
        for (Material material : banners) {
            if (inventory.contains(material))
                return true;
        }
        return false;
    }

    public static boolean isBanner(Material material) {
        return banners.contains(material);
    }
}
