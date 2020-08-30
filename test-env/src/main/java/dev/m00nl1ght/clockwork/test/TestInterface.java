package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.interfaces.ComponentInterfaceType;
import dev.m00nl1ght.clockwork.interfaces.SimpleComponentInterface;

public interface TestInterface {

    ComponentInterfaceType<TestInterface, TestTarget_A> TYPE = new SimpleComponentInterface<>(TestInterface.class, TestTarget_A.class);

    void tick();

}
