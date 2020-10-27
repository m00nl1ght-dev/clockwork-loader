package dev.m00nl1ght.clockwork.extension.security.internal;

import dev.m00nl1ght.clockwork.extension.security.CWLSecurityExtension;

import java.lang.ref.WeakReference;
import java.security.*;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class PolicyImpl extends PolicySpi {

    public static final String NAME = "Clockwork";

    private static final Map<ClassLoader, WeakReference<Context>> configMap =
            Collections.synchronizedMap(new WeakHashMap<>(3));

    public static void putContext(ClassLoader classLoader, Permissions unconditionalPerms,
                                  Map<ProtectionDomain, Permissions> declaredPerms) {
        final var context = new Context(classLoader, unconditionalPerms, declaredPerms);
        configMap.put(classLoader, new WeakReference<>(context));
    }

    private final ClassLoader platformClassLoader;
    private final ClassLoader systemClassLoader;

    PolicyImpl() {
        platformClassLoader = ClassLoader.getPlatformClassLoader();
        systemClassLoader = ClassLoader.getSystemClassLoader();
    }

    @Override
    protected boolean engineImplies(ProtectionDomain domain, Permission permission) {

        final var classloader = domain.getClassLoader();

        if (classloader == platformClassLoader) {
            return true;
        }

        if (classloader == systemClassLoader) {
            return true;
        }

        if (classloader != null) {
            final var configRef = configMap.get(classloader);
            if (configRef != null) {
                final var config = configRef.get();
                if (config != null) {
                    if (config.getUnconditionalPerms().implies(permission)) {
                        return true;
                    } else {
                        return config.getPerPluginPerms(domain).implies(permission);
                    }
                }
            }
        }

        return false;

    }

    @Override
    protected PermissionCollection engineGetPermissions(ProtectionDomain domain) {

        final var classloader = domain.getClassLoader();

        if (classloader == platformClassLoader) {
            return CWLSecurityExtension.ALL_PERMISSIONS;
        }

        if (classloader == systemClassLoader) {
            return CWLSecurityExtension.ALL_PERMISSIONS;
        }

        if (classloader != null) {
            final var configRef = configMap.get(classloader);
            if (configRef != null) {
                final var config = configRef.get();
                if (config != null) {
                    final var perms = new Permissions();
                    config.getUnconditionalPerms().elementsAsStream().forEach(perms::add);
                    config.getPerPluginPerms(domain).elementsAsStream().forEach(perms::add);
                    perms.setReadOnly();
                    return perms;
                }
            }
        }

        return CWLSecurityExtension.EMPTY_PERMISSIONS;

    }

    private static final class Context {

        private final ClassLoader classLoader;
        private final Permissions unconditionalPerms;
        private final Map<ProtectionDomain, Permissions> perPluginPerms;

        Context(ClassLoader classLoader, Permissions unconditionalPerms, Map<ProtectionDomain, Permissions> perPluginPerms) {
            this.classLoader = classLoader;
            this.unconditionalPerms = unconditionalPerms;
            this.unconditionalPerms.setReadOnly();
            this.perPluginPerms = Map.copyOf(perPluginPerms);
            this.perPluginPerms.values().forEach(PermissionCollection::setReadOnly);
        }

        public Permissions getUnconditionalPerms() {
            return unconditionalPerms;
        }

        public Permissions getPerPluginPerms(ProtectionDomain domain) {
            return perPluginPerms.getOrDefault(domain, CWLSecurityExtension.EMPTY_PERMISSIONS);
        }

    }

}
