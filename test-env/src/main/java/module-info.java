module dev.m00nl1ght.clockwork.test.env {

    exports dev.m00nl1ght.clockwork.test.env;
    exports dev.m00nl1ght.clockwork.test.env.events;

    opens dev.m00nl1ght.clockwork.test.env to dev.m00nl1ght.clockwork;

    requires dev.m00nl1ght.clockwork;

    requires static org.jetbrains.annotations;

}
