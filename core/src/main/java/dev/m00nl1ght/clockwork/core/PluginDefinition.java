package dev.m00nl1ght.clockwork.core;

import com.vdurmont.semver4j.Semver;
import dev.m00nl1ght.clockwork.util.Preconditions;

import java.lang.module.ModuleFinder;
import java.util.*;
import java.util.stream.Stream;

public final class PluginDefinition {

    private final String displayName;
    private final String description;
    private final List<String> authors;
    private final ComponentDefinition mainComponent;
    private final List<ComponentDefinition> components = new ArrayList<>();
    private final List<ComponentTargetDefinition> targets = new ArrayList<>();
    private final ModuleFinder moduleFinder;
    private final String mainModule;

    protected PluginDefinition(String pluginId, Semver version, String mainClass, String displayName, String description, List<String> authors, Collection<DependencyDefinition> dependencies, ModuleFinder moduleFinder, String mainModule) {
        this.mainComponent = new ComponentDefinition(this, pluginId, version, mainClass, ClockworkCore.CORE_TARGET_ID, dependencies, false);
        this.displayName = Preconditions.notNullOrBlank(displayName, "displayName");
        this.description = Preconditions.notNull(description, "description");
        this.authors = List.copyOf(Preconditions.notNull(authors, "authors"));
        this.mainModule = Preconditions.notNullOrBlank(mainModule, "mainModule");
        this.moduleFinder = moduleFinder;
    }

    public String getId() {
        return mainComponent.getId();
    }

    public Semver getVersion() {
        return mainComponent.getVersion();
    }

    public List<DependencyDefinition> getDependencies() {
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

    public ComponentDefinition getMainComponent() {
        return mainComponent;
    }

    public Stream<ComponentDefinition> getComponents() {
        return components.stream();
    }

    protected void addComponent(ComponentDefinition componentDefinition) {
        Preconditions.notNullAnd(componentDefinition, o -> o.getParent() == this, "componentDefinition");
        components.add(componentDefinition);
    }

    public List<ComponentTargetDefinition> getTargetDefinitions() {
        return targets;
    }

    protected void addTargetDefinition(ComponentTargetDefinition targetDefinition) {
        Preconditions.notNullAnd(targetDefinition, o -> o.getParent() == this, "targetDefinition");
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

    protected String subId(String componentId) {
        if (mainComponent == null) return componentId;
        final var t = componentId.split(":");
        if (t.length > 2) throw new IllegalArgumentException("invalid component id: "  + componentId);
        if (t.length == 1) return getId() + ":" + componentId;
        if (t[0].equals(getId())) return componentId;
        throw new IllegalArgumentException("component id does not match parent: "  + componentId + " for plugin " + getId());
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
        protected ModuleFinder moduleFinder;
        protected String mainModule;
        protected final List<String> authors = new ArrayList<>();
        protected final Map<String, DependencyDefinition> dependencies = new HashMap<>();

        protected Builder(String pluginId) {
            this.id = pluginId;
            this.displayName = pluginId;
        }

        public PluginDefinition build() {
            if (!id.equals(ClockworkCore.CORE_PLUGIN_ID)) dependencies.computeIfAbsent(ClockworkCore.CORE_PLUGIN_ID, DependencyDefinition::buildAnyVersion);
            return new PluginDefinition(id, version, mainClass, displayName, description, authors, dependencies.values(), moduleFinder, mainModule);
        }

        public Builder version(Semver version) {
            this.version = version;
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

        public Builder dependency(DependencyDefinition dependency) {
            final var prev = this.dependencies.putIfAbsent(dependency.getComponentId(), dependency);
            if (prev != null) throw PluginLoadingException.generic("Duplicate dependency: [] Already present: []", dependency, prev);
            return this;
        }

    }

}
