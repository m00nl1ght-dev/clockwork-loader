package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.FormatUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Objects;

public class PluginProcessorContext {

    private final LoadedPlugin plugin;
    private final Lookup rootLookup;

    PluginProcessorContext(@NotNull LoadedPlugin plugin, @NotNull Lookup rootLookup) {
        this.plugin = Objects.requireNonNull(plugin);
        this.rootLookup = Objects.requireNonNull(rootLookup);
        if (!rootLookup.hasFullPrivilegeAccess()) throw new IllegalArgumentException();
        if (rootLookup.lookupClass() != ClockworkLoader.class) throw new IllegalArgumentException();
    }

    public @NotNull Lookup getReflectiveAccess(@NotNull Class<?> targetClass, @NotNull AccessLevel requiredLevel) throws IllegalAccessException {
        Objects.requireNonNull(targetClass);
        Objects.requireNonNull(requiredLevel);

        final var targetModule = targetClass.getModule();
        if (targetModule != plugin.getMainModule())
            throw FormatUtil.illAccExc("Module [] is not accessible from context of []", targetModule, plugin);

        final var lookup = fetchLookup(targetClass);

        if (requiredLevel == AccessLevel.FULL && !lookup.hasFullPrivilegeAccess())
            throw FormatUtil.rtExc("This plugin processor requires full reflective access on plugin [], " +
                    "but its module does not provide such access.", plugin);

        if (requiredLevel == AccessLevel.PRIVATE && (lookup.lookupModes() & Lookup.PRIVATE) == 0)
            throw FormatUtil.rtExc("This plugin processor requires private reflective access on plugin [], " +
                    "but its module does not provide such access.", plugin);

        return lookup;
    }

    private @NotNull Lookup fetchLookup(@NotNull Class<?> targetClass) throws IllegalAccessException {

        // First, attempt to get the lookup directly from the plugin's main component.
        if (plugin.getClockworkCore().getState() == ClockworkCore.State.INITIALISED) {
            final var mainComponent = plugin.getMainComponent().get(plugin.getClockworkCore());
            if (mainComponent != null) {
                final var providedLookup = mainComponent.getReflectiveAccess();
                if (providedLookup != null) {
                    if (providedLookup.hasFullPrivilegeAccess()) {
                        return MethodHandles.privateLookupIn(targetClass, providedLookup);
                    } else {
                        return providedLookup.in(targetClass);
                    }
                }
            }
        }

        // If the main component did not provide one, try to use the root lookup to obtain it.
        if (plugin.getMainModule().isOpen(targetClass.getPackageName(), ClockworkLoader.class.getModule())) {
            ClockworkLoader.class.getModule().addReads(plugin.getMainModule());
            return MethodHandles.privateLookupIn(targetClass, rootLookup);
        }

        // If none of these options worked, fallback to public lookup.
        return rootLookup.in(targetClass);

    }

    public <C extends Component<T>, T extends ComponentTarget>
    @NotNull ComponentFactory<T, C> getComponentFactory(@NotNull RegisteredComponentType<C, T> componentType) {
        Objects.requireNonNull(componentType);
        this.checkPluginAccess(componentType.getPlugin());
        return componentType.getFactoryInternal();
    }

    public <C extends Component<T>, T extends ComponentTarget>
    void setComponentFactory(@NotNull RegisteredComponentType<C, T> componentType, @NotNull ComponentFactory<T, C> factory) {
        Objects.requireNonNull(componentType);
        Objects.requireNonNull(factory);
        this.checkPluginAccess(componentType.getPlugin());
        componentType.setFactoryInternal(factory);
    }

    private void checkPluginAccess(@NotNull LoadedPlugin other) {
        if (other != plugin)
            throw FormatUtil.illArgExc("Context of [] can not access plugin []", plugin, other);
    }

    public @NotNull LoadedPlugin getPlugin() {
        return plugin;
    }

    @Override
    public String toString() {
        return plugin.toString();
    }

    public enum AccessLevel {
        PUBLIC, PRIVATE, FULL
    }

}
