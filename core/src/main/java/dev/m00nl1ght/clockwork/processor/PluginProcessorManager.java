package dev.m00nl1ght.clockwork.processor;

import dev.m00nl1ght.clockwork.core.*;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Supplier;

public class PluginProcessorManager {

    private final Map<String, PluginProcessor> loadedProcessors = new HashMap<>();
    private final MethodHandles.Lookup reflectiveAccess;

    public PluginProcessorManager(MethodHandles.Lookup reflectiveAccess) {
        this.reflectiveAccess = checkAccess(reflectiveAccess);
        ServiceLoader.load(PluginProcessor.class).forEach(p -> loadedProcessors.put(p.getName(), p));
    }

    private static MethodHandles.Lookup checkAccess(MethodHandles.Lookup reflectiveAccess) {
        if (reflectiveAccess.lookupClass() != ClockworkCore.class || !reflectiveAccess.hasPrivateAccess()) {
            throw new IllegalArgumentException("Invalid reflective access object");
        } else {
            return reflectiveAccess;
        }
    }

    public void apply(PluginContainer object, Collection<String> processors) {
        for (var name : processors) {
            final var prc = loadedProcessors.get(name);
            if (prc == null) throw PluginLoadingException.missingProcessor("plugin", object.getId(), name);
            try {
                prc.process(object);
            } catch (Throwable t) {
                throw PluginLoadingException.inProcessor("plugin", object.getId(), name, t);
            }
        }
    }

    public void apply(ComponentType<?, ?> object, Collection<String> processors) {
        final var privateAccess = new ReflAccessSupplier(object.getComponentClass(), reflectiveAccess);
        for (var name : processors) {
            final var prc = loadedProcessors.get(name);
            if (prc == null) throw PluginLoadingException.missingProcessor("component", object.getId(), name);
            try {
                prc.process(object, privateAccess);
            } catch (Throwable t) {
                throw PluginLoadingException.inProcessor("component", object.getId(), name, t);
            }
        }
    }

    public void apply(TargetType<?> object, Collection<String> processors) {
        final var privateAccess = new ReflAccessSupplier(object.getTargetClass(), reflectiveAccess);
        for (var name : processors) {
            final var prc = loadedProcessors.get(name);
            if (prc == null) throw PluginLoadingException.missingProcessor("target", object.getId(), name);
            try {
                prc.process(object, privateAccess);
            } catch (Throwable t) {
                throw PluginLoadingException.inProcessor("target", object.getId(), name, t);
            }
        }
    }

    private static class ReflAccessSupplier implements Supplier<MethodHandles.Lookup> {

        private MethodHandles.Lookup cached = null;
        private final Class<?> target;
        private final MethodHandles.Lookup parent;

        private ReflAccessSupplier(Class<?> target, MethodHandles.Lookup parent) {
            this.target = target;
            this.parent = parent;
        }

        @Override
        public MethodHandles.Lookup get() {
            return cached != null ? cached : create();
        }

        private MethodHandles.Lookup create() {
            try {
                parent.lookupClass().getModule().addReads(target.getModule());
                return cached = MethodHandles.privateLookupIn(target, parent);
            } catch (Exception e) {
                throw new RuntimeException("Failed to get reflective access on " + target.getName(), e);
            }
        }

    }

}
