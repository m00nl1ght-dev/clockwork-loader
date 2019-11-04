package dev.m00nl1ght.clockwork.security;

import dev.m00nl1ght.clockwork.core.PluginContainer;

import java.security.Permissions;
import java.util.ArrayList;
import java.util.List;

public final class SecurityConfiguration {

    private final List<PluginPermissionEntry> pluginPerms = new ArrayList<>();

    public void addPermission(PluginPermissionEntry perm) {
        pluginPerms.add(perm);
    }

    public Permissions getPermissionsFor(PluginContainer plugin) {
        final var perms = new Permissions();
        for (var entry : pluginPerms) {
            final var ret = entry.getPermissionFor(plugin);
            if (ret != null) perms.add(ret);
        }
        return perms;
    }

}
