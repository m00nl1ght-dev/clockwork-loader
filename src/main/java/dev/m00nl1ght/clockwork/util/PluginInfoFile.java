package dev.m00nl1ght.clockwork.util;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import dev.m00nl1ght.clockwork.core.PluginDefinition;

import java.io.File;

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

    public void populatePluginBuilder(PluginDefinition.Builder builder) {
        // TODO
    }

}
