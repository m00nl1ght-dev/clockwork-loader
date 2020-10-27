module dev.m00nl1ght.clockwork.test {

    exports dev.m00nl1ght.clockwork.test;
    exports dev.m00nl1ght.clockwork.test.event;

    // needed so that event annotation processor can use reflective access in boot layer
    opens dev.m00nl1ght.clockwork.test to dev.m00nl1ght.clockwork;

    requires dev.m00nl1ght.clockwork;
    requires dev.m00nl1ght.clockwork.extension.annotations;
    requires dev.m00nl1ght.clockwork.extension.nightconfig;
    requires dev.m00nl1ght.clockwork.extension.security;
    requires org.apache.logging.log4j;

}