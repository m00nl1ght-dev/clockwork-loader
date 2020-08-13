package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.interfaces.ComponentInterfaceType;
import dev.m00nl1ght.clockwork.interfaces.ComponentInterface;

@ComponentInterface
public interface TestInterface {

    ComponentInterfaceType<TestInterface, TestTarget_A> TYPE = null; // TODO

    void tick();

}
