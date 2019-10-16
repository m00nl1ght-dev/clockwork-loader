package dev.m00nl1ght.clockwork.util;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.m00nl1ght.clockwork.core.ComponentDefinition;
import dev.m00nl1ght.clockwork.core.ComponentTargetDefinition;
import dev.m00nl1ght.clockwork.core.DependencyDefinition;
import dev.m00nl1ght.clockwork.core.PluginDefinition;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class PluginInfoFile {

    private final File file;
    private final UnmodifiableConfig config;

    public static PluginInfoFile load(File file) {
        final var conf = CommentedFileConfig.builder(file).build();
        conf.load(); conf.close();
        return new PluginInfoFile(file, conf.unmodifiable());
    }

    private PluginInfoFile(File file, UnmodifiableConfig config) {
        this.file = file;
        this.config = config;
    }

    public PluginDefinition.Builder populatePluginBuilder() {
        final var builder = PluginDefinition.builder(config.get("pluginID"));
        builder.displayName(config.get("displayName"));
        builder.version(config.get("version"));
        builder.description(config.getOrElse("description", ""));
        builder.authors(config.getOrElse("authors", List.of()));
        builder.mainClass(config.get("mainClass"));
        final Optional<List<UnmodifiableConfig>> deps = config.getOptional("dependencies");
        deps.ifPresent(l -> l.forEach(d -> builder.dependency(buildDep(d))));
        return builder;
    }

    public void populateComponents(PluginDefinition plugin) {
        final Optional<List<UnmodifiableConfig>> components = config.getOptional("components");
        if (components.isEmpty()) return;
        for (var conf : components.get()) {
            final var builder = ComponentDefinition.builder(plugin, conf.get("id"));
            builder.component(conf.get("class"));
            builder.target(conf.get("target"));
            final Optional<List<UnmodifiableConfig>> deps = conf.getOptional("dependencies");
            deps.ifPresent(l -> l.forEach(d -> builder.dependency(buildDep(d))));
            final Optional<Boolean> optional = conf.getOptional("optional");
            optional.ifPresent(builder::optional);
            builder.build();
        }
    }

    public void populateTargets(PluginDefinition plugin) {
        final Optional<List<UnmodifiableConfig>> targets = config.getOptional("targets");
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
