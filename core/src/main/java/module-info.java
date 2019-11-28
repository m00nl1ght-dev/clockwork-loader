module dev.m00nl1ght.clockwork {
    uses dev.m00nl1ght.clockwork.processor.PluginProcessor;
    provides dev.m00nl1ght.clockwork.processor.PluginProcessor with dev.m00nl1ght.clockwork.event.EventAnnotationProcessor;

    exports dev.m00nl1ght.clockwork.core;
    exports dev.m00nl1ght.clockwork.locator;
    exports dev.m00nl1ght.clockwork.processor;
    exports dev.m00nl1ght.clockwork.event;
    exports dev.m00nl1ght.clockwork.event.filter;
    exports dev.m00nl1ght.clockwork.holder;
    exports dev.m00nl1ght.clockwork.debug;
    exports dev.m00nl1ght.clockwork.security;
    exports dev.m00nl1ght.clockwork.security.permissions;

    requires org.apache.logging.log4j;
    requires core;
    requires semver4j;
    requires it.unimi.dsi.fastutil;
}