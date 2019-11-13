package dev.m00nl1ght.clockwork.core;

public abstract class PluginLoadingProblem {

    protected final ComponentDefinition errored;

    protected PluginLoadingProblem(ComponentDefinition errored) {
        this.errored = errored;
    }

    public abstract String getMessage();

    public String format() {
        return "[" + errored.getId() + "] - " + getMessage();
    }

    public boolean isFatal() {
        return true;
    }

    public static DependencyNotFound depNotFound(ComponentDefinition errored, DependencyDefinition required, ComponentDefinition present) {
        return new DependencyNotFound(errored, required, present);
    }

    public static DependencyNotFound depSkipped(ComponentDefinition errored, ComponentDefinition present) {
        return new DependencyNotFound(errored, null, present);
    }

    public static class DependencyNotFound extends PluginLoadingProblem {

        private final DependencyDefinition required;
        private final ComponentDefinition present;

        private DependencyNotFound(ComponentDefinition errored, DependencyDefinition required, ComponentDefinition present) {
            super(errored);
            this.required = required;
            this.present = present;
        }

        @Override
        public String getMessage() {
            if (required == null) {
                return "Required dependency [" + present + "] was skipped";
            } else if (present == null) {
                return "Missing required dependency [" + required.getDescriptor() + "]";
            } else {
                return "Found incorrect version [" + present.getVersion() + "] of dependency [" + required.getDescriptor() + "]";
            }
        }

        @Override
        public boolean isFatal() {
            return !errored.isOptional();
        }

    }

    public static PluginLoadingProblem parentNotFound(ComponentTargetDefinition obj) {
        return new ParentNotFound(obj.getPlugin().getMainComponent(), obj);
    }

    public static class ParentNotFound extends PluginLoadingProblem {

        private final ComponentTargetDefinition def;

        private ParentNotFound(ComponentDefinition errored, ComponentTargetDefinition def) {
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

        private T tail;

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
