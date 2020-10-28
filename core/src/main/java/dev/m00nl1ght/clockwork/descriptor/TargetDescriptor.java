package dev.m00nl1ght.clockwork.descriptor;

import dev.m00nl1ght.clockwork.core.PluginLoadingException;
import dev.m00nl1ght.clockwork.config.Config;
import dev.m00nl1ght.clockwork.version.Version;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class TargetDescriptor {

    private final String pluginId;
    private final String targetId;
    private final Version version;
    private final String parent;
    private final String targetClass;
    private final List<String> internalComponents;
    private final Config extData;

    TargetDescriptor(Builder builder) {
        this.pluginId = builder.pluginId;
        this.targetId = builder.targetId;
        this.version = Objects.requireNonNull(builder.version);
        this.parent = builder.parentId;
        this.targetClass = Objects.requireNonNull(builder.targetClass);
        this.internalComponents = List.copyOf(builder.internalComponents);
        this.extData = builder.extData;
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getId() {
        return pluginId + ":" + targetId;
    }

    public Version getVersion() {
        return version;
    }

    public String getParent() {
        return parent;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public List<String> getInternalComponents() {
        return internalComponents;
    }

    public Config getExtData() {
        return extData;
    }

    @Override
    public String toString() {
        return getId();
    }

    public static Builder builder(String pluginId, String targetId) {
        Objects.requireNonNull(pluginId);
        Objects.requireNonNull(targetId);
        final var resultingId = pluginId + ":" + targetId;
        if (!DependencyDescriptor.COMPONENT_ID_PATTERN.matcher(resultingId).matches())
            throw PluginLoadingException.invalidId(resultingId);
        return new Builder(pluginId, targetId);
    }

    public static final class Builder {

        private final String pluginId;
        private final String targetId;
        private Version version;
        private String targetClass;
        private String parentId;
        private final Set<String> internalComponents = new LinkedHashSet<>();
        private Config extData = Config.EMPTY;

        private Builder(String pluginId, String targetId) {
            this.pluginId = pluginId;
            this.targetId = targetId;
        }

        public TargetDescriptor build() {
            return new TargetDescriptor(this);
        }

        public Builder version(Version version) {
            this.version = version;
            return this;
        }

        public Builder targetClass(String targetClass) {
            this.targetClass = targetClass;
            return this;
        }

        public Builder parent(String parentId) {
            this.parentId = parentId == null ? null : parentId.contains(":") ? parentId : pluginId + ":" + parentId;
            return this;
        }

        public void internalComponent(String componentClass) {
            if (componentClass == null) return;
            internalComponents.add(componentClass);
        }

        public void extData(Config extData) {
            this.extData = Objects.requireNonNull(extData);
        }

    }

}
