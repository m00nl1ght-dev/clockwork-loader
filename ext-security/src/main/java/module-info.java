module dev.m00nl1ght.clockwork.extension.security {

    provides java.security.Provider with dev.m00nl1ght.clockwork.extension.security.internal.ProviderImpl;

    exports dev.m00nl1ght.clockwork.extension.security;
    exports dev.m00nl1ght.clockwork.extension.security.permissions;

    // needed so that reflective access works in boot layer
    opens dev.m00nl1ght.clockwork.extension.security to dev.m00nl1ght.clockwork;

    requires dev.m00nl1ght.clockwork;
    requires org.apache.logging.log4j;

    requires static org.jetbrains.annotations;

}