package com.github.sarhatabaot.loomapi;

import org.bukkit.block.banner.Pattern;

/**
 * @author sarhatabaot
 */
public interface LoomWrapper {
    void forceUpdatePattern();
    void forceLoadPatterns();
    void addPattern(Pattern pattern);

}
