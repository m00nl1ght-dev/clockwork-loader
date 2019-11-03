package dev.m00nl1ght.clockwork.security;

import dev.m00nl1ght.clockwork.classloading.PluginClassloader;
import dev.m00nl1ght.clockwork.core.PluginContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FilePermission;
import java.security.*;
import java.util.PropertyPermission;

public final class ClockworkSecurityPolicy extends Policy {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final SecurityPermission GET_POLICY_PERMISSION = new SecurityPermission ("getPolicy");

    private static ClockworkSecurityPolicy INSTALLED;

    private ClockworkSecurityPolicy() {}

    public PermissionCollection getTrusted(ProtectionDomain domain) {
        final var perms = new Permissions();
        perms.add(new AllPermission());
        return perms;
    }

    public PermissionCollection getUntrusted() {
        return new Permissions();
    }

    public PermissionCollection getUntrusted(PluginContainer plugin) {
        final var perms = new Permissions();
        // for testing TODO delegate to testenv
        final var file = new File("plugin-data/" + plugin.getId() + "/"); file.mkdirs();
        perms.add(new FilePermission(file.getAbsolutePath() + "\\-", "read,write,delete"));
        perms.add(new PropertyPermission("*", "read"));
        return perms;
    }

    @Override
    public final PermissionCollection getPermissions(ProtectionDomain domain) {
        if (domain.getClassLoader() instanceof PluginClassloader) {
            return new Permissions();
        } else {
            return getTrusted(domain);
        }
    }

    @Override
    public final boolean implies(ProtectionDomain domain, Permission permission) {
        if (domain.getClassLoader() instanceof PluginClassloader) {
            return false; // fastpath for plugins
        } else {
            return super.implies(domain, permission);
        }
    }

    public static ClockworkSecurityPolicy getActivePolicy() {
        final var sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(GET_POLICY_PERMISSION);
        return INSTALLED;
    }

    public static void install() {
        if (INSTALLED != null) throw new IllegalStateException("already installed");
        final var policy = new ClockworkSecurityPolicy();
        Policy.setPolicy(policy); INSTALLED = policy;
        System.setSecurityManager(new SecurityManager());
        LOGGER.info("Sucessfully installed security manager and policy.");
    }

}
