package dev.m00nl1ght.clockwork.locator;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.m00nl1ght.clockwork.core.ComponentDefinition;
import dev.m00nl1ght.clockwork.core.ComponentTargetDefinition;
import dev.m00nl1ght.clockwork.core.DependencyDefinition;
import dev.m00nl1ght.clockwork.core.PluginDefinition;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class PluginInfoFile {

    public static final String INFO_FILE_DIR = "META-INF";
    public static final String INFO_FILE_NAME = "plugin.toml";

    private final Path file;
    private final UnmodifiableConfig config;

    public static PluginInfoFile load(Path file) {
        final var conf = CommentedFileConfig.builder(file).build();
        conf.load(); conf.close();
        return new PluginInfoFile(file, conf.unmodifiable());
    }

    private PluginInfoFile(Path file, UnmodifiableConfig config) {
        this.file = file;
        this.config = config;
    }

    public PluginDefinition.Builder populatePluginBuilder() {
        final var builder = PluginDefinition.builder(config.get("plugin_id"));
        builder.displayName(config.get("display_name"));
        builder.version(config.get("version"));
        builder.description(config.getOrElse("description", ""));
        builder.authors(config.getOrElse("authors", List.of()));
        builder.mainClass(config.get("main_class"));
        final Optional<List<UnmodifiableConfig>> deps = config.getOptional("dependency");
        deps.ifPresent(l -> l.forEach(d -> builder.dependency(buildDep(d))));
        return builder;
    }

    public void populateComponents(PluginDefinition plugin) {
        final Optional<List<UnmodifiableConfig>> components = config.getOptional("component");
        if (components.isEmpty()) return;
        for (var conf : components.get()) {
            final var builder = ComponentDefinition.builder(plugin, conf.get("id"));
            builder.component(conf.get("class"));
            builder.target(conf.get("target"));
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
            ComponentTargetDefinition.build(plugin, id, targetClass);
        }
    }

    private DependencyDefinition buildDep(UnmodifiableConfig conf) {
        final String id = conf.get("id");
        final Optional<String> verStr = conf.getOptional("version");
        // TODO version range, ...
        return verStr.map(s -> DependencyDefinition.build(id, s)).orElseGet(() -> DependencyDefinition.build(id));
    }

}
