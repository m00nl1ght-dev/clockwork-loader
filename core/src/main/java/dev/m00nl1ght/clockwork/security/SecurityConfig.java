package dev.m00nl1ght.clockwork.security;

import dev.m00nl1ght.clockwork.core.LoadedPlugin;
import dev.m00nl1ght.clockwork.util.config.Config;

import java.security.Permission;
import java.security.Permissions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class SecurityConfig {

    private final Permissions unconditionalPermissions;
    private final Map<String, PermissionsFactory> declarablePermissions;

    private SecurityConfig(Builder builder) {
        this.unconditionalPermissions = builder.unconditionalPermissions;
        this.unconditionalPermissions.setReadOnly();
        this.declarablePermissions = Map.copyOf(builder.declarablePermissions);
    }

    private SecurityConfig(Config data) { // TODO
        this.unconditionalPermissions = null;
        this.declarablePermissions = null;
    }

    public Permissions buildUnconditionalPermissions() {
        return unconditionalPermissions;
    }

    public Permissions buildDeclaredPermissions(LoadedPlugin plugin) {
        final var perms = new HashSet<Permission>();

        for (var str : plugin.getDescriptor().getPermissions()) {
            var i = str.indexOf(':');
            var perm = i < 0 ? str : str.substring(0, i);
            var value = i < 0 ? null : str.substring(i + 1);
            var entry = declarablePermissions.get(perm);
            if (entry != null) {
                perms.addAll(entry.buildPermission(value));
            }
        }

        if (perms.isEmpty()) {
            return ClockworkSecurity.EMPTY_PERMISSIONS;
        } else {
            final var ret = new Permissions();
            perms.forEach(ret::add);
            return ret;
        }
    }

    public static SecurityConfig from(Config data) {
        return new SecurityConfig(data);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Permissions unconditionalPermissions = new Permissions();
        private final Map<String, PermissionsFactory> declarablePermissions = new HashMap<>();

        private Builder() {}

        public SecurityConfig build() {
            return new SecurityConfig(this);
        }

        public Builder addUnconditionalPermission(Permission permission) {
            unconditionalPermissions.add(permission);
            return this;
        }

        public Builder addDeclarablePermission(String name, PermissionsFactory factory) {
            declarablePermissions.put(name, factory);
            return this;
        }

        public Builder addDeclarablePermission(String name, Set<Permission> permissions) {
            final var perms = Set.copyOf(permissions);
            return addDeclarablePermission(name, p -> perms);
        }

        public Builder addDeclarablePermission(String name, Permission permission) {
            return addDeclarablePermission(name, Set.of(permission));
        }

    }

}
