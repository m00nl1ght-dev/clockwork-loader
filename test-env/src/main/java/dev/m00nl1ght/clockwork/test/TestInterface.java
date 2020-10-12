package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.interfaces.ComponentInterface;
import dev.m00nl1ght.clockwork.interfaces.impl.ComponentInterfaceImpl;

public interface TestInterface {

    ComponentInterface<TestInterface, TestTarget_A> TYPE =
            new ComponentInterfaceImpl<>(TestInterface.class, TestTarget_A.TARGET_TYPE);

    void tick();

}
