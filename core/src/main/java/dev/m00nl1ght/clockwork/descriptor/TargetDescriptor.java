package dev.m00nl1ght.clockwork.descriptor;

import dev.m00nl1ght.clockwork.core.PluginLoadingException;
import dev.m00nl1ght.clockwork.util.Preconditions;
import dev.m00nl1ght.clockwork.version.Version;

public final class TargetDescriptor {

    private final PluginDescriptor plugin;
    private final String id;
    private final String parent;
    private final String targetClass;

    TargetDescriptor(Builder builder) {
        this.plugin = Preconditions.notNull(builder.plugin, "plugin");
        this.id = Preconditions.notNullOrBlank(builder.id, "id");
        this.parent = builder.parentId;
        this.targetClass = Preconditions.notNullOrBlank(builder.targetClass, "targetClass");
    }

    public PluginDescriptor getPlugin() {
        return plugin;
    }

    public String getId() {
        return id;
    }

    public Version getVersion() {
        return plugin.getVersion();
    }

    public String getParent() {
        return parent;
    }

    public String getTargetClass() {
        return targetClass;
    }

    @Override
    public String toString() {
        return getId() + ":" + getVersion();
    }

    public static Builder builder(PluginDescriptor plugin) {
        return new Builder(plugin);
    }

    public static final class Builder {

        private final PluginDescriptor plugin;
        private String id;
        private String targetClass;
        private String parentId;

        private Builder(PluginDescriptor plugin) {
            this.plugin = plugin;
        }

        public TargetDescriptor build() {
            return new TargetDescriptor(this);
        }

        public Builder id(String id) {
            if (id == null) return this;
            if (!id.contains(":")) id = plugin.getId() + ":" + id;
            final var matcher = DependencyDescriptor.COMPONENT_ID_PATTERN.matcher(id);
            if (matcher.matches()) {
                if (matcher.group(1).equals(plugin.getId())) {
                    this.id = id;
                    return this;
                } else {
                    throw PluginLoadingException.subIdMismatch(plugin, id);
                }
            } else {
                throw PluginLoadingException.invalidId(id);
            }
        }

        public Builder targetClass(String targetClass) {
            this.targetClass = targetClass;
            return this;
        }

        public Builder parent(String parentId) {
            this.parentId = parentId == null ? null : parentId.contains(":") ? parentId : plugin.getId() + ":" + parentId;
            return this;
        }

    }

}
