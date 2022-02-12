package com.netrust.betterbanner;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;


/**
 * @author sarhatabaot
 */
public class Config {
    private static Integer permBasic = 0;
    private static Integer permDefault = 0;
    private static Integer permIntermediate = 0;
    private static Integer permAdvanced = 0;
    private static Integer permCopy = 0;

    private Config() {
        throw new UnsupportedOperationException();
    }

    public static void load(BetterBanner plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        permDefault = config.getInt("default", 6);
        if (permDefault < 6) {
            permDefault = 6;
        }

        permBasic = config.getInt("basic", 8);
        if (permBasic < 7) {
            permBasic = 7;
        }

        permIntermediate = config.getInt("intermediate", 11);
        if (permIntermediate < permBasic) {
            permIntermediate = permBasic;
        }

        permAdvanced = config.getInt("advanced", 15);
        if (permAdvanced < permIntermediate) {
            permAdvanced = permIntermediate;
        }

        String copy = config.getString("copy", "none");
        if (copy.equalsIgnoreCase("perm")) {
            permCopy = 1;
        } else if (copy.equalsIgnoreCase("all")) {
            permCopy = 2;
        } else {
            permCopy = 0;
        }

        plugin.getLogger().info("Config loaded: d: " + permDefault + " b:" + permBasic + " i:" + permIntermediate + " a:" + permAdvanced + " copy:" + permCopy);
    }

    public static int maxDefault() {
        return permDefault;
    }

    public static int maxBasic() {
        return permBasic;
    }

    public static int maxIntermediate() {
        return permIntermediate;
    }

    public static int maxAdvanced() {
        return permAdvanced;
    }

    public static boolean copyAll() {
        return permCopy == 2;
    }

    public static boolean copyNone() {
        return permCopy == 0;
    }

    public static boolean copyPerm() {
        return permCopy == 1;
    }

    public static int copyMax(Player p) {
        if (copyAll()) {
            return 999;
        }

        if (copyPerm()) {
            if (p.hasPermission(Permissions.UNLIMITED)) {
                return 999;
            }

            if (p.hasPermission(Permissions.ADVANCED)) {
                return maxAdvanced();
            }

            if (p.hasPermission(Permissions.INTERMEDIATE)) {
                return maxIntermediate();
            }

            if (p.hasPermission(Permissions.BASIC)) {
                return maxBasic();
            }
        }

        return maxDefault();

    }

    public static int createMax(final Player player) {
        if (player.hasPermission(Permissions.UNLIMITED)) {
            return 999;
        }
        if (player.hasPermission(Permissions.ADVANCED)) {
            return maxAdvanced();
        }
        if (player.hasPermission(Permissions.INTERMEDIATE)) {
            return maxIntermediate();
        }

        return player.hasPermission(Permissions.BASIC) ? maxBasic() : maxDefault();
    }
}
