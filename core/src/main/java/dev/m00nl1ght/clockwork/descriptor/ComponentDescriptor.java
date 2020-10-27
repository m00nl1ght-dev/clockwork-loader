package dev.m00nl1ght.clockwork.descriptor;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.PluginLoadingException;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.config.Config;
import dev.m00nl1ght.clockwork.version.Version;

import java.util.*;

public final class ComponentDescriptor {

    private final String pluginId;
    private final String componentId;
    private final Version version;
    private final String targetId;
    private final String parent;
    private final String componentClass;
    private final List<DependencyDescriptor> dependencies;
    private final boolean factoryAccessEnabled;
    private final boolean optional;
    private final Config extData;

    ComponentDescriptor(Builder builder) {
        this.pluginId = Arguments.notNullOrBlank(builder.pluginId, "pluginId");
        this.componentId = builder.componentId;
        this.version = Arguments.notNull(builder.version, "version");
        this.targetId = Arguments.notNullOrBlank(builder.targetId, "targetId");
        this.parent = builder.parentId;
        this.componentClass = Arguments.notNullOrBlank(builder.componentClass, "componentClass");
        this.dependencies = List.copyOf(Arguments.notNull(builder.dependencies, "dependencies").values());
        this.factoryAccessEnabled = builder.factoryAccessEnabled;
        this.optional = builder.optional;
        this.extData = Objects.requireNonNull(builder.extData);
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getId() {
        return formatId(pluginId, componentId);
    }

    public Version getVersion() {
        return version;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getParent() {
        return parent;
    }

    public String getComponentClass() {
        return componentClass;
    }

    public List<DependencyDescriptor> getDependencies() {
        return dependencies;
    }

    public boolean isOptional() {
        return optional;
    }

    public boolean isFactoryAccessEnabled() {
        return factoryAccessEnabled;
    }

    public Config getExtData() {
        return extData;
    }

    @Override
    public String toString() {
        return getId();
    }

    public static Builder builder(String pluginId) {
        Arguments.notNullOrBlank(pluginId, "pluginId");
        if (!DependencyDescriptor.PLUGIN_ID_PATTERN.matcher(pluginId).matches())
            throw PluginLoadingException.invalidId(pluginId);
        return new Builder(pluginId, null);
    }

    public static Builder builder(String pluginId, String componentId) {
        Arguments.notNullOrBlank(pluginId, "pluginId");
        Arguments.notNullOrBlank(componentId, "componentId");
        final var resultingId = formatId(pluginId, componentId);
        if (!DependencyDescriptor.COMPONENT_ID_PATTERN.matcher(resultingId).matches())
            throw PluginLoadingException.invalidId(resultingId);
        return new Builder(pluginId, componentId);
    }

    public static final class Builder {

        private final String pluginId;
        private final String componentId;
        private Version version;
        private String componentClass;
        private String targetId;
        private String parentId;
        private final Map<String, DependencyDescriptor> dependencies = new LinkedHashMap<>();
        private boolean factoryAccessEnabled = false;
        private boolean optional = false;
        private Config extData = Config.EMPTY;

        private Builder(String pluginId, String componentId) {
            this.pluginId = pluginId;
            this.componentId = componentId;
        }

        public ComponentDescriptor build() {
            this.addTrivialDeps();
            return new ComponentDescriptor(this);
        }

        public Builder version(Version version) {
            this.version = version;
            return this;
        }

        private void addTrivialDeps() {
            if (!pluginId.equals(ClockworkCore.CORE_PLUGIN_ID)) {
                dependencies.computeIfAbsent(ClockworkCore.CORE_PLUGIN_ID, DependencyDescriptor::buildAnyVersion);
                if (componentId != null) dependencies.computeIfAbsent(pluginId, DependencyDescriptor::buildAnyVersion);
                if (targetId != null) dependencies.computeIfAbsent(pluginId(targetId), DependencyDescriptor::buildAnyVersion);
                if (parentId != null) dependencies.computeIfAbsent(parentId, DependencyDescriptor::buildAnyVersion);
            }
        }

        public Builder componentClass(String componentClass) {
            this.componentClass = componentClass;
            return this;
        }

        public Builder target(String targetId) {
            this.targetId = targetId;
            return this;
        }

        public Builder parent(String parentId) {
            this.parentId = parentId == null ? null : parentId.contains(":") ? parentId : pluginId + ":" + parentId;
            return this;
        }

        public Builder dependency(DependencyDescriptor dependency) {
            final var prev = this.dependencies.putIfAbsent(dependency.getTarget(), dependency);
            if (prev != null) throw PluginLoadingException.dependencyDuplicate(formatId(pluginId, componentId), dependency, prev);
            return this;
        }

        public Builder optional(boolean optional) {
            this.optional = optional;
            return this;
        }

        public Builder optional() {
            return optional(true);
        }

        public void factoryAccessEnabled(boolean factoryAccessEnabled) {
            this.factoryAccessEnabled = factoryAccessEnabled;
        }

        public void extData(Config extData) {
            this.extData = Objects.requireNonNull(extData);
        }

    }

    private static String pluginId(String id) {
        final var i = id.indexOf(':');
        return i < 0 ? id : id.substring(0, i);
    }

    private static String formatId(String pluginId, String componentId) {
        return componentId == null ? pluginId : (pluginId + ":" + componentId);
    }

}
