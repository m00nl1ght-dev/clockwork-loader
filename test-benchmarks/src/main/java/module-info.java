module dev.m00nl1ght.clockwork.benchmarks {

    // needed so that reflective access works in boot layer
    opens dev.m00nl1ght.clockwork.benchmarks to dev.m00nl1ght.clockwork;

    requires dev.m00nl1ght.clockwork;
    requires dev.m00nl1ght.clockwork.extension.annotations;
    requires org.apache.logging.log4j;

}