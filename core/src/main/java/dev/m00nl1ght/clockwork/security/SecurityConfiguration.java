package dev.m00nl1ght.clockwork.security;

import dev.m00nl1ght.clockwork.core.LoadedPlugin;
import dev.m00nl1ght.clockwork.security.permissions.PluginPermissionEntry;

import java.security.Permissions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SecurityConfiguration {

    private final List<PluginPermissionEntry> simplePerms = new ArrayList<>(3);
    private final Map<String, PluginPermissionEntry> declaredPerms = new HashMap<>(3);

    public void addPermission(PluginPermissionEntry perm) {
        final var def = perm.getDefName();
        if (def == null) {
            simplePerms.add(perm);
        } else {
            declaredPerms.put(def, perm);
        }
    }

    public Permissions getPermissionsFor(LoadedPlugin plugin) {
        final var perms = new Permissions();

        for (var entry : simplePerms) {
            entry.getPermissions(perms::add, plugin, "");
        }

        for (var str : plugin.getDescriptor().getPermissions()) {
            var i = str.indexOf(':');
            var perm = i < 0 ? str : str.substring(0, i);
            var value = i < 0 ? "" : str.substring(i + 1);
            var entry = declaredPerms.get(perm);
            if (entry == null) continue;
            entry.getPermissions(perms::add, plugin, value);
        }

        return perms;
    }

}
