package dev.m00nl1ght.clockwork.security;

import dev.m00nl1ght.clockwork.descriptor.PluginDescriptor;

import java.security.Permission;
import java.util.Set;

public interface PermissionsFactory {

    Set<Permission> buildPermission(PluginDescriptor plugin, String params);

    default boolean mustBeDeclared() {
        return true;
    }

}
