package dev.m00nl1ght.clockwork.security;

import dev.m00nl1ght.clockwork.core.LoadedPlugin;

import java.security.Permission;
import java.security.Permissions;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class SecurityConfig {

    private final Permissions sharedPermissions;
    private final Map<String, PermissionsFactory> declarablePermissions;

    private SecurityConfig(Builder builder) {
        this.sharedPermissions = builder.sharedPermissions;
        this.sharedPermissions.setReadOnly();
        this.declarablePermissions = Map.copyOf(builder.declarablePermissions);
    }

    public Permissions buildSharedPermissions() {
        return sharedPermissions;
    }

    public Permissions buildPluginPermissions(LoadedPlugin plugin) {

        final var perms = declarablePermissions.entrySet().stream()
                .filter(entry -> !entry.getValue().mustBeDeclared())
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue().buildPermission(plugin.getDescriptor(), null),
                        (a, b) -> b, HashMap::new));

        for (final var str : plugin.getDescriptor().getPermissions()) {
            var i = str.indexOf(':');
            var perm = i < 0 ? str : str.substring(0, i);
            var value = i < 0 ? null : str.substring(i + 1);
            var entry = declarablePermissions.get(perm);
            if (entry != null) {
                perms.put(perm, entry.buildPermission(plugin.getDescriptor(), value));
            }
        }

        if (perms.isEmpty()) {
            return ClockworkSecurity.EMPTY_PERMISSIONS;
        } else {
            final var ret = new Permissions();
            perms.values().forEach(s -> s.forEach(ret::add));
            return ret;
        }

    }

    // TODO add way to read instance from config

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Permissions sharedPermissions = new Permissions();
        private final Map<String, PermissionsFactory> unconditionalPermissions = new HashMap<>();
        private final Map<String, PermissionsFactory> declarablePermissions = new HashMap<>();

        private Builder() {}

        public SecurityConfig build() {
            return new SecurityConfig(this);
        }

        public Builder addSharedPermission(Permission permission) {
            sharedPermissions.add(permission);
            return this;
        }

        public Builder addDeclarablePermission(String name, PermissionsFactory factory) {
            declarablePermissions.put(name, factory);
            return this;
        }

        public Builder addDeclarablePermission(String name, Set<Permission> permissions) {
            final var perms = Set.copyOf(permissions);
            return addDeclarablePermission(name, (d, p) -> perms);
        }

        public Builder addDeclarablePermission(String name, Permission permission) {
            return addDeclarablePermission(name, Set.of(permission));
        }

    }

}
