module dev.m00nl1ght.clockwork {
    exports dev.m00nl1ght.clockwork.core;
    exports dev.m00nl1ght.clockwork.core.plugin;
    exports dev.m00nl1ght.clockwork.locator;
    exports dev.m00nl1ght.clockwork.event;

    requires org.apache.logging.log4j;
    requires core;
    requires semver4j;
}