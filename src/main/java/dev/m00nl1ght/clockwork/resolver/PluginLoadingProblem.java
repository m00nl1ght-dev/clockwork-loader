package dev.m00nl1ght.clockwork.resolver;

import dev.m00nl1ght.clockwork.core.ComponentDefinition;
import dev.m00nl1ght.clockwork.core.DependencyDefinition;

public abstract class PluginLoadingProblem {

    protected final ComponentDefinition errored;

    protected PluginLoadingProblem(ComponentDefinition errored) {
        this.errored = errored;
    }

    public abstract String getMessage();

    public boolean isFatal() {
        return true;
    }

    public static DependencyNotFound depNotFound(ComponentDefinition errored, DependencyDefinition required, ComponentDefinition present, boolean depSkipped) {
        return new DependencyNotFound(errored, required, present, depSkipped);
    }

    public static class DependencyNotFound extends PluginLoadingProblem {

        private final DependencyDefinition required;
        private final ComponentDefinition present;
        private final boolean depSkipped;

        private DependencyNotFound(ComponentDefinition errored, DependencyDefinition required, ComponentDefinition present, boolean depSkipped) {
            super(errored);
            this.required = required;
            this.present = present;
            this.depSkipped = depSkipped;
        }

        @Override
        public String getMessage() {
            if (depSkipped) {
                return "Required dependency [" + required.getDescriptor() + "] was skipped";
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

    public static DuplicateIdFound duplicateIdFound(ComponentDefinition current, ComponentDefinition present) {
        return new DuplicateIdFound(current, present);
    }

    public static class DuplicateIdFound extends PluginLoadingProblem {

        private final ComponentDefinition present;

        protected DuplicateIdFound(ComponentDefinition current, ComponentDefinition present) {
            super(current);
            this.present = present;
        }

        public ComponentDefinition getPresent() {
            return present;
        }

        @Override
        public String getMessage() {
            return "Another component with the same id is already present: " + present;
        }

    }

    public static DepCycleFound depCycleFound(ComponentDefinition tail) {
        return new DepCycleFound(tail);
    }

    public static class DepCycleFound extends PluginLoadingProblem {

        protected DepCycleFound(ComponentDefinition tail) {
            super(tail);
        }

        @Override
        public String getMessage() {
            return "This component has a transient dependency on itself";
        }

    }

}
