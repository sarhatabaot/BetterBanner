package com.netrust.betterbanner;


import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sarhatabaot
 */
public class BannerUtil {
    private BannerUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static List<Material> banners = createBannerList();
    public static boolean isBannerInInventory(Inventory inventory){
        for (Material material: banners){
            if(inventory.contains(material))
                return true;
        }
        return false;
    }

    public static boolean isBanner(Material material) {
        return banners.contains(material);
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
