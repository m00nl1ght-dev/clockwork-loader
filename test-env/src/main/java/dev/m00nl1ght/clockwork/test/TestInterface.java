package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.interfaces.ComponentInterfaceType;

public interface TestInterface {

    ComponentInterfaceType<TestInterface, TestTarget_A> TYPE = null; // TODO

    void tick();

}
