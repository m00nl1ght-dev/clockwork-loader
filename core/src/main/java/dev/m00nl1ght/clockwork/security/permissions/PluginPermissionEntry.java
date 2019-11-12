package dev.m00nl1ght.clockwork.security.permissions;

import dev.m00nl1ght.clockwork.core.PluginContainer;

import java.security.Permission;
import java.util.function.Consumer;

public interface PluginPermissionEntry {

    void getPermissions(Consumer<Permission> permissions, PluginContainer plugin, String value);

    /**
     * Returns the name of this permission. Used for plugin definition files.
     * If this is a permission that doesn't need to be declared, this method returns null.
     */
    default String getDefName() {
        return null;
    }

}
