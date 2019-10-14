package dev.m00nl1ght.clockwork.util;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import java.io.File;

public class PluginInfoFile {

    private static final ConfigSpec CONFIG_SPEC = new ConfigSpec();

    private final File file;
    private final UnmodifiableCommentedConfig config;

    static {
        CONFIG_SPEC.define("plugin_id", "ExamplePlugin");
    }

    public PluginInfoFile(File file) {
        this.file = file;
        this.config = CommentedFileConfig.builder(file).build().unmodifiable();
    }

}
