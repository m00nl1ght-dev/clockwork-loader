package dev.m00nl1ght.clockwork.descriptor;

import dev.m00nl1ght.clockwork.version.Version;
import dev.m00nl1ght.clockwork.version.VersionRequirement;

import java.util.Objects;

import static dev.m00nl1ght.clockwork.descriptor.Namespaces.COMBINED_ID_PATTERN;
import static dev.m00nl1ght.clockwork.descriptor.Namespaces.DEPENDENCY_PATTERN;

public final class DependencyDescriptor {

    private final String plugin;
    private final String component;
    private final String ivyRange;
    private final VersionRequirement versionRequirement;

    private DependencyDescriptor(String targetId, String ivyRange, VersionRequirement versionRequirement) {
        this.ivyRange = ivyRange;
        this.versionRequirement = versionRequirement;
        Objects.requireNonNull(targetId);
        final var matcher = COMBINED_ID_PATTERN.matcher(targetId);
        if (matcher.matches()) {
            this.plugin = matcher.group(1);
            this.component = matcher.group(2) == null ? "" : matcher.group(2);
        } else {
            throw new IllegalArgumentException("invalid descriptor: " + targetId);
        }
    }

    public static DependencyDescriptor build(String descriptor) {
        Objects.requireNonNull(descriptor);
        final var matcher = DEPENDENCY_PATTERN.matcher(descriptor);
        if (matcher.matches()) {
            final var plugin = matcher.group(1);
            final var component = matcher.group(2);
            final var range = matcher.group(3);
            final var id = component == null ? plugin : plugin + ":" + component;
            return range == null ? buildAnyVersion(id) : buildIvyRange(id, range);
        } else {
            throw new IllegalArgumentException("invalid descriptor: " + descriptor);
        }
    }

    public static DependencyDescriptor buildIvyRange(String targetId, String versionRange) {
        Objects.requireNonNull(versionRange);
        return new DependencyDescriptor(targetId, versionRange, VersionRequirement.buildIvy(versionRange));
    }

    public static DependencyDescriptor buildExact(String targetId, Version version) {
        Objects.requireNonNull(version);
        return new DependencyDescriptor(targetId, version.toString(), VersionRequirement.build(version));
    }

    public static DependencyDescriptor buildAnyVersion(String targetId) {
        return new DependencyDescriptor(targetId, "", null);
    }

    public boolean acceptsVersion(Version version) {
        return versionRequirement == null || versionRequirement.isSatisfiedBy(version);
    }

    public String getPlugin() {
        return plugin;
    }

    public String getComponent() {
        return component;
    }

    public String getTarget() {
        return component.isEmpty() ? plugin : plugin + ":" + component;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();

        builder.append(plugin);

        if (!component.isEmpty()) {
            builder.append(':');
            builder.append(component);
        }

        if (!ivyRange.isEmpty()) {
            builder.append('[');
            builder.append(ivyRange);
            builder.append(']');
        }

        return builder.toString();
    }

}
