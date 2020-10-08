package dev.m00nl1ght.clockwork.security.permissions;

import dev.m00nl1ght.clockwork.descriptor.PluginDescriptor;

import java.security.Permission;
import java.util.function.Consumer;

public interface PluginPermissionEntry {

    void getPermissions(Consumer<Permission> permissions, PluginDescriptor plugin, String value);

    /**
     * Returns the name of this permission. Used for plugin definition files.
     * If this is a permission that doesn't need to be declared, this method returns null.
     */
    default String getDefName() {
        return null;
    }

}
