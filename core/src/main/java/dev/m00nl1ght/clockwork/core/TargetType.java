package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.descriptor.TargetDescriptor;
import dev.m00nl1ght.clockwork.util.CollectionUtil;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class TargetType<T extends ComponentTarget> {

    private final LoadedPlugin plugin;
    private final TargetDescriptor descriptor;
    private final Class<T> targetClass;
    private final TargetType<? super T> root;
    private final TargetType<? super T> parent;

    private final List<ComponentType<?, T>> ownComponents = new LinkedList<>();
    private final List<ComponentType<?, ? super T>> allComponents;

    private final List<TargetType<? extends T>> directSubtargets = new LinkedList<>();
    private final List<TargetType<?>> allSubtargets;

    private int subtargetIdxFirst = -1, subtargetIdxLast = -1;

    TargetType(LoadedPlugin plugin, TargetType<? super T> parent, TargetDescriptor descriptor, Class<T> targetClass) {
        this.plugin = plugin;
        this.descriptor = descriptor;
        this.targetClass = targetClass;
        this.parent = parent;
        if (parent == null) {
            this.root = this;
            this.allComponents = Collections.unmodifiableList(ownComponents);
            this.allSubtargets = new LinkedList<>();
        } else {
            this.root = parent.root;
            this.allComponents = CollectionUtil.compoundList(parent.allComponents, ownComponents);
            this.parent.directSubtargets.add(this);
            this.allSubtargets = parent.allSubtargets;
        }
    }

    public LoadedPlugin getPlugin() {
        return plugin;
    }

    public ClockworkCore getClockworkCore() {
        return plugin.getClockworkCore();
    }

    public TargetDescriptor getDescriptor() {
        return descriptor;
    }

    public String getId() {
        return descriptor.getId();
    }

    public Class<T> getTargetClass() {
        return targetClass;
    }

    public TargetType<? super T> getParent() {
        return parent;
    }

    public TargetType<? super T> getRoot() {
        return root;
    }

    public boolean isEquivalentTo(TargetType<?> other) {
        return other == this || (getParent() != null && getParent().isEquivalentTo(other));
    }

    @SuppressWarnings("unchecked")
    public List<TargetType<? extends T>> getAllSubtargets() {
        getClockworkCore().getState().requireOrAfter(ClockworkCore.State.POPULATED);
        return IntStream.rangeClosed(subtargetIdxFirst, subtargetIdxLast)
                .mapToObj(i -> (TargetType<? extends T>) allSubtargets.get(i))
                .collect(Collectors.toUnmodifiableList());
    }

    public List<TargetType<? extends T>> getDirectSubtargets() {
        return Collections.unmodifiableList(directSubtargets);
    }

    public int getSubtargetIdxFirst() {
        return subtargetIdxFirst;
    }

    public int getSubtargetIdxLast() {
        return subtargetIdxLast;
    }

    public List<ComponentType<?, T>> getOwnComponentTypes() {
        getClockworkCore().getState().requireOrAfter(ClockworkCore.State.POPULATED);
        return Collections.unmodifiableList(ownComponents);
    }

    public List<ComponentType<?, ? super T>> getAllComponentTypes() {
        getClockworkCore().getState().requireOrAfter(ClockworkCore.State.POPULATED);
        return allComponents;
    }

    @Override
    public String toString() {
        return descriptor.toString();
    }

    // ### Internal ###

    <C> void addComponent(ComponentType<?, T> componentType) {
        getClockworkCore().getState().require(ClockworkCore.State.POPULATING);
        if (componentType.getTargetType() != this) throw new IllegalArgumentException();
        ownComponents.add(componentType);
    }

    void init() {
        getClockworkCore().getState().require(ClockworkCore.State.POPULATING);
        if (parent == null) {
            for (int i = 0; i < ownComponents.size(); i++) ownComponents.get(i).setInternalIdx(i);
            this.populateSubtargets();
        } else {
            final var offset = parent.allComponents.size();
            for (int i = 0; i < ownComponents.size(); i++) ownComponents.get(i).setInternalIdx(i + offset);
        }
    }

    private void populateSubtargets() {
        this.subtargetIdxFirst = this.allSubtargets.size();
        this.allSubtargets.add(this);
        for (final var sub : this.directSubtargets) sub.populateSubtargets();
        this.subtargetIdxLast = this.allSubtargets.size() - 1;
    }

}
