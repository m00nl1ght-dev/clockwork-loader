package dev.m00nl1ght.clockwork.loader.reader.impl;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.descriptor.*;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.reader.PluginReader;
import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.ModifiableConfig;
import dev.m00nl1ght.clockwork.utils.version.Version;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.jar.Manifest;

public class ManifestPluginReader implements PluginReader {

    public static final String TYPE = "internal.pluginreader.manifest";

    public static void registerTo(ClockworkLoader loader) {
        loader.getFeatureProviders().register(PluginReader.class, TYPE, ManifestPluginReader::new);
    }

    public static ModifiableConfig newConfig(String name) {
        return newConfig(name, "META-INF/MANIFEST.MF");
    }

    public static ModifiableConfig newConfig(String name, String manifestPath) {
        return Config.newConfig()
                .putString("type", TYPE)
                .putString("name", name)
                .putString("manifestPath", manifestPath);
    }

    private static final String HEADER_PREFIX = "CWL-";
    private static final String HEADER_EXT_PREFIX = "EXT-";
    private static final String HEADER_EXT_COMBINED = HEADER_PREFIX + HEADER_EXT_PREFIX;
    private static final String HEADER_PLUGIN_ID = "Plugin-Id";
    private static final String HEADER_PLUGIN_VERSION = "Plugin-Version";
    private static final String HEADER_PLUGIN_NAME = "Plugin-Display-Name";
    private static final String HEADER_PLUGIN_DESC = "Plugin-Description";
    private static final String HEADER_PLUGIN_AUTHORS = "Plugin-Authors";
    private static final String HEADER_TARGET_ID = "Target-Id";
    private static final String HEADER_TARGET_EXTENDS = "Target-Extends";
    private static final String HEADER_COMPONENT_ID = "Component-Id";
    private static final String HEADER_COMPONENT_TARGET = "Component-Target";
    private static final String HEADER_COMPONENT_DEPENDENCIES = "Component-Requires";
    private static final String HEADER_COMPONENT_OPTIONAL = "Component-Optional";
    private static final String HEADER_COMPONENT_FACTORY_CHANGES = "Component-FactoryChangesAllowed";

    protected final String name;
    protected final String manifestFilePath;

    protected ManifestPluginReader(ClockworkLoader loader, Config config) {
        this.name = config.getRequired("name", Config.STRING);
        this.manifestFilePath = config.getRequired("manifestPath", Config.STRING);
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
        final var mainConfig = Config.fromAttributes(manifest.getMainAttributes(), HEADER_PREFIX);
        final String pluginId = mainConfig.getString(HEADER_PLUGIN_ID);
        if (pluginId == null) return Optional.empty();
        final var descriptorBuilder = PluginDescriptor.builder(pluginId);
        final var version = new Version(mainConfig.getRequired(HEADER_PLUGIN_VERSION, Config.STRING), Version.VersionType.IVY);
        descriptorBuilder.displayName(mainConfig.getRequired(HEADER_PLUGIN_NAME, Config.STRING));
        descriptorBuilder.description(mainConfig.getOrDefault(HEADER_PLUGIN_DESC, Config.STRING, ""));
        mainConfig.getOrDefault(HEADER_PLUGIN_AUTHORS, Config.LISTF, List.of()).forEach(descriptorBuilder::author);

        descriptorBuilder.extData(Config.fromAttributes(manifest.getMainAttributes(), HEADER_EXT_COMBINED));

        for (final var entry : manifest.getEntries().entrySet()) {
            final var className = extractClassName(entry.getKey());
            if (className == null) continue;
            final var entryConfig = Config.fromAttributes(entry.getValue(), HEADER_PREFIX);
            final var componentId = entryConfig.getString(HEADER_COMPONENT_ID);
            if (componentId != null) {
                if (componentId.equals(pluginId)) {
                    final var mainCompBuilder = ComponentDescriptor.builder(pluginId);
                    mainCompBuilder.version(version);
                    mainCompBuilder.target(ClockworkCore.CORE_TARGET_ID);
                    mainCompBuilder.componentClass(className);
                    entryConfig.getOrDefault(HEADER_COMPONENT_DEPENDENCIES, Config.LISTF, List.of())
                            .forEach(d -> mainCompBuilder.dependency(DependencyDescriptor.build(d)));
                    mainCompBuilder.factoryChangesAllowed(entryConfig.getOrDefault(HEADER_COMPONENT_FACTORY_CHANGES, Config.BOOLEAN, false));
                    mainCompBuilder.extData(Config.fromAttributes(entry.getValue(), HEADER_EXT_COMBINED));
                    descriptorBuilder.mainComponent(mainCompBuilder.build());
                } else {
                    final var compBuilder = ComponentDescriptor.builder(pluginId, componentId);
                    compBuilder.version(version);
                    compBuilder.componentClass(className);
                    compBuilder.target(Namespaces.combinedId(entryConfig.getRequired(HEADER_COMPONENT_TARGET, Config.STRING), pluginId));
                    entryConfig.getOrDefault(HEADER_COMPONENT_DEPENDENCIES, Config.LISTF, List.of())
                            .forEach(d -> compBuilder.dependency(DependencyDescriptor.build(d)));
                    compBuilder.optional(entryConfig.getOrDefault(HEADER_COMPONENT_OPTIONAL, Config.BOOLEAN, false));
                    compBuilder.factoryChangesAllowed(entryConfig.getOrDefault(HEADER_COMPONENT_FACTORY_CHANGES, Config.BOOLEAN, false));
                    compBuilder.extData(Config.fromAttributes(entry.getValue(), HEADER_EXT_COMBINED));
                    descriptorBuilder.component(compBuilder.build());
                }
            } else {
                final var targetId = entryConfig.getString(HEADER_TARGET_ID);
                if (targetId != null) {
                    final var builder = TargetDescriptor.builder(pluginId, targetId);
                    builder.version(version);
                    builder.targetClass(className);
                    entryConfig.getOptional(HEADER_TARGET_EXTENDS, Config.STRING)
                            .ifPresent(e -> builder.parent(Namespaces.combinedId(e, pluginId)));
                    builder.extData(Config.fromAttributes(entry.getValue(), HEADER_EXT_COMBINED));
                    descriptorBuilder.target(builder.build());
                }
            }
        }

        return Optional.of(descriptorBuilder.build());
    }

    private String extractClassName(String entryName) {
        if (!entryName.endsWith(".class")) return null;
        return entryName.substring(0, entryName.length() - 6).replace('/', '.');
    }

    @Override
    public String toString() {
        return TYPE + "[" + name +  "]";
    }

}
