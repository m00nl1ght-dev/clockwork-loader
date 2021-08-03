package dev.m00nl1ght.clockwork.loader;

import dev.m00nl1ght.clockwork.loader.fnder.impl.ModuleLayerPluginFinder;
import dev.m00nl1ght.clockwork.loader.fnder.impl.ModulePathPluginFinder;
import dev.m00nl1ght.clockwork.loader.fnder.impl.NestedPluginFinder;
import dev.m00nl1ght.clockwork.loader.jigsaw.impl.JigsawStrategyFlat;
import dev.m00nl1ght.clockwork.loader.reader.impl.ManifestPluginReader;
import dev.m00nl1ght.clockwork.util.Registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ExtensionContext {

    private final Map<Class<?>, Registry<?>> registryMap = new HashMap<>();

    public ExtensionContext(boolean registerDefaults) {
        if (registerDefaults) {
            ManifestPluginReader.registerTo(this);
            ModuleLayerPluginFinder.registerTo(this);
            ModulePathPluginFinder.registerTo(this);
            NestedPluginFinder.registerTo(this);
            JigsawStrategyFlat.registerTo(this);
        }
    }

    public <T> T get(String name, Class<T> type) {
        return registryFor(type).get(name);
    }
    
    public <T> Collection<T> getAll(Class<T> type) {
        return registryFor(type).getRegistered().values();
    }

    @SuppressWarnings("unchecked")
    public <T> Registry<T> registryFor(Class<T> forClass) {
        return (Registry<T>) registryMap.computeIfAbsent(forClass, Registry::new);
    }

}
