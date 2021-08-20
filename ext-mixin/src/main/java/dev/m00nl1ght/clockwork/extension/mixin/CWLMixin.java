package dev.m00nl1ght.clockwork.extension.mixin;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.MainComponent;
import dev.m00nl1ght.clockwork.logger.Logger;
import org.jetbrains.annotations.NotNull;

public final class CWLMixin extends MainComponent {

    static final Logger LOGGER = Logger.create("Clockwork-Ext-Mixin");

    private CWLMixin(@NotNull ClockworkCore core) {
        super(core);
    }

}
