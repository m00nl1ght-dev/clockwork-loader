package dev.m00nl1ght.clockwork.descriptor;

import dev.m00nl1ght.clockwork.config.Config;
import dev.m00nl1ght.clockwork.version.Version;

import java.util.Objects;

public final class TargetDescriptor {

    private final String pluginId;
    private final String targetId;
    private final Version version;
    private final String parent;
    private final String targetClass;
    private final Config extData;

    TargetDescriptor(Builder builder) {
        this.pluginId = builder.pluginId;
        this.targetId = builder.targetId;
        this.version = Objects.requireNonNull(builder.version);
        this.parent = builder.parentId;
        this.targetClass = Objects.requireNonNull(builder.targetClass);
        this.extData = builder.extData;
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getId() {
        return Namespaces.combine(pluginId, targetId);
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

    public Config getExtData() {
        return extData;
    }

    @Override
    public String toString() {
        return getId();
    }

    public static Builder builder(String pluginId, String targetId) {
        final var first = Namespaces.simpleId(Objects.requireNonNull(pluginId));
        final var second = Namespaces.second(Namespaces.combinedIdWithFirst(targetId, pluginId));
        return new Builder(first, second);
    }

    public static final class Builder {

        private final String pluginId;
        private final String targetId;
        private Version version;
        private String targetClass;
        private String parentId;
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
            this.parentId = parentId == null ? null : Namespaces.combinedId(parentId, pluginId);
            return this;
        }

        public void extData(Config extData) {
            this.extData = Objects.requireNonNull(extData);
        }

    }

}
