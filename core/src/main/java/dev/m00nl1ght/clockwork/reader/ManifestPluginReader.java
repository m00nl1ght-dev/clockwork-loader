package dev.m00nl1ght.clockwork.reader;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ClockworkLoader;
import dev.m00nl1ght.clockwork.core.plugin.CollectClockworkExtensionsEvent;
import dev.m00nl1ght.clockwork.descriptor.ComponentDescriptor;
import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.descriptor.PluginDescriptor;
import dev.m00nl1ght.clockwork.descriptor.TargetDescriptor;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.ConsumingConfig;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.ImmutableConfig;
import dev.m00nl1ght.clockwork.version.Version;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.jar.Manifest;

public class ManifestPluginReader implements PluginReader {

    public static final String NAME = "ManifestPluginReader";
    public static final PluginReaderType FACTORY = ManifestPluginReader::new;

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String HEADER_PREFIX = "CWL-";
    private static final String HEADER_PLUGIN_ID = HEADER_PREFIX + "Plugin-Id";
    private static final String HEADER_PLUGIN_VERSION = HEADER_PREFIX + "Plugin-Version";
    private static final String HEADER_PLUGIN_NAME = HEADER_PREFIX + "Plugin-Display-Name";
    private static final String HEADER_PLUGIN_DESC = HEADER_PREFIX + "Plugin-Description";
    private static final String HEADER_PLUGIN_AUTHORS = HEADER_PREFIX + "Plugin-Authors";
    private static final String HEADER_PLUGIN_PROCESSORS = HEADER_PREFIX + "Plugin-Processors";
    private static final String HEADER_PLUGIN_PERMISSIONS = HEADER_PREFIX + "Plugin-Permissions";
    private static final String HEADER_TARGET_ID = HEADER_PREFIX + "Target-Id";
    private static final String HEADER_TARGET_EXTENDS = HEADER_PREFIX + "Target-Extends";
    private static final String HEADER_TARGET_INTERNAL_COMPONENTS = HEADER_PREFIX + "Target-Internal-Components";
    private static final String HEADER_COMPONENT_ID = HEADER_PREFIX + "Component-Id";
    private static final String HEADER_COMPONENT_TARGET = HEADER_PREFIX + "Component-Target";
    private static final String HEADER_COMPONENT_EXTENDS = HEADER_PREFIX + "Component-Extends";
    private static final String HEADER_COMPONENT_DEPENDENCIES = HEADER_PREFIX + "Component-Requires";
    private static final String HEADER_COMPONENT_OPTIONAL = HEADER_PREFIX + "Component-Optional";
    private static final String HEADER_COMPONENT_FACTORY_ACCESS = HEADER_PREFIX + "Component-AllowFactoryAccess";

    protected final ReaderConfig config;
    protected final String manifestFilePath;

    protected ManifestPluginReader(ReaderConfig config) {
        this.config = Arguments.notNull(config, "config");
        this.manifestFilePath = config.get("manifestPath");
    }

    public static void registerTo(ClockworkLoader loader) {
        Arguments.notNull(loader, "loader");
        loader.registerReaderType(NAME, FACTORY);
    }

    public static void registerTo(CollectClockworkExtensionsEvent event) {
        Arguments.notNull(event, "event");
        event.registerReaderType(NAME, FACTORY);
    }

    public static ReaderConfig newConfig(String name) {
        return newConfig(name, "META-INF/MANIFEST.MF");
    }

    public static ReaderConfig newConfig(String name, String manifestPath) {
        return new ReaderConfig(name, NAME, Map.of("manifestPath", manifestPath));
    }

    @Override
    public PluginDescriptor read(Path path) {
        final var manifestPath = path.resolve(manifestFilePath);
        if (!Files.exists(manifestPath)) return null;
        try (final var fis = Files.newInputStream(manifestPath)) {
            return read(new Manifest(fis));
        } catch (Exception e) {
            LOGGER.warn("Failed to read manifest: " + manifestPath, e);
            return null;
        }
    }

    public PluginDescriptor read(Manifest manifest) {
        final var mainConfig = new ConsumingConfig(ImmutableConfig.fromAttributes(manifest.getMainAttributes()));
        final String pluginId = mainConfig.getOrNull(HEADER_PLUGIN_ID);
        if (pluginId == null) return null;
        final var descriptorBuilder = PluginDescriptor.builder(pluginId);
        final var version = new Version(mainConfig.get(HEADER_PLUGIN_VERSION), Version.VersionType.IVY);
        descriptorBuilder.displayName(mainConfig.get(HEADER_PLUGIN_NAME));
        descriptorBuilder.description(mainConfig.getOrDefault(HEADER_PLUGIN_DESC, ""));
        mainConfig.getListOrEmpty(HEADER_PLUGIN_AUTHORS).forEach(descriptorBuilder::author);
        mainConfig.getListOrEmpty(HEADER_PLUGIN_PROCESSORS).forEach(descriptorBuilder::markForProcessor);
        mainConfig.getListOrEmpty(HEADER_PLUGIN_PERMISSIONS).forEach(descriptorBuilder::permission);
        mainConfig.throwOnRemaining(e -> e.startsWith(HEADER_PREFIX));

        for (final var entry : manifest.getEntries().entrySet()) {
            final var className = extractClassName(entry.getKey());
            if (className == null) continue;
            final var entryConfig = new ConsumingConfig(ImmutableConfig.fromAttributes(entry.getValue()));
            final var componentId = entryConfig.getOrNull(HEADER_COMPONENT_ID);
            if (componentId != null) {
                if (componentId.equals(pluginId)) {
                    final var mainCompBuilder = ComponentDescriptor.builder(pluginId);
                    mainCompBuilder.version(version);
                    mainCompBuilder.target(ClockworkCore.CORE_TARGET_ID);
                    mainCompBuilder.componentClass(className);
                    entryConfig.getListOrEmpty(HEADER_COMPONENT_DEPENDENCIES)
                            .forEach(d -> mainCompBuilder.dependency(DependencyDescriptor.build(d)));
                    descriptorBuilder.mainComponent(mainCompBuilder.build());
                } else {
                    final var compBuilder = ComponentDescriptor.builder(pluginId, verifyId(componentId, pluginId));
                    compBuilder.version(version);
                    compBuilder.componentClass(className);
                    compBuilder.target(entryConfig.get(HEADER_COMPONENT_TARGET));
                    compBuilder.parent(autoId(entryConfig.getOrNull(HEADER_COMPONENT_EXTENDS), pluginId));
                    entryConfig.getListOrEmpty(HEADER_COMPONENT_DEPENDENCIES)
                            .forEach(d -> compBuilder.dependency(DependencyDescriptor.build(d)));
                    compBuilder.optional(entryConfig.getBooleanOrDefault(HEADER_COMPONENT_OPTIONAL, false));
                    compBuilder.factoryAccessEnabled(entryConfig.getBooleanOrDefault(HEADER_COMPONENT_FACTORY_ACCESS, false));
                    descriptorBuilder.component(compBuilder.build());
                }
            } else {
                final var targetId = entryConfig.getOrNull(HEADER_TARGET_ID);
                if (targetId != null) {
                    final var builder = TargetDescriptor.builder(pluginId, verifyId(targetId, pluginId));
                    builder.version(version);
                    builder.targetClass(className);
                    builder.parent(entryConfig.getOrNull(HEADER_TARGET_EXTENDS));
                    entryConfig.getListOrEmpty(HEADER_TARGET_INTERNAL_COMPONENTS).forEach(builder::internalComponent);
                    descriptorBuilder.target(builder.build());
                }
            }
            mainConfig.throwOnRemaining(e -> e.startsWith(HEADER_PREFIX));
        }

        return descriptorBuilder.build();
    }

    private String extractClassName(String entryName) {
        if (!entryName.endsWith(".class")) return null;
        return entryName.substring(0, entryName.length() - 6).replace('/', '.');
    }

    private String verifyId(String id, String pluginId) {
        final var idx = id.indexOf(':');
        if (idx < 0) return id;
        final var pId = id.substring(0, idx);
        if (!pId.equals(pluginId))
            throw FormatUtil.rtExc("plugin with id [] can't define mismatched id []", pluginId, id);
        return id.substring(idx + 1);
    }

    private String autoId(String id, String pluginId) {
        if (id == null) return null;
        return id.contains(":") ? id : (pluginId + id);
    }

    @Override
    public String toString() {
        return config.getType() + "[" + config.getName() +  "]";
    }

}
