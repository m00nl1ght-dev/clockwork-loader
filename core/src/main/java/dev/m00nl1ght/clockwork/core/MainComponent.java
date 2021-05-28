package dev.m00nl1ght.clockwork.core;

import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles;

public abstract class MainComponent extends Component<ClockworkCore> {

    protected MainComponent(@NotNull ClockworkCore core) {
        super(core);
    }

    protected MethodHandles.Lookup getReflectiveAccess() {
        return null;
    }

}
