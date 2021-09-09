module dev.m00nl1ght.clockwork {

    exports dev.m00nl1ght.clockwork.core;
    exports dev.m00nl1ght.clockwork.descriptor;
    exports dev.m00nl1ght.clockwork.loader.fnder;
    exports dev.m00nl1ght.clockwork.loader.reader;
    exports dev.m00nl1ght.clockwork.event;
    exports dev.m00nl1ght.clockwork.event.impl;
    exports dev.m00nl1ght.clockwork.event.impl.listener;
    exports dev.m00nl1ght.clockwork.interfaces;
    exports dev.m00nl1ght.clockwork.interfaces.impl;
    exports dev.m00nl1ght.clockwork.event.impl.event;
    exports dev.m00nl1ght.clockwork.event.impl.bus;
    exports dev.m00nl1ght.clockwork.event.impl.forwarding;
    exports dev.m00nl1ght.clockwork.event.debug;
    exports dev.m00nl1ght.clockwork.loader.jigsaw;
    exports dev.m00nl1ght.clockwork.loader;
    exports dev.m00nl1ght.clockwork.loader.fnder.impl;
    exports dev.m00nl1ght.clockwork.loader.jigsaw.impl;
    exports dev.m00nl1ght.clockwork.loader.reader.impl;
    exports dev.m00nl1ght.clockwork.component;
    exports dev.m00nl1ght.clockwork.component.impl;
    exports dev.m00nl1ght.clockwork.loader.classloading;
    exports dev.m00nl1ght.clockwork.utils.reflect;

    requires transitive dev.m00nl1ght.clockwork.utils.config;
    requires transitive dev.m00nl1ght.clockwork.utils.logger;
    requires transitive dev.m00nl1ght.clockwork.utils.profiler;
    requires transitive dev.m00nl1ght.clockwork.utils.version;

    requires static org.jetbrains.annotations;

}