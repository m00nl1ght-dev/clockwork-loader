package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.function.Predicate;

public final class DependencyDefinition {

    private final String componentId;
    private final String descriptor;
    private final Predicate<String> acceptsVersion;

    public DependencyDefinition(String componentId, String descriptor, Predicate<String> acceptsVersion) {
        this.componentId = Preconditions.notNullOrBlank(componentId, "componentId");
        this.descriptor = Preconditions.notNullOrBlank(descriptor, "descriptor");
        this.acceptsVersion = Preconditions.notNull(acceptsVersion, "acceptsVersion");
    }

    public static DependencyDefinition build(String componentId, String minVersion, String maxVersion) {
        Predicate<String> range = v -> true; // TODO implement version range
        return new DependencyDefinition(componentId, componentId + "[" + minVersion + ";" + maxVersion + "]", range);
    }

    public static DependencyDefinition build(String componentId, String minVersion) {
        Predicate<String> range = v -> true; // TODO implement version range
        return new DependencyDefinition(componentId, componentId + "[" + minVersion + "+]", range);
    }

    public static DependencyDefinition build(String componentId) {
        return new DependencyDefinition(componentId, componentId + "[+]", v -> true);
    }

    public String getComponentId() {
        return componentId;
    }

    public boolean acceptsVersion(String version) {
        return acceptsVersion.test(version);
    }

    public String getDescriptor() {
        return descriptor;
    }

}
