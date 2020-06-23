package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.core.ComponentDescriptor;
import dev.m00nl1ght.clockwork.core.PluginDefinition;

import java.util.Collection;

public interface PluginLocator {

    Collection<PluginDefinition> findAll();

    Collection<PluginDefinition> find(ComponentDescriptor target);

    String getName();

}
