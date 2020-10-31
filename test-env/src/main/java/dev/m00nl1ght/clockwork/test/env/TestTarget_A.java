package dev.m00nl1ght.clockwork.test.env;

import dev.m00nl1ght.clockwork.core.AbstractComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;

public class TestTarget_A extends AbstractComponentTarget<TestTarget_A> {

    public TestTarget_A(TargetType<? extends TestTarget_A> targetType) {
        super(targetType);
    }

}
