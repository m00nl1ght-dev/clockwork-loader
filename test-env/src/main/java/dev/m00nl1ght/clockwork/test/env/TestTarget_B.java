package dev.m00nl1ght.clockwork.test.env;

import dev.m00nl1ght.clockwork.core.TargetType;

public class TestTarget_B extends TestTarget_A {

    public TestTarget_B(TargetType<? extends TestTarget_B> targetType) {
        super(targetType);
    }

}
