package dev.m00nl1ght.clockwork.extension.nightconfig;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ClockworkLoader;
import dev.m00nl1ght.clockwork.core.plugin.CollectClockworkExtensionsEvent;
import dev.m00nl1ght.clockwork.descriptor.ComponentDescriptor;
import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.descriptor.PluginDescriptor;
import dev.m00nl1ght.clockwork.descriptor.TargetDescriptor;
import dev.m00nl1ght.clockwork.reader.PluginReader;
import dev.m00nl1ght.clockwork.reader.PluginReaderType;
import dev.m00nl1ght.clockwork.reader.PluginReaderConfig;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.version.Version;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NightconfigPluginReader implements PluginReader {

    public static final String NAME = "extension.pluginreader.nightconfig";
    public static final PluginReaderType FACTORY = NightconfigPluginReader::new;

    private static final Logger LOGGER = LogManager.getLogger();

    protected final PluginReaderConfig config;
    protected final String descriptorFilePath;

    protected NightconfigPluginReader(PluginReaderConfig config) {
        this.config = Arguments.notNull(config, "config");
        this.descriptorFilePath = config.get("descriptorPath");
    }

    public static void registerTo(ClockworkLoader loader) {
        Arguments.notNull(loader, "loader");
        loader.registerReaderType(NAME, FACTORY);
    }

    public static void registerTo(CollectClockworkExtensionsEvent event) {
        Arguments.notNull(event, "event");
        event.registerReaderType(NAME, FACTORY);
    }

    public static PluginReaderConfig newConfig(String name, String descriptorPath) {
        return new PluginReaderConfig(name, NAME, Map.of("descriptorPath", descriptorPath));
    }

    @Override
    public Optional<PluginDescriptor> read(Path sourcePath) {
        final var path = sourcePath.resolve(descriptorFilePath);
        if (!Files.exists(path)) return Optional.empty();
        final var config = FileConfig.of(path);
        config.load(); config.close();

        final String pluginId = config.get("plugin_id");
        final var descriptorBuilder = PluginDescriptor.builder(pluginId);
        final var version = new Version(config.get("version"), Version.VersionType.IVY);
        descriptorBuilder.displayName(config.get("display_name"));
        descriptorBuilder.description(config.getOrElse("description", ""));
        final Optional<List<String>> authors = config.getOptional("authors");
        authors.ifPresent(l -> l.forEach(descriptorBuilder::author));
        final Optional<List<UnmodifiableConfig>> perms = config.getOptional("permission");
        perms.ifPresent(l -> l.forEach(p -> descriptorBuilder.permission(buildPerm(p))));
        final Optional<List<String>> processors = config.getOptional("processors");
        processors.ifPresent(l -> l.forEach(descriptorBuilder::markForProcessor));

        final var mainCompBuilder = ComponentDescriptor.builder(pluginId);
        mainCompBuilder.version(version);
        mainCompBuilder.target(ClockworkCore.CORE_TARGET_ID);
        mainCompBuilder.componentClass(config.get("main_class"));
        final Optional<List<UnmodifiableConfig>> deps = config.getOptional("dependency");
        deps.ifPresent(l -> l.forEach(d -> mainCompBuilder.dependency(buildDep(d))));
        final var mainComp = mainCompBuilder.build();

        descriptorBuilder.mainComponent(mainComp);

        final Optional<List<UnmodifiableConfig>> components = config.getOptional("component");
        if (components.isPresent()) for (var conf : components.get()) {
            final var builder = ComponentDescriptor.builder(pluginId, verifyId(conf.get("id"), pluginId));
            builder.version(version);
            builder.componentClass(conf.get("class"));
            builder.parent(conf.getOrElse("parent", () -> null));
            builder.target(conf.get("target"));
            final Optional<List<UnmodifiableConfig>> compDeps = conf.getOptional("dependency");
            compDeps.ifPresent(l -> l.forEach(d -> builder.dependency(buildDep(d))));
            final Optional<Boolean> optional = conf.getOptional("optional");
            optional.ifPresent(builder::optional);
            final Optional<Boolean> factoryAccess = conf.getOptional("factoryAccess");
            factoryAccess.ifPresent(builder::factoryAccessEnabled);
            descriptorBuilder.component(builder.build());
        }

        final Optional<List<UnmodifiableConfig>> targets = config.getOptional("target");
        if (targets.isPresent()) for (var conf : targets.get()) {
            final var builder = TargetDescriptor.builder(pluginId, verifyId(conf.get("id"), pluginId));
            builder.version(version);
            builder.targetClass(conf.get("class"));
            builder.parent(conf.get("parent"));
            final Optional<List<String>> internalComps = conf.getOptional("internalComponent");
            internalComps.ifPresent(l -> l.forEach(builder::internalComponent));
            descriptorBuilder.target(builder.build());
        }

        return Optional.of(descriptorBuilder.build());
    }

    private DependencyDescriptor buildDep(UnmodifiableConfig conf) {
        final String id = conf.get("id");
        final Optional<String> verStr = conf.getOptional("version");
        return verStr.map(s -> DependencyDescriptor.buildIvyRange(id, s)).orElseGet(() -> DependencyDescriptor.buildAnyVersion(id));
    }

    private String buildPerm(UnmodifiableConfig conf) {
        final String perm = conf.get("id");
        final Optional<String> value = conf.getOptional("value");
        return (value.isPresent() && !value.get().isEmpty()) ? perm + ":" + value.get() : perm;
    }

    private String verifyId(String id, String pluginId) {
        if (id == null) throw new IllegalStateException("id is missing");
        final var idx = id.indexOf(':');
        if (idx < 0) return id;
        final var pId = id.substring(0, idx);
        if (!pId.equals(pluginId))
            throw FormatUtil.rtExc("plugin with id [] can't define mismatched id []", pluginId, id);
        return id.substring(idx + 1);
    }

    @Override
    public String toString() {
        return config.getType() + "[" + config.getName() +  "]";
    }

}
