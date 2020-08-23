package dev.m00nl1ght.clockwork.security;

import dev.m00nl1ght.clockwork.classloading.PluginClassloader;
import dev.m00nl1ght.clockwork.core.LoadedPlugin;
import dev.m00nl1ght.clockwork.util.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.*;

public final class ClockworkSecurityPolicy extends Policy {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final SecurityPermission GET_POLICY_PERMISSION = new SecurityPermission("getPolicy");

    private static ClockworkSecurityPolicy INSTALLED;

    private final SecurityConfiguration config;

    private ClockworkSecurityPolicy(SecurityConfiguration config) {
        this.config = config;
    }

    public PermissionCollection getTrusted(ProtectionDomain domain) {
        final var perms = new Permissions();
        perms.add(new AllPermission());
        return perms;
    }

    public PermissionCollection getUntrusted() {
        return new Permissions();
    }

    public PermissionCollection getUntrusted(LoadedPlugin plugin) {
        return config.getPermissionsFor(plugin);
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

    public static void install(SecurityConfiguration config) {
        Preconditions.notNull(config, "config");
        final var policy = new ClockworkSecurityPolicy(config);
        Policy.setPolicy(policy); INSTALLED = policy;
        System.setSecurityManager(new SecurityManager());
        LOGGER.info("Sucessfully installed security manager and policy.");
    }

}
