package dev.m00nl1ght.clockwork.descriptor;

import dev.m00nl1ght.clockwork.locator.PluginLocator;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.version.Version;

import java.lang.module.ModuleFinder;

public final class PluginReference {

    private final PluginDescriptor descriptor;
    private final PluginLocator locator;
    private final String mainModule;
    private final ModuleFinder moduleFinder;

    PluginReference(Builder builder) {
        this.descriptor = Arguments.notNull(builder.descriptor, "descriptor");
        this.locator = Arguments.notNull(builder.locator, "locator");
        this.mainModule = Arguments.notNullOrBlank(builder.mainModule, "mainModule");
        this.moduleFinder = builder.moduleFinder;
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

    public PluginLocator getLocator() {
        return locator;
    }

    /**
     * Returns the name of the java module containing this plugin.
     */
    public String getMainModule() {
        return mainModule;
    }

    /**
     * Returns the ModuleFinder pointing to the java module(s) containing this plugin,
     * or {@code null} for plugins that are loaded from the boot layer.
     */
    public ModuleFinder getModuleFinder() {
        return moduleFinder;
    }

    @Override
    public String toString() {
        return descriptor.toString();
    }

    public static Builder builder(PluginDescriptor descriptor) {
        return new Builder(descriptor);
    }

    public static final class Builder {

        private final PluginDescriptor descriptor;
        private PluginLocator locator;
        private String mainModule;
        private ModuleFinder moduleFinder;

        private Builder(PluginDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        public PluginReference build() {
            return new PluginReference(this);
        }

        public Builder locator(PluginLocator locator) {
            this.locator = locator;
            return this;
        }

        public Builder mainModule(String mainModule) {
            this.mainModule = mainModule;
            return this;
        }

        public Builder moduleFinder(ModuleFinder moduleFinder) {
            this.moduleFinder = moduleFinder;
            return this;
        }

    }

}
