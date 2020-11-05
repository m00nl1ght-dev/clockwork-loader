package dev.m00nl1ght.clockwork.test.env;

import dev.m00nl1ght.clockwork.core.AbstractComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;

public class TestTarget_A extends AbstractComponentTarget<TestTarget_A> {

    private final TestTarget_C testTargetC;

    public TestTarget_A(TargetType<? extends TestTarget_A> targetType, TestTarget_C testTargetC) {
        super(targetType);
        this.testTargetC = testTargetC;
        componentContainer.initComponents();
    }

    public TestTarget_C getTestTargetC() {
        return testTargetC;
    }

}
