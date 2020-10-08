package dev.m00nl1ght.clockwork.descriptor;

import dev.m00nl1ght.clockwork.fnder.PluginFinder;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.version.Version;

public final class PluginReference {

    private final PluginDescriptor descriptor;
    private final PluginFinder finder;
    private final String mainModule;

    public static PluginReference of(PluginDescriptor descriptor, PluginFinder finder, String mainModule) {
        return new PluginReference(descriptor, finder, mainModule);
    }

    private PluginReference(PluginDescriptor descriptor, PluginFinder finder, String mainModule) {
        this.descriptor = Arguments.notNull(descriptor, "descriptor");
        this.finder = Arguments.notNull(finder, "finder");
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

    public PluginFinder getFinder() {
        return finder;
    }

    public String getMainModule() {
        return mainModule;
    }

    @Override
    public String toString() {
        return descriptor.toString();
    }

}
