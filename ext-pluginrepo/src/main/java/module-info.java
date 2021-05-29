module dev.m00nl1ght.clockwork.extension.jarinjar {

    exports dev.m00nl1ght.clockwork.extension.pluginrepo;

    // needed so that reflective access works in boot layer
    opens dev.m00nl1ght.clockwork.extension.pluginrepo to dev.m00nl1ght.clockwork;

    requires dev.m00nl1ght.clockwork;

    requires static org.jetbrains.annotations;

}