package dev.m00nl1ght.clockwork.reader;

import dev.m00nl1ght.clockwork.config.AttributesWrapper;
import dev.m00nl1ght.clockwork.config.ImmutableConfig;
import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.descriptor.*;
import dev.m00nl1ght.clockwork.util.Registry;
import dev.m00nl1ght.clockwork.version.Version;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.Manifest;

public class ManifestPluginReader implements PluginReader {

    public static final String NAME = "internal.pluginreader.manifest";
    public static final PluginReaderType FACTORY = ManifestPluginReader::new;

    private static final String HEADER_PREFIX = "CWL-";
    private static final String HEADER_EXT_PREFIX = "EXT-";
    private static final String HEADER_EXT_COMBINED = HEADER_PREFIX + HEADER_EXT_PREFIX;
    private static final String HEADER_PLUGIN_ID = "Plugin-Id";
    private static final String HEADER_PLUGIN_VERSION = "Plugin-Version";
    private static final String HEADER_PLUGIN_NAME = "Plugin-Display-Name";
    private static final String HEADER_PLUGIN_DESC = "Plugin-Description";
    private static final String HEADER_PLUGIN_AUTHORS = "Plugin-Authors";
    private static final String HEADER_PLUGIN_PROCESSORS = "Plugin-Processors";
    private static final String HEADER_TARGET_ID = "Target-Id";
    private static final String HEADER_TARGET_EXTENDS = "Target-Extends";
    private static final String HEADER_COMPONENT_ID = "Component-Id";
    private static final String HEADER_COMPONENT_TARGET = "Component-Target";
    private static final String HEADER_COMPONENT_DEPENDENCIES = "Component-Requires";
    private static final String HEADER_COMPONENT_OPTIONAL = "Component-Optional";
    private static final String HEADER_COMPONENT_FACTORY_ACCESS = "Component-AllowFactoryAccess";

    protected final PluginReaderConfig config;
    protected final String manifestFilePath;

    protected ManifestPluginReader(PluginReaderConfig config) {
        this.config = Objects.requireNonNull(config);
        this.manifestFilePath = config.getParams().get("manifestPath");
    }

    public static void registerTo(Registry<PluginReaderType> registry) {
        Objects.requireNonNull(registry).register(NAME, FACTORY);
    }

    public static PluginReaderConfig newConfig(String name) {
        return newConfig(name, "META-INF/MANIFEST.MF");
    }

    public static PluginReaderConfig newConfig(String name, String manifestPath) {
        return PluginReaderConfig.of(name, NAME, ImmutableConfig.builder()
                .putString("manifestPath", manifestPath)
                .build());
    }

    @Override
    public Optional<PluginDescriptor> read(Path path) {
        final var manifestPath = path.resolve(manifestFilePath);
        if (!Files.exists(manifestPath)) return Optional.empty();
        try (final var fis = Files.newInputStream(manifestPath)) {
            return read(new Manifest(fis));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read manifest: " + manifestPath, e);
        }
    }

    public Optional<PluginDescriptor> read(Manifest manifest) {
        final var mainConfig = new AttributesWrapper(manifest.getMainAttributes(), HEADER_PREFIX).strict();
        final String pluginId = mainConfig.getOrNull(HEADER_PLUGIN_ID);
        if (pluginId == null) return Optional.empty();
        final var descriptorBuilder = PluginDescriptor.builder(pluginId);
        final var version = new Version(mainConfig.get(HEADER_PLUGIN_VERSION), Version.VersionType.IVY);
        descriptorBuilder.displayName(mainConfig.get(HEADER_PLUGIN_NAME));
        descriptorBuilder.description(mainConfig.getOrDefault(HEADER_PLUGIN_DESC, ""));
        mainConfig.getListOrSingletonOrEmpty(HEADER_PLUGIN_AUTHORS).forEach(descriptorBuilder::author);
        mainConfig.getListOrSingletonOrEmpty(HEADER_PLUGIN_PROCESSORS).forEach(descriptorBuilder::markForProcessor);
        mainConfig.throwOnRemaining(e -> !e.startsWith(HEADER_EXT_PREFIX));

        descriptorBuilder.extData(new AttributesWrapper(manifest.getMainAttributes(), HEADER_EXT_COMBINED));

        for (final var entry : manifest.getEntries().entrySet()) {
            final var className = extractClassName(entry.getKey());
            if (className == null) continue;
            final var entryConfig = new AttributesWrapper(entry.getValue(), HEADER_PREFIX).strict();
            final var componentId = entryConfig.getOrNull(HEADER_COMPONENT_ID);
            if (componentId != null) {
                if (componentId.equals(pluginId)) {
                    final var mainCompBuilder = ComponentDescriptor.builder(pluginId);
                    mainCompBuilder.version(version);
                    mainCompBuilder.target(ClockworkCore.CORE_TARGET_ID);
                    mainCompBuilder.componentClass(className);
                    entryConfig.getListOrSingletonOrEmpty(HEADER_COMPONENT_DEPENDENCIES)
                            .forEach(d -> mainCompBuilder.dependency(DependencyDescriptor.build(d)));
                    mainCompBuilder.factoryAccessEnabled(entryConfig.getBooleanOrDefault(HEADER_COMPONENT_FACTORY_ACCESS, false));
                    mainCompBuilder.extData(new AttributesWrapper(entry.getValue(), HEADER_EXT_COMBINED));
                    descriptorBuilder.mainComponent(mainCompBuilder.build());
                } else {
                    final var compBuilder = ComponentDescriptor.builder(pluginId, componentId);
                    compBuilder.version(version);
                    compBuilder.componentClass(className);
                    compBuilder.target(Namespaces.combinedId(entryConfig.get(HEADER_COMPONENT_TARGET), pluginId));
                    entryConfig.getListOrSingletonOrEmpty(HEADER_COMPONENT_DEPENDENCIES)
                            .forEach(d -> compBuilder.dependency(DependencyDescriptor.build(d)));
                    compBuilder.optional(entryConfig.getBooleanOrDefault(HEADER_COMPONENT_OPTIONAL, false));
                    compBuilder.factoryAccessEnabled(entryConfig.getBooleanOrDefault(HEADER_COMPONENT_FACTORY_ACCESS, false));
                    compBuilder.extData(new AttributesWrapper(entry.getValue(), HEADER_EXT_COMBINED));
                    descriptorBuilder.component(compBuilder.build());
                }
            } else {
                final var targetId = entryConfig.getOrNull(HEADER_TARGET_ID);
                if (targetId != null) {
                    final var builder = TargetDescriptor.builder(pluginId, targetId);
                    builder.version(version);
                    builder.targetClass(className);
                    entryConfig.getOptional(HEADER_TARGET_EXTENDS)
                            .ifPresent(e -> builder.parent(Namespaces.combinedId(e, pluginId)));
                    builder.extData(new AttributesWrapper(entry.getValue(), HEADER_EXT_COMBINED));
                    descriptorBuilder.target(builder.build());
                }
            }
            entryConfig.throwOnRemaining(e -> !e.startsWith(HEADER_EXT_PREFIX));
        }

        return Optional.of(descriptorBuilder.build());
    }

    private String extractClassName(String entryName) {
        if (!entryName.endsWith(".class")) return null;
        return entryName.substring(0, entryName.length() - 6).replace('/', '.');
    }

    @Override
    public String toString() {
        return config.getType() + "[" + config.getName() +  "]";
    }

}
