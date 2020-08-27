package dev.m00nl1ght.clockwork.descriptor;

import dev.m00nl1ght.clockwork.core.PluginLoadingException;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.version.Version;

import java.util.LinkedList;
import java.util.List;

public final class PluginDescriptor {

    private final String id;
    private final Version version;
    private final String displayName;
    private final String description;
    private final List<String> authors;
    private final List<String> permissions;

    PluginDescriptor(Builder builder) {
        this.id = Arguments.notNullOrBlank(builder.id, "id");
        this.version = Arguments.notNull(builder.version, "version");
        this.displayName = Arguments.notNullOrBlank(builder.displayName, "displayName");
        this.description = Arguments.notNull(builder.description, "description");
        this.authors = List.copyOf(Arguments.notNull(builder.authors, "authors"));
        this.permissions = List.copyOf(Arguments.notNull(builder.permissions, "permissions"));
    }

    public String getId() {
        return id;
    }

    public Version getVersion() {
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

    public List<String> getPermissions() {
        return permissions;
    }

    @Override
    public String toString() {
        return getId() + ":" + getVersion();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String id;
        private Version version;
        private String displayName;
        private String description = "";
        private final LinkedList<String> authors = new LinkedList<>();
        private final LinkedList<String> permissions = new LinkedList<>();

        private Builder() {}

        public PluginDescriptor build() {
            return new PluginDescriptor(this);
        }

        public Builder id(String id) {
            if (id == null) return this;
            if (!DependencyDescriptor.PLUGIN_ID_PATTERN.matcher(id).matches())
                throw PluginLoadingException.invalidId(id);
            this.id = id;
            return this;
        }

        public Builder version(Version version) {
            this.version = version;
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
            if (author == null || author.isBlank()) return this;
            if (!authors.contains(author)) this.authors.add(author);
            return this;
        }

        public Builder permission(String permission) {
            if (permission == null || permission.isBlank()) return this;
            if (!permissions.contains(permission)) this.permissions.add(permission);
            return this;
        }

    }

}
