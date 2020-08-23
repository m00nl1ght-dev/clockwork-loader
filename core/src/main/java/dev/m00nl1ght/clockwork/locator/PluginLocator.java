package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.core.DependencyDescriptor;
import dev.m00nl1ght.clockwork.core.PluginReference;

import java.util.Collection;

public interface PluginLocator {

    Collection<PluginReference> findAll();

    Collection<PluginReference> find(DependencyDescriptor target);

    String getName();

}
