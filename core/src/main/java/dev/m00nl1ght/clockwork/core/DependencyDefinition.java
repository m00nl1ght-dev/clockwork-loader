package dev.m00nl1ght.clockwork.core;

import com.vdurmont.semver4j.Requirement;
import com.vdurmont.semver4j.Semver;
import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.function.Predicate;

public final class DependencyDefinition {

    private final String componentId;
    private final String descriptor;
    private final Predicate<Semver> acceptsVersion;

    public DependencyDefinition(String componentId, String descriptor, Predicate<Semver> acceptsVersion) {
        this.componentId = Preconditions.notNullOrBlank(componentId, "componentId");
        this.descriptor = Preconditions.notNullOrBlank(descriptor, "descriptor");
        this.acceptsVersion = Preconditions.notNull(acceptsVersion, "acceptsVersion");
    }

    public static DependencyDefinition build(String componentId, Requirement versionReq) {
        return new DependencyDefinition(componentId, componentId + "[" + versionReq + "]", versionReq::isSatisfiedBy);
    }

    public static DependencyDefinition buildIvyRange(String componentId, String versionRange) {
        return build(componentId, Requirement.buildIvy(versionRange));
    }

    public static DependencyDefinition buildAnyVersion(String componentId) {
        return new DependencyDefinition(componentId, componentId + "[*]", v -> true);
    }

    public String getComponentId() {
        return componentId;
    }

    public boolean acceptsVersion(Semver version) {
        return acceptsVersion.test(version);
    }

    public String getDescriptor() {
        return descriptor;
    }

}
