module dev.m00nl1ght.clockwork {
    uses dev.m00nl1ght.clockwork.processor.PluginProcessor;
    provides dev.m00nl1ght.clockwork.processor.PluginProcessor with dev.m00nl1ght.clockwork.event.EventAnnotationProcessor;

    uses dev.m00nl1ght.clockwork.event.EventTypeFactory;
    provides dev.m00nl1ght.clockwork.event.EventTypeFactory with dev.m00nl1ght.clockwork.event.types.CancellableEventType.Factory;

    exports dev.m00nl1ght.clockwork.core;
    exports dev.m00nl1ght.clockwork.locator;
    exports dev.m00nl1ght.clockwork.processor;
    exports dev.m00nl1ght.clockwork.event;
    exports dev.m00nl1ght.clockwork.event.types;
    exports dev.m00nl1ght.clockwork.security;
    exports dev.m00nl1ght.clockwork.security.permissions;

    requires org.apache.logging.log4j;
    requires core;
    requires semver4j;
}