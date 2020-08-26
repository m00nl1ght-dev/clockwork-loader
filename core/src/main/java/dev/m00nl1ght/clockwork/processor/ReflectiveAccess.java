package dev.m00nl1ght.clockwork.processor;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ClockworkLoader;
import dev.m00nl1ght.clockwork.core.LoadedPlugin;
import dev.m00nl1ght.clockwork.util.Preconditions;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

public class ReflectiveAccess {

    private final LoadedPlugin plugin;
    private final Lookup rootLookup;

    public ReflectiveAccess(LoadedPlugin plugin, Lookup rootLookup) {
        this.plugin = Preconditions.notNull(plugin, "plugin");
        this.rootLookup = Preconditions.notNull(rootLookup, "rootLookup");
        plugin.getClockworkCore().getState().require(ClockworkCore.State.PROCESSING);
        if (!rootLookup.hasFullPrivilegeAccess()) throw new IllegalArgumentException();
        if (rootLookup.lookupClass() != ClockworkLoader.class) throw new IllegalArgumentException();
    }

    public Lookup lookup(Class<?> targetClass) throws IllegalAccessException {
        final var targetModule = targetClass.getModule();
        if (targetModule != plugin.getMainModule())
            throw new IllegalAccessException();
        final var lookup = MethodHandles.privateLookupIn(targetClass, rootLookup);
        return lookup; // TODO check for potential security exploits
    }

}
