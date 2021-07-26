package dev.m00nl1ght.clockwork.loader.classloading;

public interface ClassTransformer {

    byte[] transform(String className, byte[] classBytes);

    int priority();

}
