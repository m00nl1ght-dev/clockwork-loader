package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ComponentTargetType;

public class SubclassTestComponentTarget extends TestComponentTarget {

    public <T extends SubclassTestComponentTarget> SubclassTestComponentTarget(ComponentTargetType<T> targetType) {
        super(targetType);
    }

}
