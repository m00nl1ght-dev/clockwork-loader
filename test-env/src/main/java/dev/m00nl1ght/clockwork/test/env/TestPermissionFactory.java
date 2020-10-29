package dev.m00nl1ght.clockwork.test.env;

import dev.m00nl1ght.clockwork.descriptor.PluginDescriptor;
import dev.m00nl1ght.clockwork.extension.security.permissions.PermissionsFactory;

import java.io.File;
import java.io.FilePermission;
import java.security.Permission;
import java.util.Set;

public class TestPermissionFactory implements PermissionsFactory {

    public static final File TEST_DIR = new File(TestEnvironment.ENV_DIR, "perm-test");

    @Override
    public Set<Permission> buildPermission(PluginDescriptor plugin, String params) {
        final var dir = new File(TEST_DIR, plugin.getId());
        return Set.of(new FilePermission(dir.getAbsolutePath(), "read,write,delete"),
                new FilePermission(dir.getAbsolutePath() + File.separator + "-", "read,write,delete"));
    }

    @Override
    public boolean mustBeDeclared() {
        return false;
    }

}
