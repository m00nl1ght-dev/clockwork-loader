package dev.m00nl1ght.clockwork.security;

import dev.m00nl1ght.clockwork.descriptor.PluginDescriptor;
import dev.m00nl1ght.clockwork.security.permissions.PluginPermissionEntry;

import java.security.Permissions;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityConfiguration {

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

    public Permissions getPermissions(ProtectionDomain domain) {
        return new Permissions(); // TODO
    }

    public Permissions getPermissions(PluginDescriptor plugin) {
        final var perms = new Permissions();

        for (var entry : simplePerms) {
            entry.getPermissions(perms::add, plugin, "");
        }

        for (var str : plugin.getPermissions()) {
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
