package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.interfaces.ComponentInterface;
import dev.m00nl1ght.clockwork.test.env.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComponentInterfaceTest extends ClockworkTest {

    protected TargetType<TestTarget_A> targetTypeA;
    protected TargetType<TestTarget_B> targetTypeB;

    @Override
    protected TestEnvironment buildEnvironment(ClockworkCore core) {
        final var env = super.buildEnvironment(core);
        targetTypeA = core.getTargetTypeOrThrow(TestTarget_A.class);
        targetTypeB = core.getTargetTypeOrThrow(TestTarget_B.class);
        return env;
    }

    @Test
    public void applyInterfaceToComponentA() {
        final var testContext = new TestContext();
        final var compInterface = ComponentInterface.of(TestInterface.class, targetTypeA);
        final var target = new TestTarget_A(targetTypeA, null);
        compInterface.apply(target, i -> i.applyTestInterface(testContext));
        assertTrue(testContext.isMarkerPresent("TestComponent_A#applyTestInterface"));
    }

    @Test
    public void applyInterfaceToComponentB() {
        final var testContext = new TestContext();
        final var compInterface = ComponentInterface.of(TestInterface.class, targetTypeA);
        final var target = new TestTarget_B(targetTypeB, null);
        compInterface.apply(target, i -> i.applyTestInterface(testContext));
        assertTrue(testContext.isMarkerPresent("TestComponent_A#applyTestInterface"));
        assertTrue(testContext.isMarkerPresent("TestComponent_B#applyTestInterface"));
    }

}
