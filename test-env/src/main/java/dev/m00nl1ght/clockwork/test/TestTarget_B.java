package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ComponentContainer;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.holder.StaticHolder;

public class TestTarget_B extends TestTarget_A {

    @StaticHolder
    public static final TargetType<TestTarget_B> TARGET_TYPE = null;

    @Override
    protected ComponentContainer<? extends TestTarget_A> buildContainer() {
        return new ComponentContainer<>(TARGET_TYPE, this);
    }

}
