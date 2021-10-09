package dev.m00nl1ght.clockwork.utils.config.impl;

import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.ConfigSpec;
import dev.m00nl1ght.clockwork.utils.config.ModifiableConfig;
import dev.m00nl1ght.clockwork.utils.config.SimpleDataParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public abstract class AbstractSDPConfig implements Config {

    protected final SimpleDataParser.Format dataFormat;
    protected final SimpleDataParser.StringSegment stringFormat;
    protected final SimpleDataParser.ConfigSegment configFormat;
    protected final SimpleDataParser.StringListSegment listFormat;
    protected final SimpleDataParser.ConfigListSegment configListFormat;

    protected AbstractSDPConfig(SimpleDataParser.Format dataFormat) {
        this.dataFormat = Objects.requireNonNull(dataFormat);
        this.stringFormat = dataFormat.getSegment(SimpleDataParser.StringSegment.class).orElseThrow();
        this.configFormat = dataFormat.getSegment(SimpleDataParser.ConfigSegment.class).orElseThrow();
        this.listFormat = dataFormat.getSegment(SimpleDataParser.StringListSegment.class).orElseThrow();
        this.configListFormat = dataFormat.getSegment(SimpleDataParser.ConfigListSegment.class).orElseThrow();
    }

    @Override
    public String getString(String key) {
        final var raw = getRaw(key);
        return raw == null ? null : SimpleDataParser.parse(dataFormat, stringFormat, raw);
    }

    @Override
    public Config getSubconfig(String key) {
        final var raw = getRaw(key);
        return raw == null ? null : SimpleDataParser.parse(dataFormat, configFormat, raw);
    }

    @Override
    public List<String> getStrings(String key) {
        final var raw = getRaw(key);
        return raw == null ? null : SimpleDataParser.parse(dataFormat, listFormat, raw);
    }

    @Override
    public List<Config> getSubconfigs(String key) {
        final var raw = getRaw(key);
        return raw == null ? null : SimpleDataParser.parse(dataFormat, configListFormat, raw);
    }

    protected abstract String getRaw(String key);

    @Override
    public Config copy(@Nullable ConfigSpec spec) {
        return build().copy(spec);
    }

    @Override
    public ModifiableConfig modifiableCopy(@Nullable ConfigSpec spec) {
        return spec == null ? build() : build().modifiableCopy(spec);
    }

    protected ModifiableConfig build() {
        final var config = Config.newConfig();

        for (final var key : getKeys()) {
            final var value = getRaw(key);

            final var subconfig = SimpleDataParser.parse(dataFormat, configFormat, value);
            if (subconfig != null) {
                config.putSubconfig(key, subconfig);
                continue;
            }

            final var stringList = SimpleDataParser.parse(dataFormat, listFormat, value);
            if (stringList != null) {
                config.putStrings(key, stringList);
                continue;
            }

            final var configList = SimpleDataParser.parse(dataFormat, configListFormat, value);
            if (configList != null) {
                config.putSubconfigs(key, configList);
                continue;
            }

            final var str = SimpleDataParser.parse(dataFormat, stringFormat, value);
            if (str != null) {
                config.putString(key, str);
            }
        }

        return config;
    }

    public @NotNull SimpleDataParser.Format getDataFormat() {
        return dataFormat;
    }

}
