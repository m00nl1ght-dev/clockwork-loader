package dev.m00nl1ght.clockwork.classloading;

public class PluginClassloader extends ClassLoader {

    private final String pluginId;

    public PluginClassloader(String pluginId, ClassLoader parent) {
        super("PluginClassloader[" + pluginId + "]", parent);
        this.pluginId = pluginId;
    }

}
