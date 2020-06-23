package dev.m00nl1ght.clockwork.core;

import com.vdurmont.semver4j.Semver;
import dev.m00nl1ght.clockwork.locator.PluginLocator;
import dev.m00nl1ght.clockwork.util.Preconditions;

import java.lang.module.ModuleFinder;
import java.util.*;

public final class PluginDefinition {

    private final String displayName;
    private final String description;
    private final List<String> authors;
    private final ComponentDefinition mainComponent;
    private final List<ComponentDefinition> components = new ArrayList<>();
    private final List<TargetDefinition> targets = new ArrayList<>();
    private final PluginLocator locator;
    private final ModuleFinder moduleFinder;
    private final String mainModule;
    private final List<String> processors;
    private final List<String> permissions;

    protected PluginDefinition(String pluginId, Semver version, String mainClass, String displayName, String description, List<String> authors, Collection<ComponentDescriptor> dependencies, PluginLocator locator, ModuleFinder moduleFinder, String mainModule, List<String> processors, List<String> permissions) {
        this.mainComponent = new ComponentDefinition(this, pluginId, version, mainClass, ClockworkCore.CORE_TARGET_ID, dependencies, false, processors);
        this.displayName = Preconditions.notNullOrBlank(displayName, "displayName");
        this.description = Preconditions.notNull(description, "description");
        this.authors = List.copyOf(Preconditions.notNull(authors, "authors"));
        this.processors = List.copyOf(Preconditions.notNull(processors, "processors"));
        this.permissions = List.copyOf(Preconditions.notNull(permissions, "permissions"));
        this.mainModule = Preconditions.notNullOrBlank(mainModule, "mainModule");
        this.locator = Preconditions.notNull(locator, "locator");
        this.moduleFinder = moduleFinder;
    }

    public String getId() {
        return mainComponent.getId();
    }

    public Semver getVersion() {
        return mainComponent.getVersion();
    }

    public List<ComponentDescriptor> getDependencies() {
        return mainComponent.getDependencies();
    }

    /**
     * Returns the ModuleFinder pointing to the java module(s) containing this plugin,
     * or {@code null} for plugins that are loaded from the boot layer.
     */
    public ModuleFinder getModuleFinder() {
        return moduleFinder;
    }

    /**
     * Returns the name of the java module containing this plugin.
     */
    public String getMainModule() {
        return mainModule;
    }

    public PluginLocator getLocator() {
        return locator;
    }

    public ComponentDefinition getMainComponent() {
        return mainComponent;
    }

    public List<ComponentDefinition> getComponentDefinitions() {
        return Collections.unmodifiableList(components);
    }

    protected void addComponentDefinition(ComponentDefinition componentDefinition) {
        Preconditions.notNullAnd(componentDefinition, o -> o.getParent() == this, "componentDefinition");
        components.add(componentDefinition);
    }

    public List<TargetDefinition> getTargetDefinitions() {
        return Collections.unmodifiableList(targets);
    }

    protected void addTargetDefinition(TargetDefinition targetDefinition) {
        Preconditions.notNullAnd(targetDefinition, o -> o.getPlugin() == this, "targetDefinition");
        targets.add(targetDefinition);
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public List<String> getProcessors() {
        return processors;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    protected String subId(String componentId) {
        if (mainComponent == null) {
            if (!ComponentDescriptor.PLUGIN_ID_PATTERN.matcher(componentId).matches())
                throw PluginLoadingException.invalidId(this, componentId);
            return componentId;
        } else if (!componentId.contains(":")) {
            componentId = getId() + ":" + componentId;
        }

        final var matcher = ComponentDescriptor.COMPONENT_ID_PATTERN.matcher(componentId);
        if (matcher.matches()) {
            if (matcher.group(1).equals(getId())) {
                return componentId;
            } else {
                throw PluginLoadingException.subIdMismatch(this, componentId);
            }
        } else {
            throw PluginLoadingException.invalidId(this, componentId);
        }
    }

    @Override
    public String toString() {
        return getId() + ":" + getVersion();
    }

    public static Builder builder(String pluginId) {
        return new Builder(pluginId);
    }

    public static class Builder {

        protected final String id;
        protected Semver version;
        protected String mainClass;
        protected String displayName;
        protected String description = "";
        protected PluginLocator locator;
        protected ModuleFinder moduleFinder;
        protected String mainModule;
        protected final List<String> authors = new ArrayList<>(3);
        protected final List<String> processors = new ArrayList<>(3);
        protected final List<String> permissions = new ArrayList<>(3);
        protected final Map<String, ComponentDescriptor> dependencies = new HashMap<>();

        protected Builder(String pluginId) {
            this.id = pluginId;
            this.displayName = pluginId;
        }

        public PluginDefinition build() {
            if (!id.equals(ClockworkCore.CORE_PLUGIN_ID)) dependencies.computeIfAbsent(ClockworkCore.CORE_PLUGIN_ID, ComponentDescriptor::buildAnyVersion);
            return new PluginDefinition(id, version, mainClass, displayName, description, authors, dependencies.values(), locator, moduleFinder, mainModule, processors, permissions);
        }

        public Builder version(Semver version) {
            this.version = version;
            return this;
        }

        public Builder locator(PluginLocator locator) {
            this.locator = locator;
            return this;
        }

        public Builder mainClass(String mainClass) {
            this.mainClass = mainClass;
            return this;
        }

        public Builder moduleFinder(ModuleFinder moduleFinder, String mainModule) {
            this.moduleFinder = moduleFinder;
            this.mainModule = mainModule;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder author(String author) {
            this.authors.add(author);
            return this;
        }

        public Builder authors(Collection<String> authors) {
            this.authors.addAll(authors);
            return this;
        }

        public Builder dependency(ComponentDescriptor dependency) {
            final var prev = this.dependencies.putIfAbsent(dependency.getTarget(), dependency);
            if (prev != null) throw PluginLoadingException.dependencyDuplicate(id, dependency, prev);
            return this;
        }

        public Builder markForProcessor(String processor) {
            this.processors.add(processor);
            return this;
        }

        public Builder permission(String perm) {
            this.permissions.add(perm);
            return this;
        }

    }

}
