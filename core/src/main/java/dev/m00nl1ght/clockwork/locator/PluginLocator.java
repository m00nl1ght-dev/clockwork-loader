package dev.m00nl1ght.clockwork.locator;

import dev.m00nl1ght.clockwork.core.PluginDefinition;

import java.util.Collection;
import java.util.Optional;

public interface PluginLocator {

    Collection<PluginDefinition> findAll();

    Optional<PluginDefinition> find(String plugin_id);

    String getName();

}
