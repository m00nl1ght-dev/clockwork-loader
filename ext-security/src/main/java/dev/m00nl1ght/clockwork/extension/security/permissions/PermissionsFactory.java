package dev.m00nl1ght.clockwork.extension.security.permissions;

import dev.m00nl1ght.clockwork.descriptor.PluginDescriptor;

import java.security.Permission;
import java.util.Set;

public interface PermissionsFactory {

    Set<Permission> buildPermission(PluginDescriptor plugin, String params);

    default boolean mustBeDeclared() {
        return true;
    }

    class Fixed implements PermissionsFactory {

        private final Set<Permission> permissions;
        private final boolean mustBeDeclared;

        public Fixed(Set<Permission> permissions) {
            this(permissions, false);
        }

        public Fixed(Set<Permission> permissions, boolean mustBeDeclared) {
            this.permissions = Set.copyOf(permissions);
            this.mustBeDeclared = mustBeDeclared;
        }

        @Override
        public Set<Permission> buildPermission(PluginDescriptor plugin, String params) {
            return permissions;
        }

        @Override
        public boolean mustBeDeclared() {
            return mustBeDeclared;
        }

    }

}
