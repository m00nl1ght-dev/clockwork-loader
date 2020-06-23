package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.locator.PluginLocator;

public abstract class PluginLoadingProblem {

    protected final String cause;

    protected PluginLoadingProblem(ComponentDefinition errored) {
        this.cause = errored.getId();
    }

    protected PluginLoadingProblem(ComponentDescriptor errored) {
        this.cause = errored.getTarget();
    }

    public abstract String getMessage();

    public String format() {
        return "[" + cause + "] - " + getMessage();
    }

    public boolean isFatal() {
        return true;
    }

    public static PluginNotFound pluginNotFound(ComponentDescriptor target) {
        return new PluginNotFound(target);
    }

    public static class PluginNotFound extends PluginLoadingProblem {

        private final ComponentDescriptor target;

        private PluginNotFound(ComponentDescriptor target) {
            super(target);
            this.target = target;
        }

        @Override
        public String getMessage() {
            return "Could not locate plugin [" + target.toString() + "]";
        }

    }

    public static LocatorMismatch locatorMismatch(ComponentDefinition errored, PluginLocator locator) {
        return new LocatorMismatch(errored, locator);
    }

    public static class LocatorMismatch extends PluginLoadingProblem {

        private final ComponentDefinition errored;
        private final PluginLocator locator;

        private LocatorMismatch(ComponentDefinition errored, PluginLocator locator) {
            super(errored);
            this.errored = errored;
            this.locator = locator;
        }

        @Override
        public String getMessage() {
            final String locA = locator.getName(), locB = errored.getParent().getLocator().getName();
            return "Locator [" + locA + "] returned this definition, but it was actually located by [" + locB + "]";
        }

    }

    public static DependencyNotFound depNotFound(ComponentDefinition errored, ComponentDescriptor required, ComponentDefinition present) {
        return new DependencyNotFound(errored, required, present);
    }

    public static DependencyNotFound depSkipped(ComponentDefinition errored, ComponentDefinition present) {
        return new DependencyNotFound(errored, null, present);
    }

    public static class DependencyNotFound extends PluginLoadingProblem {

        private final ComponentDefinition errored;
        private final ComponentDescriptor required;
        private final ComponentDefinition present;

        private DependencyNotFound(ComponentDefinition errored, ComponentDescriptor required, ComponentDefinition present) {
            super(errored);
            this.errored = errored;
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
            return !errored.isOptional();
        }

    }

    public static PluginLoadingProblem parentNotFound(TargetDefinition obj) {
        return new ParentNotFound(obj.getPlugin().getMainComponent(), obj);
    }

    public static class ParentNotFound extends PluginLoadingProblem {

        private final TargetDefinition def;

        private ParentNotFound(ComponentDefinition errored, TargetDefinition def) {
            super(errored);
            this.def = def;
        }

        @Override
        public String getMessage() {
            return "Parent [" + def.getParent() + "] for target [" + def + "] not found";
        }

    }

    public static <T> DuplicateIdFound<T> duplicateIdFound(ComponentDefinition def, T current, T present) {
        return new DuplicateIdFound<>(def, current, present);
    }

    public static class DuplicateIdFound<T> extends PluginLoadingProblem {

        private final T current, present;

        protected DuplicateIdFound(ComponentDefinition def, T current, T present) {
            super(def);
            this.current = current;
            this.present = present;
        }

        public T getPresent() {
            return present;
        }

        @Override
        public String getMessage() {
            if (current instanceof ComponentDefinition) {
                return "Another component with the same id is already present: " + present;
            } else {
                return "Registered " + current + " but a " + current.getClass().getSimpleName() + " with the same id is already present: " + present;
            }
        }

    }

    public static <T> DepCycleFound<T> depCycleFound(ComponentDefinition def, T tail) {
        return new DepCycleFound<>(def, tail);
    }

    public static class DepCycleFound<T> extends PluginLoadingProblem {

        private final T tail;

        protected DepCycleFound(ComponentDefinition def, T tail) {
            super(def);
            this.tail = tail;
        }

        @Override
        public String getMessage() {
            if (tail instanceof ComponentDefinition) {
                return "This component has a (transient) dependency on itself";
            } else {
                return "The " + tail.getClass().getSimpleName() + " " + tail + " defined in this component is a (transient) parent of itself";
            }
        }

    }

}
