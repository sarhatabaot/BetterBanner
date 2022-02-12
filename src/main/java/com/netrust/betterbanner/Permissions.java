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
}
