package com.netrust.betterbanner;

/**
 * @author sarhatabaot
 */
public class Permissions {
    private Permissions() {
        throw new UnsupportedOperationException();
    }
    public static final String UNLIMITED = "betterbanner.unlimited";
    public static final String ADVANCED = "betterbanner.advanced";
    public static final String INTERMEDIATE = "betterbanner.intermediate";
    public static final String BASIC = "betterbanner.basic";
    public static final String COMMAND_RELOAD = "betterbanner.reload";
    public static final String COMMAND_DEBUG = "betterbanner.debug";
    public static final String COMMAND_VERSION = "betterbanner.version";

    /**
     * Permission to use {@code /betterbanner debug nms} — dumps the live NMS
     * class layout (declared fields + methods of {@code ContainerLoom} and
     * {@code Container}). More sensitive than the plain {@code debug}
     * toggle because it reveals NMS internals to anyone with the node.
     */
    public static final String COMMAND_DEBUG_NMS = "betterbanner.debug.nms";
}
