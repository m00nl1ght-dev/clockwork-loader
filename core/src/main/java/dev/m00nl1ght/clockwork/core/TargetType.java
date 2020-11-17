package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.FormatUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TargetType<T extends ComponentTarget> {

    protected final Class<T> targetClass;
    protected final TargetType<? super T> root;
    protected final TargetType<? super T> parent;
    protected final IdentityComponentType<T> identityComponentType;

    private final List<TargetType<? extends T>> directSubtargets = new LinkedList<>();
    private final List<TargetType<?>> allSubtargets;

    private final Set<ComponentType<?, T>> ownComponentTypes = new LinkedHashSet<>();
    private List<ComponentType<?, ? super T>> componentTypes;
    private int subtargetIdxFirst = -1, subtargetIdxLast = -1;

    public TargetType(TargetType<? super T> parent, Class<T> targetClass) {
        this.targetClass = Objects.requireNonNull(targetClass);
        this.parent = parent;
        if (this.parent == null) {
            this.root = this;
            this.allSubtargets = new LinkedList<>();
            this.identityComponentType = new IdentityComponentType<>(null, this);
        } else {
            synchronized (this.parent) {
                this.root = this.parent.root;
                if (!this.parent.targetClass.isAssignableFrom(targetClass))
                    throw FormatUtil.illArgExc("Heap pollution: Incompatible parent target [] for []", parent, this);
                if (this.parent.isInitialised())
                    throw FormatUtil.illStateExc("Parent TargetType [] is already initialised", this.parent);
                this.parent.directSubtargets.add(this);
                this.allSubtargets = this.parent.allSubtargets;
                this.identityComponentType = new IdentityComponentType<>(parent.identityComponentType, this);
            }
        }
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

    public Set<ComponentType<?, T>> getOwnComponentTypes() {
        return Collections.unmodifiableSet(ownComponentTypes);
    }

    /**
     * Returns the {@link ComponentType} for the given component class, wrapped in an {@link Optional}.
     * If no such component is registered to this TargetType, this method will return an empty optional.
     *
     * @param componentClass the class corresponding to the desired ComponentType
     */
    @SuppressWarnings("unchecked")
    public <C> Optional<? extends ComponentType<C, ? super T>> getComponentType(Class<C> componentClass) {
        return componentTypes.stream()
                .filter(c -> c.getComponentClass() == componentClass)
                .map(c -> (ComponentType<C, ? super T>) c)
                .findFirst();
    }

    @SuppressWarnings("unchecked")
    public <C> Optional<? extends ComponentType<C, T>> getOwnComponentType(Class<C> componentClass) {
        return ownComponentTypes.stream()
                .filter(c -> c.getComponentClass() == componentClass)
                .map(c -> (ComponentType<C, T>) c)
                .findFirst();
    }

    public final ComponentType<T, T> getIdentityComponentType() {
        return identityComponentType;
    }

    public final List<TargetType<? super T>> getAllParents() {
        if (parent == null) return Collections.emptyList();
        final var list = new ArrayList<TargetType<? super T>>();
        TargetType<? super T> type = this;
        while ((type = type.parent) != null) list.add(type);
        return List.copyOf(list);
    }

    @SuppressWarnings("unchecked")
    public final List<TargetType<? extends T>> getAllSubtargets() {
        requireInitialised();
        return IntStream.rangeClosed(subtargetIdxFirst, subtargetIdxLast)
                .mapToObj(i -> (TargetType<? extends T>) allSubtargets.get(i))
                .collect(Collectors.toUnmodifiableList());
    }

    public final List<TargetType<? extends T>> getDirectSubtargets() {
        requireInitialised();
        return Collections.unmodifiableList(directSubtargets);
    }

    public final int getSubtargetIdxFirst() {
        return subtargetIdxFirst;
    }

    public final int getSubtargetIdxLast() {
        return subtargetIdxLast;
    }

    public final boolean isInitialised() {
        return componentTypes != null;
    }

    public final void requireInitialised() {
        if (componentTypes == null) throw FormatUtil.illStateExc("TargetType [] is not initialised", this);
    }

    public final void requireNotInitialised() {
        if (componentTypes != null) throw FormatUtil.illStateExc("TargetType [] is initialised", this);
    }

    @Override
    public String toString() {
        return targetClass.getSimpleName();
    }

    // ### Internal ###

    protected final synchronized void init() {
        if (this.componentTypes != null)
            throw FormatUtil.illStateExc("TargetType [] is already initialised", this);

        final var componentList = new LinkedList<ComponentType<?, ? super T>>();
        if (parent == null) {
            this.populateSubtargets();
        } else {
            if (parent.componentTypes == null)
                throw FormatUtil.illStateExc("Parent TargetType [] is not initialised", parent);
            componentList.addAll(parent.componentTypes);
            this.verifySiblings();
        }

        for (final var component : ownComponentTypes) {
            if (component.parent != null) {
                componentList.set(component.parent.getInternalIdx(), component);
            } else {
                componentList.add(component);
            }
        }

        for (int i = 0; i < componentList.size(); i++) {
            final var component = componentList.get(i);
            final var idx = component.getInternalIdx();
            if (idx < 0) {
                component.init(i);
            } else {
                if (idx != i) throw new IllegalStateException();
            }
        }

        this.componentTypes = List.copyOf(componentList);
    }

    synchronized void registerComponentType(ComponentType<?, T> componentType) {
        requireNotInitialised();
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
