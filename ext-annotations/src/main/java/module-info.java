module dev.m00nl1ght.clockwork.extension.annotations {

    exports dev.m00nl1ght.clockwork.extension.annotations;

    // needed so that reflective access works in boot layer
    opens dev.m00nl1ght.clockwork.extension.annotations to dev.m00nl1ght.clockwork;

    requires dev.m00nl1ght.clockwork;
    requires org.apache.logging.log4j;

    requires static org.jetbrains.annotations;

}