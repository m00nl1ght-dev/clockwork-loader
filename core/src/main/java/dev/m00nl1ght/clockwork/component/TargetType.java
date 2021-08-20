package dev.m00nl1ght.clockwork.component;

import dev.m00nl1ght.clockwork.loader.PluginLoadingException;
import dev.m00nl1ght.clockwork.util.FormatUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TargetType<T extends ComponentTarget> {

    protected final Class<T> targetClass;
    protected final TargetType<? super T> root;
    protected final TargetType<? super T> parent;
    protected final ComponentType.Identity<T> identityComponentType;

    private final List<TargetType<? extends T>> directSubtargets = new LinkedList<>();
    private final List<TargetType<?>> allSubtargets;

    private final Set<ComponentType<?, T>> ownComponentTypes = new LinkedHashSet<>();
    private List<ComponentType<?, ? super T>> componentTypes;
    private int subtargetIdxFirst = -1, subtargetIdxLast = -1;

    public TargetType(TargetType<? super T> parent, Class<T> targetClass) {
        this.targetClass = Objects.requireNonNull(targetClass);
        this.identityComponentType = new ComponentType.Identity<>(this);
        this.parent = parent;
        if (this.parent == null) {
            this.root = this;
            this.allSubtargets = new LinkedList<>();
        } else {
            synchronized (this.parent) {
                this.root = this.parent.root;
                if (!this.parent.targetClass.isAssignableFrom(targetClass))
                    throw FormatUtil.illArgExc("Incompatible parent target [] for []", parent, this);
                if (this.parent.isLocked())
                    throw FormatUtil.illStateExc("Parent TargetType [] is locked", this.parent);
                this.parent.directSubtargets.add(this);
                this.allSubtargets = this.parent.allSubtargets;
            }
        }
    }

    public static <T extends ComponentTarget> TargetType<T> empty(Class<T> targetClass) {
        final var target = new TargetType<>(null, targetClass);
        target.lock();
        return target;
    }

    public final Class<T> getTargetClass() {
        return targetClass;
    }

    public final TargetType<? super T> getParent() {
        return parent;
    }

    public final TargetType<? super T> getRoot() {
        return root;
    }

    public boolean isEquivalentTo(TargetType<?> other) {
        return other == this || (parent != null && parent.isEquivalentTo(other));
    }

    public final List<ComponentType<?, ? super T>> getComponentTypes() {
        return componentTypes;
    }

    public final Set<ComponentType<?, T>> getOwnComponentTypes() {
        return Collections.unmodifiableSet(ownComponentTypes);
    }

    /**
     * Returns the {@link ComponentType} for the given component class, wrapped in an {@link Optional}.
     * If no such component is registered to this TargetType, this method will return an empty optional.
     *
     * @param componentClass the class corresponding to the desired ComponentType
     */
    @SuppressWarnings("unchecked")
    public final <C> Optional<? extends ComponentType<C, ? super T>> getComponentType(Class<C> componentClass) {
        return componentTypes.stream()
                .filter(c -> c.getComponentClass() == componentClass)
                .map(c -> (ComponentType<C, ? super T>) c)
                .findFirst();
    }

    public final <C> ComponentType<C, ? super T> getComponentTypeOrThrow(Class<C> componentClass) {
        return getComponentType(componentClass)
                .orElseThrow(() -> new RuntimeException("Missing component type for class: " + componentClass));
    }

    @SuppressWarnings("unchecked")
    public final <C> Optional<? extends ComponentType<C, T>> getOwnComponentType(Class<C> componentClass) {
        return ownComponentTypes.stream()
                .filter(c -> c.getComponentClass() == componentClass)
                .map(c -> (ComponentType<C, T>) c)
                .findFirst();
    }

    public final <C> ComponentType<C, T> getOwnComponentTypeOrThrow(Class<C> componentClass) {
        return getOwnComponentType(componentClass)
                .orElseThrow(() -> new RuntimeException("Missing component type for class: " + componentClass));
    }

    public final ComponentType<T, T> getIdentityComponentType() {
        return identityComponentType;
    }

    public final List<TargetType<? super T>> getSelfAndAllParents() {
        if (parent == null) return List.of(this);
        final var list = new ArrayList<TargetType<? super T>>();
        TargetType<? super T> type = this;
        do list.add(type); while ((type = type.parent) != null);
        return List.copyOf(list);
    }

    @SuppressWarnings("unchecked")
    public final List<TargetType<? extends T>> getAllSubtargets() {
        requireLocked();
        return IntStream.rangeClosed(subtargetIdxFirst, subtargetIdxLast)
                .mapToObj(i -> (TargetType<? extends T>) allSubtargets.get(i))
                .collect(Collectors.toUnmodifiableList());
    }

    public final List<TargetType<? extends T>> getDirectSubtargets() {
        requireLocked();
        return Collections.unmodifiableList(directSubtargets);
    }

    public final int getSubtargetIdxFirst() {
        return subtargetIdxFirst;
    }

    public final int getSubtargetIdxLast() {
        return subtargetIdxLast;
    }

    public final boolean isLocked() {
        return componentTypes != null;
    }

    public final void requireLocked() {
        if (componentTypes == null) throw FormatUtil.illStateExc("TargetType [] is not locked yet", this);
    }

    public final void requireNotLocked() {
        if (componentTypes != null) throw FormatUtil.illStateExc("TargetType [] is locked", this);
    }

    @Override
    public String toString() {
        return targetClass.getSimpleName();
    }

    public synchronized void lock() {
        if (this.componentTypes != null)
            throw FormatUtil.illStateExc("TargetType [] is already locked", this);

        final var componentList = new LinkedList<ComponentType<?, ? super T>>();
        if (parent == null) {
            this.populateSubtargets();
        } else {
            if (parent.componentTypes == null)
                throw FormatUtil.illStateExc("Parent TargetType [] is not locked", parent);
            componentList.addAll(parent.componentTypes);
            this.verifySiblings();
        }

        componentList.addAll(ownComponentTypes);

        for (int i = 0; i < componentList.size(); i++) {
            final var component = componentList.get(i);
            final var idx = component.getInternalIdx();
            if (idx < 0) {
                component.setInternalIdx(i);
            } else {
                if (idx != i) throw new IllegalStateException();
            }
        }

        this.componentTypes = List.copyOf(componentList);
    }

    protected synchronized void registerComponentType(ComponentType<?, T> componentType) {
        requireNotLocked();
        if (componentType.targetType != this) throw new IllegalArgumentException();
        if (ownComponentTypes.contains(componentType)) throw new IllegalStateException();
        ownComponentTypes.add(componentType);
    }

    private void populateSubtargets() {
        this.subtargetIdxFirst = this.allSubtargets.size();
        this.allSubtargets.add(this);
        for (final var sub : this.directSubtargets) sub.populateSubtargets();
        this.subtargetIdxLast = this.allSubtargets.size() - 1;
    }

    private void verifySiblings() {
        for (final var sibling : parent.directSubtargets) {
            if (sibling == this) continue;
            if (this.targetClass.isAssignableFrom(sibling.targetClass)) {
                throw PluginLoadingException.illegalSubtarget(this, sibling);
            }
        }
    }

}
