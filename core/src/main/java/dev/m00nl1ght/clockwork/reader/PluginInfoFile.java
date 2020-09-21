package dev.m00nl1ght.clockwork.reader;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.toml.TomlFormat;
import dev.m00nl1ght.clockwork.core.*;
import dev.m00nl1ght.clockwork.descriptor.*;
import dev.m00nl1ght.clockwork.version.Version;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class PluginInfoFile {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String INFO_FILE = "plugin.toml";

    private final UnmodifiableConfig config;
    private final String pluginId;

    public static PluginInfoFile loadFromDir(Path path) {
        if (Files.isDirectory(path)) {
            return load(path.resolve(INFO_FILE));
        } else if (Files.isRegularFile(path)) {
            try (final var fs = FileSystems.newFileSystem(path, PluginInfoFile.class.getClassLoader())) {
                return load(fs.getPath(INFO_FILE));
            } catch (IOException | FileSystemNotFoundException e) {
                LOGGER.debug("Failed to open as filesystem: " + path, e);
                return null;
            }
        } else {
            return null;
        }
    }

    public static PluginInfoFile load(Path path) {
        if (!Files.exists(path)) return null;
        final var parser = TomlFormat.instance().createParser();
        final var conf = parser.parse(path, FileNotFoundAction.THROW_ERROR);
        return new PluginInfoFile(conf.unmodifiable());
    }

    private PluginInfoFile(UnmodifiableConfig config) {
        this.config = config;
        this.pluginId = config.get("plugin_id");
    }

    public PluginReference.Builder populatePluginBuilder() {
        final var descriptorBuilder = PluginDescriptor.builder();
        descriptorBuilder.id(pluginId);
        descriptorBuilder.displayName(config.get("display_name"));
        descriptorBuilder.version(new Version(config.get("version"), Version.VersionType.IVY));
        descriptorBuilder.description(config.getOrElse("description", ""));
        final Optional<List<String>> authors = config.getOptional("authors");
        authors.ifPresent(l -> l.forEach(descriptorBuilder::author));
        final Optional<List<UnmodifiableConfig>> perms = config.getOptional("permission");
        perms.ifPresent(l -> l.forEach(p -> descriptorBuilder.permission(buildPerm(p))));
        final Optional<List<String>> processors = config.getOptional("processors");
        processors.ifPresent(l -> l.forEach(descriptorBuilder::markForProcessor));
        final var descriptor = descriptorBuilder.build();

        final var mainCompBuilder = ComponentDescriptor.builder(descriptor);
        mainCompBuilder.id(pluginId);
        mainCompBuilder.target(ClockworkCore.CORE_TARGET_ID);
        mainCompBuilder.componentClass(config.get("main_class"));
        final Optional<List<UnmodifiableConfig>> deps = config.getOptional("dependency");
        deps.ifPresent(l -> l.forEach(d -> mainCompBuilder.dependency(buildDep(d))));
        final var mainComp = mainCompBuilder.build();

        final var referenceBuilder = PluginReference.builder(descriptor);
        referenceBuilder.mainComponent(mainComp);

        final Optional<List<UnmodifiableConfig>> components = config.getOptional("component");
        if (components.isPresent()) for (var conf : components.get()) {
            final var builder = ComponentDescriptor.builder(descriptor);
            builder.id(conf.get("id"));
            builder.componentClass(conf.get("class"));
            builder.parent(conf.getOrElse("parent", () -> null));
            builder.target(conf.get("target"));
            final Optional<List<UnmodifiableConfig>> compDeps = conf.getOptional("dependency");
            compDeps.ifPresent(l -> l.forEach(d -> builder.dependency(buildDep(d))));
            final Optional<Boolean> optional = conf.getOptional("optional");
            optional.ifPresent(builder::optional);
            final Optional<Boolean> factoryAccess = conf.getOptional("factoryAccess");
            factoryAccess.ifPresent(builder::factoryAccessEnabled);
            referenceBuilder.component(builder.build());
        }

        final Optional<List<UnmodifiableConfig>> targets = config.getOptional("target");
        if (targets.isPresent()) for (var conf : targets.get()) {
            final var builder = TargetDescriptor.builder(descriptor);
            builder.id(conf.get("id"));
            builder.targetClass(conf.get("class"));
            builder.parent(conf.getOrElse("parent", () -> null));
            referenceBuilder.target(builder.build());
        }

        return referenceBuilder;
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

    public String getPluginId() {
        return pluginId;
    }

}
