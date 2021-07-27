module dev.m00nl1ght.clockwork.extension.mixin {

    exports dev.m00nl1ght.clockwork.extension.mixin;

    // needed so that reflective access works in boot layer
    opens dev.m00nl1ght.clockwork.extension.mixin to dev.m00nl1ght.clockwork;

    requires dev.m00nl1ght.clockwork;

    requires static org.jetbrains.annotations;

}