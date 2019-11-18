package dev.m00nl1ght.clockwork.core;

import com.vdurmont.semver4j.Semver;
import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.List;

public class PluginContainer {

    private final String id;
    private final Semver version;
    private final String displayName;
    private final String description;
    private final List<String> authors;
    private final List<String> permissions;
    private final Module mainModule;
    private final ClockworkCore core;

    PluginContainer(PluginDefinition definition, Module mainModule, ClockworkCore core) {
        Preconditions.notNull(definition, "definition");
        this.id = definition.getId();
        this.version = definition.getVersion();
        this.displayName = definition.getDisplayName();
        this.description = definition.getDescription();
        this.authors = definition.getAuthors();
        this.permissions = definition.getPermissions();
        this.mainModule = Preconditions.notNull(mainModule, "mainModule");
        this.core = Preconditions.notNull(core, "core");
    }

    public String getId() {
        return id;
    }

    public Semver getVersion() {
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

    public Module getMainModule() {
        return mainModule;
    }

    public ClockworkCore getClockworkCore() {
        return core;
    }

    public List<String> getPermissions() {
        return permissions;
    }

}
