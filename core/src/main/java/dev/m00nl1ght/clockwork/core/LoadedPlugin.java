package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Preconditions;

public class LoadedPlugin {

    private final PluginDescriptor descriptor;
    private final ClockworkCore clockworkCore;
    private final Module mainModule;

    LoadedPlugin(PluginDescriptor descriptor, ClockworkCore clockworkCore, Module mainModule) {
        this.descriptor = Preconditions.notNull(descriptor, "descriptor");
        this.clockworkCore = Preconditions.notNull(clockworkCore, "clockworkCore");
        this.mainModule = Preconditions.notNull(mainModule, "mainModule");
    }

    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    public String getId() {
        return descriptor.getId();
    }

    public ClockworkCore getClockworkCore() {
        return clockworkCore;
    }

    public Module getMainModule() {
        return mainModule;
    }

}
