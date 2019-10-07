package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.List;

public class PluginContainer<C> {

    private final String id;
    private final String version;
    private final String displayName;
    private final String description;
    private final List<String> authors;
    private final Module module;

    protected PluginContainer(PluginDefinition definition, Module module) {
        Preconditions.notNull(definition, "definition");
        this.id = definition.getId();
        this.version = definition.getVersion();
        this.displayName = definition.getDisplayName();
        this.description = definition.getDescription();
        this.authors = definition.getAuthors();
        this.module = Preconditions.notNull(module, "module");
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
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

    public Module getModule() {
        return module;
    }

}
