package dev.m00nl1ght.clockwork.descriptor;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.locator.PluginLocator;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.version.Version;

import java.lang.module.ModuleFinder;
import java.util.LinkedList;
import java.util.List;

public final class PluginReference {

    private final PluginDescriptor descriptor;
    private final ComponentDescriptor mainComponent;
    private final List<ComponentDescriptor> components;
    private final List<TargetDescriptor> targets;
    private final List<String> processors;
    private final PluginLocator locator;
    private final String mainModule;
    private final ModuleFinder moduleFinder;

    PluginReference(Builder builder) {
        this.descriptor = Arguments.notNull(builder.descriptor, "descriptor");
        this.mainComponent = Arguments.notNull(builder.mainComponent, "mainComponent");
        this.components = List.copyOf(Arguments.notNull(builder.components, "components"));
        this.targets = List.copyOf(Arguments.notNull(builder.targets, "targets"));
        this.processors = List.copyOf(Arguments.notNull(builder.processors, "processors"));
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

    public ComponentDescriptor getMainComponent() {
        return mainComponent;
    }

    public List<ComponentDescriptor> getComponentDescriptors() {
        return components;
    }

    public List<TargetDescriptor> getTargetDescriptors() {
        return targets;
    }

    public List<String> getProcessors() {
        return processors;
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
        private ComponentDescriptor mainComponent;
        private final LinkedList<ComponentDescriptor> components = new LinkedList<>();
        private final LinkedList<TargetDescriptor> targets = new LinkedList<>();
        private final LinkedList<String> processors = new LinkedList<>();
        private PluginLocator locator;
        private String mainModule;
        private ModuleFinder moduleFinder;

        private Builder(PluginDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        public PluginReference build() {
            if (mainComponent != null && !components.contains(mainComponent)) components.addFirst(mainComponent);
            return new PluginReference(this);
        }

        public Builder mainComponent(ComponentDescriptor mainComponent) {
            this.mainComponent = mainComponent;
            if (mainComponent == null) return this;
            if (!mainComponent.getId().equals(descriptor.getId()))
                throw new IllegalArgumentException("mainComponent id is different than plugin id");
            if (!mainComponent.getTargetId().equals(ClockworkCore.CORE_TARGET_ID))
                throw new IllegalArgumentException("mainComponent target must be [" + ClockworkCore.CORE_TARGET_ID + "]");
            if (mainComponent.isOptional())
                throw new IllegalArgumentException("mainComponent can not be optional");
            return this;
        }

        public Builder component(ComponentDescriptor component) {
            if (component == null) return this;
            if (component.getPlugin() != descriptor)
                throw new IllegalArgumentException("component is from different plugin");
            if (!components.contains(component)) this.components.add(component);
            return this;
        }

        public Builder target(TargetDescriptor target) {
            if (target == null) return this;
            if (target.getPlugin() != descriptor)
                throw new IllegalArgumentException("target is from different plugin");
            if (!targets.contains(target)) this.targets.add(target);
            return this;
        }

        public Builder markForProcessor(String processor) {
            if (processor == null || processor.isBlank()) return this;
            if (!processors.contains(processor)) this.processors.add(processor);
            return this;
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
