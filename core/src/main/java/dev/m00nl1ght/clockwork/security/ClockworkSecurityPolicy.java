package dev.m00nl1ght.clockwork.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.WeakHashMap;

public final class ClockworkSecurityPolicy extends Policy {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void install() {
        Policy.setPolicy(new ClockworkSecurityPolicy());
        System.setSecurityManager(new SecurityManager());
        LOGGER.info("Sucessfully installed security manager and policy.");
    }

    private final Map<ClassLoader, WeakReference<SecurityConfiguration>> configMap = new WeakHashMap<>();

    private ClockworkSecurityPolicy() {}

    @Override
    public final PermissionCollection getPermissions(ProtectionDomain domain) {

        final var classloader = domain.getClassLoader();

        if (classloader != null) {
            final var configRef = configMap.get(classloader);
            if (configRef != null) {
                final var config = configRef.get();
                if (config != null) {
                    return config.getPermissions(domain);
                }
            }
        }

        return new Permissions();

    }

}
