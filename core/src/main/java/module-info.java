module dev.m00nl1ght.clockwork {

    uses dev.m00nl1ght.clockwork.processor.PluginProcessor;

    exports dev.m00nl1ght.clockwork.core;
    exports dev.m00nl1ght.clockwork.locator;
    exports dev.m00nl1ght.clockwork.processor;
    exports dev.m00nl1ght.clockwork.events;
    exports dev.m00nl1ght.clockwork.version;
    exports dev.m00nl1ght.clockwork.interfaces;
    exports dev.m00nl1ght.clockwork.debug;
    exports dev.m00nl1ght.clockwork.debug.profiler;
    exports dev.m00nl1ght.clockwork.debug.profiler.core;
    exports dev.m00nl1ght.clockwork.debug.profiler.generic;
    exports dev.m00nl1ght.clockwork.security;
    exports dev.m00nl1ght.clockwork.security.permissions;
    exports dev.m00nl1ght.clockwork.util;

    requires org.apache.logging.log4j;
    requires com.electronwill.nightconfig.core;
    requires com.electronwill.nightconfig.toml;

}