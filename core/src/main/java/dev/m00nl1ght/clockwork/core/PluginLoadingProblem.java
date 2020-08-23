package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.locator.PluginLocator;

public abstract class PluginLoadingProblem {

    private final String cause;

    PluginLoadingProblem(String cause) {
        this.cause = cause;
    }

    public abstract String getMessage();

    public String format() {
        return "[" + cause + "] - " + getMessage();
    }

    public boolean isFatal() {
        return true;
    }

    public static PluginNotFound pluginNotFound(DependencyDescriptor target) {
        return new PluginNotFound(target);
    }

    public static class PluginNotFound extends PluginLoadingProblem {

        private final DependencyDescriptor missing;

        private PluginNotFound(DependencyDescriptor missing) {
            super(missing.getTarget());
            this.missing = missing;
        }

        @Override
        public String getMessage() {
            return "Could not locate plugin [" + missing.toString() + "]";
        }

        public DependencyDescriptor getMissing() {
            return missing;
        }

    }

    public static LocatorMismatch locatorMismatch(PluginReference plugin, PluginLocator actualLocator) {
        return new LocatorMismatch(plugin, actualLocator);
    }

    public static class LocatorMismatch extends PluginLoadingProblem {

        private final PluginReference plugin;
        private final PluginLocator actualLocator;

        private LocatorMismatch(PluginReference plugin, PluginLocator actualLocator) {
            super(plugin.getId());
            this.plugin = plugin;
            this.actualLocator = actualLocator;
        }

        @Override
        public String getMessage() {
            final String locA = actualLocator.getName(), locB = plugin.getLocator().getName();
            return "Locator [" + locA + "] returned this definition, but it was actually located by [" + locB + "]";
        }

        public PluginLocator getActualLocator() {
            return actualLocator;
        }

        public PluginReference getPlugin() {
            return plugin;
        }

    }

    public static DependencyNotFound depNotFound(ComponentDescriptor errored, DependencyDescriptor required, ComponentDescriptor present) {
        return new DependencyNotFound(errored, required, present);
    }

    public static DependencyNotFound depSkipped(ComponentDescriptor errored, ComponentDescriptor present) {
        return new DependencyNotFound(errored, null, present);
    }

    public static class DependencyNotFound extends PluginLoadingProblem {

        private final ComponentDescriptor component;
        private final DependencyDescriptor required;
        private final ComponentDescriptor present;

        private DependencyNotFound(ComponentDescriptor component, DependencyDescriptor required, ComponentDescriptor present) {
            super(component.getId());
            this.component = component;
            this.required = required;
            this.present = present;
        }

        @Override
        public String getMessage() {
            if (required == null) {
                return "Required dependency [" + present + "] was skipped";
            } else if (present == null) {
                return "Missing required dependency [" + required.toString() + "]";
            } else {
                return "Found incorrect version [" + present.getVersion() + "] of dependency [" + required.toString() + "]";
            }
        }

        @Override
        public boolean isFatal() {
            return !component.isOptional();
        }

        public ComponentDescriptor getComponent() {
            return component;
        }

        public ComponentDescriptor getPresent() {
            return present;
        }

        public DependencyDescriptor getRequired() {
            return required;
        }

    }

    public static PluginLoadingProblem parentNotFound(TargetDescriptor target) {
        return new ParentNotFound(target);
    }

    public static class ParentNotFound extends PluginLoadingProblem {

        private final TargetDescriptor target;

        private ParentNotFound(TargetDescriptor target) {
            super(target.getId());
            this.target = target;
        }

        @Override
        public String getMessage() {
            return "Parent [" + target.getParent() + "] for target [" + target.getId() + "] not found";
        }

        public TargetDescriptor getTarget() {
            return target;
        }

    }

    public static <T> DuplicateIdFound<T> duplicateIdFound(PluginDescriptor plugin, T current, T present) {
        return new DuplicateIdFound<>(plugin, current, present);
    }

    public static class DuplicateIdFound<T> extends PluginLoadingProblem {

        private final T current, present;

        private DuplicateIdFound(PluginDescriptor plugin, T current, T present) {
            super(plugin.getId());
            this.current = current;
            this.present = present;
        }

        public T getPresent() {
            return present;
        }

        public T getCurrent() {
            return current;
        }

        @Override
        public String getMessage() {
            return "Registered " + current + " but a " + current.getClass().getSimpleName() + " with the same id is already present: " + present;
        }

    }

    public static <T> DepCycleFound<T> depCycleFound(PluginDescriptor plugin, T tail) {
        return new DepCycleFound<>(plugin, tail);
    }

    public static class DepCycleFound<T> extends PluginLoadingProblem {

        private final T tail;

        private DepCycleFound(PluginDescriptor plugin, T tail) {
            super(plugin.getId());
            this.tail = tail;
        }

        @Override
        public String getMessage() {
            return tail.getClass().getSimpleName() + " [" + tail + "] has a (transient) dependency on itself";
        }

    }

}
