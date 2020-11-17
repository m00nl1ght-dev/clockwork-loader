module dev.m00nl1ght.clockwork {

    exports dev.m00nl1ght.clockwork.core;
    exports dev.m00nl1ght.clockwork.descriptor;
    exports dev.m00nl1ght.clockwork.container;
    exports dev.m00nl1ght.clockwork.fnder;
    exports dev.m00nl1ght.clockwork.reader;
    exports dev.m00nl1ght.clockwork.verifier;
    exports dev.m00nl1ght.clockwork.events;
    exports dev.m00nl1ght.clockwork.events.impl;
    exports dev.m00nl1ght.clockwork.events.listener;
    exports dev.m00nl1ght.clockwork.version;
    exports dev.m00nl1ght.clockwork.interfaces;
    exports dev.m00nl1ght.clockwork.interfaces.impl;
    exports dev.m00nl1ght.clockwork.debug;
    exports dev.m00nl1ght.clockwork.debug.profiler;
    exports dev.m00nl1ght.clockwork.util;
    exports dev.m00nl1ght.clockwork.config;

    requires org.apache.logging.log4j;

    requires static org.jetbrains.annotations;

}