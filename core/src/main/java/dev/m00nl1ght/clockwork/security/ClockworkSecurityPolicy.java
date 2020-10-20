package dev.m00nl1ght.clockwork.security;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.util.Arguments;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;
import java.security.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class ClockworkSecurityPolicy extends Policy {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final Permissions ALL_PERMISSIONS = allPermissions();
    private static Permissions allPermissions() {
        final var permissions = new Permissions();
        permissions.add(new AllPermission());
        permissions.setReadOnly();
        return permissions;
    }

    public static final Permissions EMPTY_PERMISSIONS = emptyPermissions();
    private static Permissions emptyPermissions() {
        final var permissions = new Permissions();
        permissions.setReadOnly();
        return permissions;
    }

    private final ClassLoader platformClassLoader;
    private final ClassLoader systemClassLoader;

    public static void install() {
        Policy.setPolicy(new ClockworkSecurityPolicy());
        System.setSecurityManager(new SecurityManager());
        LOGGER.info("Sucessfully installed security manager and policy.");
    }

    public static ClockworkSecurityPolicy getPolicy() {
        final var policy = Policy.getPolicy();
        return policy instanceof ClockworkSecurityPolicy ? (ClockworkSecurityPolicy) policy : null;
    }

    private final Map<ClassLoader, WeakReference<Context>> configMap =
            Collections.synchronizedMap(new WeakHashMap<>());

    private ClockworkSecurityPolicy() {
        platformClassLoader = ClassLoader.getPlatformClassLoader();
        systemClassLoader = ClassLoader.getSystemClassLoader();
    }

    public final void registerContext(ClockworkCore core, SecurityConfig securityConfig) {

        Arguments.notNull(core, "core");
        Arguments.notNull(securityConfig, "securityConfig");

        core.getState().requireOrAfter(ClockworkCore.State.POPULATED);

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(new SecurityPermission("setPolicy"));

        final var optClassLoader = core.getModuleLayer().modules().stream().findFirst().map(Module::getClassLoader);
        if (optClassLoader.isEmpty()) return;

        final var classLoader = optClassLoader.get();
        final var unconditionalPerms = securityConfig.buildUnconditionalPermissions();
        final var declaredPerms = new HashMap<ProtectionDomain, Permissions>();

        for (final var plugin : core.getLoadedPlugins()) {
            final var mainClass = plugin.getMainComponent().getComponentClass();
            if (mainClass.getClassLoader() != classLoader) continue;
            final var permissions = securityConfig.buildDeclaredPermissions(plugin);
            if (permissions != EMPTY_PERMISSIONS) {
                declaredPerms.put(mainClass.getProtectionDomain(), permissions);
            }
        }

        final var context = new Context(classLoader, unconditionalPerms, declaredPerms);
        configMap.put(classLoader, new WeakReference<>(context));

    }

    @Override
    public final PermissionCollection getPermissions(ProtectionDomain domain) {

        final var classloader = domain.getClassLoader();

        if (classloader == platformClassLoader) {
            return ALL_PERMISSIONS;
        }

        if (classloader == systemClassLoader) {
            return ALL_PERMISSIONS;
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

        return EMPTY_PERMISSIONS;

    }

    @Override
    public boolean implies(ProtectionDomain domain, Permission permission) {

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
            return perPluginPerms.getOrDefault(domain, EMPTY_PERMISSIONS);
        }

    }

}
