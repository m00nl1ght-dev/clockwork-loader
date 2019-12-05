package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ComponentContainer;
import dev.m00nl1ght.clockwork.core.TargetType;

public class TestTarget_B extends TestTarget_A {

    public static final TargetType<TestTarget_B> TARGET_TYPE = TestLauncher.getTargetType(TestTarget_B.class);

    @Override
    protected ComponentContainer<?> buildContainer() {
        return new ComponentContainer<>(TARGET_TYPE, this);
    }

}
