package dev.m00nl1ght.clockwork.descriptor;

import dev.m00nl1ght.clockwork.config.Config;
import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.version.Version;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class PluginDescriptor {

    private final String id;
    private final String displayName;
    private final String description;
    private final List<String> authors;
    private final ComponentDescriptor mainComponent;
    private final List<ComponentDescriptor> components;
    private final List<TargetDescriptor> targets;
    private final Config extData;

    PluginDescriptor(Builder builder) {
        this.id = builder.id;
        this.displayName = Objects.requireNonNull(builder.displayName);
        this.description = Objects.requireNonNull(builder.description);
        this.authors = List.copyOf(builder.authors);
        this.mainComponent = Objects.requireNonNull(builder.mainComponent);
        this.components = List.copyOf(builder.components);
        this.targets = List.copyOf(builder.targets);
        this.extData = builder.extData;
    }

    public String getId() {
        return id;
    }

    public Version getVersion() {
        return mainComponent.getVersion();
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

    public ComponentDescriptor getMainComponent() {
        return mainComponent;
    }

    public List<ComponentDescriptor> getComponentDescriptors() {
        return components;
    }

    public List<TargetDescriptor> getTargetDescriptors() {
        return targets;
    }

    public Config getExtData() {
        return extData;
    }

    @Override
    public String toString() {
        return getId() + "[" + getVersion() + "]";
    }

    public static Builder builder(String id) {
        return new Builder(Namespaces.simpleId(Objects.requireNonNull(id)));
    }

    public static final class Builder {

        private final String id;
        private String displayName;
        private String description = "";
        private final LinkedList<String> authors = new LinkedList<>();
        private ComponentDescriptor mainComponent;
        private final LinkedList<ComponentDescriptor> components = new LinkedList<>();
        private final LinkedList<TargetDescriptor> targets = new LinkedList<>();
        private Config extData = Config.EMPTY;

        private Builder(String id) {
            this.id = id;
        }

        public PluginDescriptor build() {
            if (mainComponent != null && !components.contains(mainComponent)) components.addFirst(mainComponent);
            return new PluginDescriptor(this);
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

        public Builder mainComponent(ComponentDescriptor mainComponent) {
            this.mainComponent = mainComponent;
            if (mainComponent == null) return this;
            if (!mainComponent.getId().equals(id))
                throw new IllegalArgumentException("mainComponent id must be equal to plugin id");
            if (!mainComponent.getTargetId().equals(ClockworkCore.CORE_TARGET_ID))
                throw new IllegalArgumentException("mainComponent target must be [" + ClockworkCore.CORE_TARGET_ID + "]");
            if (mainComponent.isOptional())
                throw new IllegalArgumentException("mainComponent can not be optional");
            return this;
        }

        public Builder component(ComponentDescriptor component) {
            if (component == null) return this;
            if (!component.getPluginId().equals(id))
                throw new IllegalArgumentException("component is from different plugin");
            if (!components.contains(component)) this.components.add(component);
            return this;
        }

        public Builder target(TargetDescriptor target) {
            if (target == null) return this;
            if (!target.getPluginId().equals(id))
                throw new IllegalArgumentException("target is from different plugin");
            if (!targets.contains(target)) this.targets.add(target);
            return this;
        }

        public void extData(Config extData) {
            this.extData = Objects.requireNonNull(extData).immutable();
        }

    }

}
