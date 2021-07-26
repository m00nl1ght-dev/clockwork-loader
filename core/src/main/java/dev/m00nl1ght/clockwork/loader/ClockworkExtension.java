package dev.m00nl1ght.clockwork.loader;

import org.jetbrains.annotations.NotNull;

public interface ClockworkExtension {

    void registerFeatures(@NotNull ExtensionContext extensionContext);

}
