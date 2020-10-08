package dev.m00nl1ght.clockwork.fnder;

import dev.m00nl1ght.clockwork.core.LoadingContext;
import dev.m00nl1ght.clockwork.descriptor.DependencyDescriptor;
import dev.m00nl1ght.clockwork.descriptor.PluginReference;

import java.lang.module.ModuleFinder;
import java.util.Collection;

public interface PluginFinder {

    Collection<PluginReference> findAll(LoadingContext context);

    Collection<PluginReference> find(LoadingContext context, DependencyDescriptor target);

    ModuleFinder getModuleFinder(LoadingContext context);

}
