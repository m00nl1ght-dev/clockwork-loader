module dev.m00nl1ght.clockwork {
    uses dev.m00nl1ght.clockwork.processor.PluginProcessor;
    uses dev.m00nl1ght.clockwork.event.EventTypeFactory;
    provides dev.m00nl1ght.clockwork.processor.PluginProcessor with dev.m00nl1ght.clockwork.event.EventAnnotationProcessor;

    exports dev.m00nl1ght.clockwork.core;
    exports dev.m00nl1ght.clockwork.core.plugin;
    exports dev.m00nl1ght.clockwork.locator;
    exports dev.m00nl1ght.clockwork.processor;
    exports dev.m00nl1ght.clockwork.event;
    exports dev.m00nl1ght.clockwork.security;

    requires org.apache.logging.log4j;
    requires core;
    requires semver4j;
}