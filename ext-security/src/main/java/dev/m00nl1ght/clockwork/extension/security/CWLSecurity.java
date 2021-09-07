package dev.m00nl1ght.clockwork.extension.security;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.MainComponent;
import dev.m00nl1ght.clockwork.extension.security.internal.PolicyImpl;
import dev.m00nl1ght.clockwork.extension.security.internal.ProviderImpl;
import dev.m00nl1ght.clockwork.utils.logger.Logger;

import java.security.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

public final class CWLSecurity extends MainComponent {

    static final Logger LOGGER = Logger.create("Clockwork-Ext-Security");

    private CWLSecurity(ClockworkCore core) {
        super(core);
    }

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
        install(false);
    }

    public static void install(boolean trustUnknownLoaders) {
        try {
            Security.addProvider(new ProviderImpl());
            Policy.setPolicy(Policy.getInstance(ProviderImpl.NAME, null));
            PolicyImpl.setTrustUnknownLoaders(trustUnknownLoaders);
            System.setSecurityManager(new SecurityManager());
            LOGGER.info("Sucessfully installed security manager, provider and policy.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to install security provider", e);
        }
    }

    public static void registerContext(ClockworkCore core, SecurityConfig securityConfig) {

        Objects.requireNonNull(core);
        Objects.requireNonNull(securityConfig);

        core.getPhase().requireOrAfter(ClockworkCore.Phase.POPULATED);

        final var classLoaders = core.getModuleLayers().stream()
                .flatMap(m -> m.modules().stream())
                .map(Module::getClassLoader)
                .collect(Collectors.toUnmodifiableSet());

        if (classLoaders.isEmpty()) return;

        final var unconditionalPerms = securityConfig.buildSharedPermissions();
        final var declaredPerms = new HashMap<ProtectionDomain, Permissions>();

        for (final var plugin : core.getLoadedPlugins()) {
            final var mainClass = plugin.getMainComponent().getComponentClass();
            final var permissions = securityConfig.buildPluginPermissions(plugin);
            if (permissions != EMPTY_PERMISSIONS) {
                declaredPerms.put(mainClass.getProtectionDomain(), permissions);
            }
        }

        for (final var classLoader : classLoaders) {
            PolicyImpl.putContext(classLoader, unconditionalPerms, declaredPerms);
        }

    }

}
