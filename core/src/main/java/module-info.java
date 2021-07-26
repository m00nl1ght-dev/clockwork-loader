module dev.m00nl1ght.clockwork {

    exports dev.m00nl1ght.clockwork.core;
    exports dev.m00nl1ght.clockwork.descriptor;
    exports dev.m00nl1ght.clockwork.loader.fnder;
    exports dev.m00nl1ght.clockwork.loader.reader;
    exports dev.m00nl1ght.clockwork.loader.verifier;
    exports dev.m00nl1ght.clockwork.event;
    exports dev.m00nl1ght.clockwork.event.impl;
    exports dev.m00nl1ght.clockwork.event.impl.listener;
    exports dev.m00nl1ght.clockwork.version;
    exports dev.m00nl1ght.clockwork.interfaces;
    exports dev.m00nl1ght.clockwork.interfaces.impl;
    exports dev.m00nl1ght.clockwork.debug;
    exports dev.m00nl1ght.clockwork.debug.profiler;
    exports dev.m00nl1ght.clockwork.util;
    exports dev.m00nl1ght.clockwork.config;
    exports dev.m00nl1ght.clockwork.event.impl.event;
    exports dev.m00nl1ght.clockwork.event.impl.bus;
    exports dev.m00nl1ght.clockwork.event.impl.forwarding;
    exports dev.m00nl1ght.clockwork.logger;
    exports dev.m00nl1ght.clockwork.loader.jigsaw;
    exports dev.m00nl1ght.clockwork.loader;
    exports dev.m00nl1ght.clockwork.loader.processor;
    exports dev.m00nl1ght.clockwork.loader.fnder.impl;
    exports dev.m00nl1ght.clockwork.loader.jigsaw.impl;
    exports dev.m00nl1ght.clockwork.loader.reader.impl;
    exports dev.m00nl1ght.clockwork.debug.profiler.impl;
    exports dev.m00nl1ght.clockwork.config.impl;
    exports dev.m00nl1ght.clockwork.component;
    exports dev.m00nl1ght.clockwork.component.impl;
    exports dev.m00nl1ght.clockwork.loader.classloading;

    requires static org.slf4j;
    requires static org.apache.logging.log4j;
    requires static org.jetbrains.annotations;

}