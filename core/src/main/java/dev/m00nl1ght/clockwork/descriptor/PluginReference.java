package dev.m00nl1ght.clockwork.descriptor;

import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.version.Version;

import java.lang.module.ModuleReference;

public final class PluginReference {

    private final PluginDescriptor descriptor;
    private final ModuleReference mainModule;

    public static PluginReference of(PluginDescriptor descriptor, ModuleReference mainModule) {
        return new PluginReference(descriptor, mainModule);
    }

    private PluginReference(PluginDescriptor descriptor, ModuleReference mainModule) {
        this.descriptor = Arguments.notNull(descriptor, "descriptor");
        this.mainModule = Arguments.notNull(mainModule, "mainModule");
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

    public ModuleReference getMainModule() {
        return mainModule;
    }

    @Override
    public String toString() {
        return descriptor.toString();
    }

}
