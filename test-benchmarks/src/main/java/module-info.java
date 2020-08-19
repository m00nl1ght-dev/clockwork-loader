module dev.m00nl1ght.clockwork.benchmarks {

    // needed so that event annotation processor can use reflective access in boot layer
    opens dev.m00nl1ght.clockwork.benchmarks to dev.m00nl1ght.clockwork;

    requires dev.m00nl1ght.clockwork;
    requires dev.m00nl1ght.clockwork.extension.annotations;
    requires org.apache.logging.log4j;

}