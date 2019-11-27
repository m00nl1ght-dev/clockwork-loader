package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ComponentContainer;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.holder.StaticHolder;

public class TestTarget_A implements ComponentTarget {

    @StaticHolder
    public static final TargetType<TestTarget_A> TARGET_TYPE = null;

    protected final ComponentContainer<? extends TestTarget_A> container;

    protected TestTarget_A() {
        this.container = buildContainer();
        this.container.initComponents();
    }

    protected ComponentContainer<? extends TestTarget_A> buildContainer() {
        return new ComponentContainer<>(TARGET_TYPE, this);
    }

    @Override
    public TargetType<? extends TestTarget_A> getTargetType() {
        return container.getTargetType();
    }

    @Override
    public Object getComponent(int internalID) {
        return container.getComponent(internalID);
    }

}
