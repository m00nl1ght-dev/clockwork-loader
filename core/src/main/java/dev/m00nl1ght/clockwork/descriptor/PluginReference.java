package dev.m00nl1ght.clockwork.descriptor;

import dev.m00nl1ght.clockwork.version.Version;

import java.lang.module.ModuleFinder;
import java.util.Objects;

public final class PluginReference {

    private final PluginDescriptor descriptor;
    private final ModuleFinder moduleFinder;
    private final String moduleName;

    public static PluginReference of(PluginDescriptor descriptor, ModuleFinder moduleFinder, String moduleName) {
        Objects.requireNonNull(descriptor);
        Objects.requireNonNull(moduleFinder);
        Objects.requireNonNull(moduleName);
        return new PluginReference(descriptor, moduleFinder, moduleName);
    }

    public static PluginReference of(PluginReference other, ModuleFinder additionalModules) {
        Objects.requireNonNull(other);
        Objects.requireNonNull(additionalModules);
        final var newFinder = ModuleFinder.compose(other.moduleFinder, additionalModules);
        return new PluginReference(other.descriptor, newFinder, other.moduleName);
    }

    private PluginReference(PluginDescriptor descriptor, ModuleFinder moduleFinder, String moduleName) {
        this.descriptor = descriptor;
        this.moduleFinder = moduleFinder;
        this.moduleName = moduleName;
    }

    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    public String getId() {
        return descriptor.getId();
    }

    public Version getVersion() {
        return descriptor.getVersion();
    }

    public ModuleFinder getModuleFinder() {
        return moduleFinder;
    }

    public String getModuleName() {
        return moduleName;
    }

    @Override
    public String toString() {
        return descriptor.toString();
    }

}
