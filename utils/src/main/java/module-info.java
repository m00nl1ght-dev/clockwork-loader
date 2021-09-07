module dev.m00nl1ght.clockwork.utils {

    exports dev.m00nl1ght.clockwork.utils.version;
    exports dev.m00nl1ght.clockwork.utils.debug;
    exports dev.m00nl1ght.clockwork.utils.debug.profiler;
    exports dev.m00nl1ght.clockwork.utils.debug.profiler.impl;
    exports dev.m00nl1ght.clockwork.utils.reflect;
    exports dev.m00nl1ght.clockwork.utils.config;
    exports dev.m00nl1ght.clockwork.utils.config.impl;
    exports dev.m00nl1ght.clockwork.utils.logger;
    exports dev.m00nl1ght.clockwork.utils.logger.impl;
    exports dev.m00nl1ght.clockwork.utils.collections;

    requires static org.slf4j;
    requires static org.apache.logging.log4j;
    requires static org.jetbrains.annotations;

}