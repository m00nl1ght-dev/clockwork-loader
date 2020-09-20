package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.interfaces.InterfaceType;
import dev.m00nl1ght.clockwork.interfaces.InterfaceTypeImpl;

public interface TestInterface {

    InterfaceType<TestInterface, TestTarget_A> TYPE =
            new InterfaceTypeImpl<>(TestInterface.class, TestTarget_A.TARGET_TYPE, true);

    void tick();

}
