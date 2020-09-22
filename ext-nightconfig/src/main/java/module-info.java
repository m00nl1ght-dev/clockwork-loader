module dev.m00nl1ght.clockwork.extension.nightconfig {

    exports dev.m00nl1ght.clockwork.extension.nightconfig;

    // needed so that reflective access works in boot layer
    opens dev.m00nl1ght.clockwork.extension.nightconfig to dev.m00nl1ght.clockwork;

    requires dev.m00nl1ght.clockwork;
    requires org.apache.logging.log4j;
    requires com.electronwill.nightconfig.core;
    requires com.electronwill.nightconfig.toml;

}