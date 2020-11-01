module dev.m00nl1ght.clockwork.test.env {

    exports dev.m00nl1ght.clockwork.test.env;
    exports dev.m00nl1ght.clockwork.test.env.events;
    exports dev.m00nl1ght.clockwork.test.env.security;

    opens dev.m00nl1ght.clockwork.test.env to dev.m00nl1ght.clockwork;

    requires dev.m00nl1ght.clockwork;
    requires dev.m00nl1ght.clockwork.extension.security;
    requires org.apache.logging.log4j;

}