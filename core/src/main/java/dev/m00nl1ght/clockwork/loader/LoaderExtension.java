package dev.m00nl1ght.clockwork.loader;

import dev.m00nl1ght.clockwork.core.Component;
import org.jetbrains.annotations.NotNull;

public abstract class LoaderExtension extends Component<ClockworkLoader> {

    protected LoaderExtension(@NotNull ClockworkLoader target) {
        super(target);
    }

    public void registerFeatures() {}

    public void initFeatures() {}

    public void onCoreConstructed() {}

    public void onCorePopulated() {}

    public void onCoreProcessed() {}

    public void onCoreInitialised() {}

}
