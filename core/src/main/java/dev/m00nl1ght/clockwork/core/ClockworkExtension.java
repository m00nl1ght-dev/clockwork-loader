package dev.m00nl1ght.clockwork.core;

import org.jetbrains.annotations.NotNull;

public interface ClockworkExtension {

    void registerFeatures(@NotNull ExtensionContext extensionContext);

}
