package dev.m00nl1ght.clockwork.security;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.internal.InternalLoggers;
import dev.m00nl1ght.clockwork.security.internal.PolicyImpl;
import dev.m00nl1ght.clockwork.security.internal.ProviderImpl;
import dev.m00nl1ght.clockwork.util.Arguments;

import java.security.*;
import java.util.HashMap;

public final class ClockworkSecurity {

    private ClockworkSecurity() {}

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

    public static Provider newProvider() {
        return new ProviderImpl();
    }

    public static void install() {
        try {
            Security.addProvider(new ProviderImpl());
            Policy.setPolicy(Policy.getInstance(ProviderImpl.NAME, null));
            System.setSecurityManager(new SecurityManager());
            InternalLoggers.SECURITY.info("Sucessfully installed security manager, provider and policy.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to install security provider", e);
        }
    }

    public static void registerContext(ClockworkCore core, SecurityConfig securityConfig) {

        Arguments.notNull(core, "core");
        Arguments.notNull(securityConfig, "securityConfig");

        core.getState().requireOrAfter(ClockworkCore.State.POPULATED);

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(new SecurityPermission("setPolicy"));

        final var optClassLoader = core.getModuleLayer().modules().stream()
                .findFirst().map(Module::getClassLoader);

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

        PolicyImpl.putContext(classLoader, unconditionalPerms, declaredPerms);

    }

}
