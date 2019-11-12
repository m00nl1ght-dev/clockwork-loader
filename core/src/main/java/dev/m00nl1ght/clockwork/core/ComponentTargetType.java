package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.event.EventType;
import dev.m00nl1ght.clockwork.event.EventTypeRegistry;
import dev.m00nl1ght.clockwork.util.CollectionUtil;
import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.*;

public final class ComponentTargetType<T> {

    private final String id;
    private final Class<T> targetClass;
    private final PluginContainer plugin;
    private boolean lockRegistry = false;
    private final ComponentTargetType<? super T> parent;
    private final List<ComponentType<?, ? super T>> allComponents;
    private final Map<String, ComponentType<?, T>> components = new HashMap<>();
    private final ArrayList<ComponentType<?, T>> ownComponents = new ArrayList<>();
    private final Map<Class<?>, EventType<?, T>> eventTypes = new HashMap<>();
    private final EventTypeRegistry eventTypeRegistry;

    protected ComponentTargetType(ComponentTargetDefinition definition, PluginContainer plugin, Class<T> targetClass) {
        this.id = definition.getId();
        this.plugin = Preconditions.notNull(plugin, "plugin");
        this.parent = definition.getParent() == null ? null : findParent(plugin.getClockworkCore(), definition.getParent());
        this.allComponents = parent == null ? Collections.unmodifiableList(ownComponents) : CollectionUtil.compoundList(parent.allComponents, ownComponents);
        this.eventTypeRegistry = plugin.getClockworkCore().getEventTypeRegistry();
        Preconditions.notNull(definition, "definition");
        Preconditions.verifyType(targetClass, ComponentTarget.class, "targetClass");
        this.targetClass = Preconditions.notNullAnd(targetClass, o -> definition.getTargetClass().equals(o.getCanonicalName()), "targetClass");
    }

    @SuppressWarnings("unchecked")
    private ComponentTargetType<? super T> findParent(ClockworkCore core, String id) {
        final var found = core.getTargetType(id).orElseThrow();
        if (found.targetClass.isAssignableFrom(this.targetClass)) {
            return (ComponentTargetType<? super T>) found;
        } else {
            throw PluginLoadingException.invalidParentForTarget(this, found);
        }
    }

    protected synchronized <C> ComponentType<C, T> register(ComponentDefinition def, PluginContainer plugin, Class<C> compClass) {
        if (lockRegistry) throw new IllegalStateException("target type registry already locked");
        final var componentType = new ComponentType<>(def, plugin, compClass, this, allComponents.size());
        components.put(componentType.getId(), componentType);
        ownComponents.add(componentType);
        return componentType;
    }

    @SuppressWarnings("unchecked")
    public <E> EventType<E, T> getEventType(Class<E> eventClass) { // TODO what about parent target type?
        Preconditions.notNull(eventClass, "eventClass");
        if (!Event.class.isAssignableFrom(eventClass)) throw new IllegalArgumentException("eventClass must be a sublass of Event.class");
        return (EventType<E, T>) eventTypes.computeIfAbsent(eventClass, k -> eventTypeRegistry.getEventTypeFor(k, this));
    }

    public <F> FunctionalSubtarget<T, F> getSubtarget(Class<F> type) {
        Preconditions.notNull(type, "type");
        return new FunctionalSubtarget<>(this, type);
    }

    public PluginContainer getPlugin() {
        return plugin;
    }

    public String getId() {
        return id;
    }

    public Class<T> getTargetClass() {
        return targetClass;
    }

    public List<ComponentType<?, ? super T>> getRegisteredTypes() {
        return allComponents;
    }

    protected int getComponentCount() {
        return allComponents.size();
    }

    protected synchronized void lockRegistry() {
        lockRegistry = true;
        ownComponents.trimToSize();
    }

    protected boolean isRegistryLocked() {
        return lockRegistry;
    }

}
