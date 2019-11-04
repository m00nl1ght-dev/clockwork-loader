package dev.m00nl1ght.clockwork.security;

import dev.m00nl1ght.clockwork.core.PluginContainer;

import java.security.Permission;

public interface PluginPermissionEntry {

    Permission getPermissionFor(PluginContainer plugin);

    default String getDefName() {
        return null;
    }

}
