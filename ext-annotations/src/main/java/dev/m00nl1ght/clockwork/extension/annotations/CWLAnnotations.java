package dev.m00nl1ght.clockwork.extension.annotations;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.MainComponent;
import dev.m00nl1ght.clockwork.utils.logger.Logger;
import org.jetbrains.annotations.NotNull;

public final class CWLAnnotations extends MainComponent {

    static final Logger LOGGER = Logger.create("Clockwork-Ext-Annotations");

    private CWLAnnotations(@NotNull ClockworkCore core) {
        super(core);
    }

}
