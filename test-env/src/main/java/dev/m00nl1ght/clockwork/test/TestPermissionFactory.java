package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.descriptor.PluginDescriptor;
import dev.m00nl1ght.clockwork.extension.security.permissions.PermissionsFactory;

import java.io.File;
import java.io.FilePermission;
import java.security.Permission;
import java.util.Set;

public class TestPermissionFactory implements PermissionsFactory {

    @Override
    public Set<Permission> buildPermission(PluginDescriptor plugin, String params) {
        final var dir = new File(TestLauncher.PLUGIN_DATA_DIR, plugin.getId());
        return Set.of(new FilePermission(dir.getAbsolutePath(), "read,write,delete"),
                new FilePermission(dir.getAbsolutePath() + File.separator + "-", "read,write,delete"));
    }

    @Override
    public boolean mustBeDeclared() {
        return false;
    }

}
