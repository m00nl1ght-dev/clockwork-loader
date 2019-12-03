package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.FunctionalSubtarget;
import dev.m00nl1ght.clockwork.subtarget.SubtargetType;

@SubtargetType
public interface TestSubtarget {

    FunctionalSubtarget<TestTarget_A, TestSubtarget> TYPE = TestTarget_A.TARGET_TYPE.getSubtarget(TestSubtarget.class);

    void tick();

}
