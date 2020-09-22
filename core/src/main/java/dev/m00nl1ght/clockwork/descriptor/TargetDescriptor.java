package dev.m00nl1ght.clockwork.descriptor;

import dev.m00nl1ght.clockwork.core.PluginLoadingException;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.version.Version;

public final class TargetDescriptor {

    private final String pluginId;
    private final String targetId;
    private final Version version;
    private final String parent;
    private final String targetClass;

    TargetDescriptor(Builder builder) {
        this.pluginId = Arguments.notNullOrBlank(builder.pluginId, "pluginId");
        this.targetId = Arguments.notNullOrBlank(builder.targetId, "targetId");
        this.version = Arguments.notNull(builder.version, "version");
        this.parent = builder.parentId;
        this.targetClass = Arguments.notNullOrBlank(builder.targetClass, "targetClass");
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

    @Override
    public String toString() {
        return getId();
    }

    public static Builder builder(String pluginId, String targetId) {
        Arguments.notNullOrBlank(pluginId, "pluginId");
        Arguments.notNullOrBlank(targetId, "targetId");
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

    }

}
