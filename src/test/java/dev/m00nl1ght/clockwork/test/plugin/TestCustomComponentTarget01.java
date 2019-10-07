package dev.m00nl1ght.clockwork.test.plugin;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;

import java.util.Optional;

public class TestCustomComponentTarget01 implements ComponentTarget<TestCustomComponentTarget01> {

    @Override
    public <C> Optional<C> getComponent(ComponentType<C, TestCustomComponentTarget01> component) {
        return Optional.empty();
    }

}
