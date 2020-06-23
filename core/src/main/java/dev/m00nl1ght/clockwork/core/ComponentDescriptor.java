package dev.m00nl1ght.clockwork.core;

import com.vdurmont.semver4j.Requirement;
import com.vdurmont.semver4j.Semver;
import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.regex.Pattern;

public final class ComponentDescriptor {

    public static final Pattern PLUGIN_ID_PATTERN = Pattern.compile("^[a-z][a-z0-9_-]{3,32}$");
    public static final Pattern COMPONENT_ID_PATTERN = Pattern.compile("^([a-z][a-z0-9_-]{3,32})(?::([a-z][a-z0-9_-]{3,32}))?$");
    public static final Pattern DESCRIPTOR_PATTERN = Pattern.compile("^([a-z][a-z0-9_-]{3,32})(?::([a-z][a-z0-9_-]{3,32}))?(?:\\[(.*)])?$");

    private final String plugin;
    private final String component;
    private final String ivyRange;
    private final Requirement versionRequirement;

    private ComponentDescriptor(String targetId, String ivyRange, Requirement versionRequirement) {
        this.ivyRange = ivyRange;
        this.versionRequirement = versionRequirement;
        Preconditions.notNullOrBlank(targetId, "targetId");
        final var matcher = COMPONENT_ID_PATTERN.matcher(targetId);
        if (matcher.matches()) {
            this.plugin = matcher.group(1);
            this.component = matcher.group(2) == null ? "" : matcher.group(2);
        } else {
            throw new IllegalArgumentException("invalid descriptor: " + targetId);
        }
    }

    public static ComponentDescriptor build(String descriptor) {
        Preconditions.notNullOrBlank(descriptor, "descriptor");
        final var matcher = DESCRIPTOR_PATTERN.matcher(descriptor);
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

    public static ComponentDescriptor buildIvyRange(String targetId, String versionRange) {
        Preconditions.notNullOrEmpty(versionRange, "versionRange");
        return new ComponentDescriptor(targetId, versionRange, Requirement.buildIvy(versionRange));
    }

    public static ComponentDescriptor buildExact(String targetId, Semver version) {
        Preconditions.notNull(version, "version");
        return new ComponentDescriptor(targetId, version.toString(), Requirement.build(version));
    }

    public static ComponentDescriptor buildAnyVersion(String targetId) {
        return new ComponentDescriptor(targetId, "", null);
    }

    public boolean acceptsVersion(Semver version) {
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
