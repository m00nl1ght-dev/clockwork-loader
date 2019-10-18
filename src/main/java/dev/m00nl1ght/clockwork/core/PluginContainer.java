package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.List;

public class PluginContainer {

    private final String id;
    private final String version;
    private final String displayName;
    private final String description;
    private final List<String> authors;
    private final Module mainModule;

    protected PluginContainer(PluginDefinition definition, Module mainModule) {
        Preconditions.notNull(definition, "definition");
        this.id = definition.getId();
        this.version = definition.getVersion();
        this.displayName = definition.getDisplayName();
        this.description = definition.getDescription();
        this.authors = definition.getAuthors();
        this.mainModule = mainModule;
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

    /**
     * Returns the java module containing this plugin,
     * or {@code null} for plugins that are loaded from the boot layer.
     */
    public Module getMainModule() {
        return mainModule;
    }

}
