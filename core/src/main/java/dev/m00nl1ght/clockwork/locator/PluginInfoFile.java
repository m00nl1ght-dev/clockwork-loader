package dev.m00nl1ght.clockwork.locator;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.vdurmont.semver4j.Semver;
import dev.m00nl1ght.clockwork.core.ComponentDefinition;
import dev.m00nl1ght.clockwork.core.ComponentDescriptor;
import dev.m00nl1ght.clockwork.core.PluginDefinition;
import dev.m00nl1ght.clockwork.core.TargetDefinition;
import dev.m00nl1ght.clockwork.events.annotation.EventListenerAnnotationProcessor;
import dev.m00nl1ght.clockwork.interfaces.ComponentInterfaceAnnotationProcessor;
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

    public PluginDefinition.Builder populatePluginBuilder() {
        final var builder = PluginDefinition.builder(pluginId);
        builder.displayName(config.get("display_name"));
        builder.version(new Semver(config.get("version"), Semver.SemverType.IVY));
        builder.description(config.getOrElse("description", ""));
        builder.authors(config.getOrElse("authors", List.of()));
        builder.mainClass(config.get("main_class"));
        builder.markForProcessor(EventListenerAnnotationProcessor.NAME);
        builder.markForProcessor(ComponentInterfaceAnnotationProcessor.NAME);
        final Optional<List<UnmodifiableConfig>> deps = config.getOptional("dependency");
        deps.ifPresent(l -> l.forEach(d -> builder.dependency(buildDep(d))));
        final Optional<List<UnmodifiableConfig>> perms = config.getOptional("permission");
        perms.ifPresent(l -> l.forEach(p -> builder.permission(buildPerm(p))));
        return builder;
    }

    public void populateComponents(PluginDefinition plugin) {
        final Optional<List<UnmodifiableConfig>> components = config.getOptional("component");
        if (components.isEmpty()) return;
        for (var conf : components.get()) {
            final var builder = ComponentDefinition.builder(plugin, conf.get("id"));
            builder.component(conf.get("class"));
            builder.target(conf.get("target"));
            builder.markForProcessor(EventListenerAnnotationProcessor.NAME);
            builder.markForProcessor(ComponentInterfaceAnnotationProcessor.NAME);
            final Optional<List<UnmodifiableConfig>> deps = conf.getOptional("dependency");
            deps.ifPresent(l -> l.forEach(d -> builder.dependency(buildDep(d))));
            final Optional<Boolean> optional = conf.getOptional("optional");
            optional.ifPresent(builder::optional);
            builder.build();
        }
    }

    public void populateTargets(PluginDefinition plugin) {
        final Optional<List<UnmodifiableConfig>> targets = config.getOptional("target");
        if (targets.isEmpty()) return;
        for (var conf : targets.get()) {
            final String id = conf.get("id");
            final String targetClass = conf.get("class");
            final String parent = conf.getOrElse("parent", () -> null);
            TargetDefinition.build(plugin, id, autoId(parent, plugin.getId()), targetClass, EventListenerAnnotationProcessor.NAME, ComponentInterfaceAnnotationProcessor.NAME);
        }
    }

    private ComponentDescriptor buildDep(UnmodifiableConfig conf) {
        final String id = conf.get("id");
        final Optional<String> verStr = conf.getOptional("version");
        return verStr.map(s -> ComponentDescriptor.buildIvyRange(id, s)).orElseGet(() -> ComponentDescriptor.buildAnyVersion(id));
    }

    private String buildPerm(UnmodifiableConfig conf) {
        final String perm = conf.get("id");
        final Optional<String> value = conf.getOptional("value");
        return (value.isPresent() && !value.get().isEmpty()) ? perm + ":" + value.get() : perm;
    }

    private static String autoId(String id, String pId) {
        return id == null ? null : id.contains(":") ? id : pId + ":" + id;
    }

    public String getPluginId() {
        return pluginId;
    }

}
