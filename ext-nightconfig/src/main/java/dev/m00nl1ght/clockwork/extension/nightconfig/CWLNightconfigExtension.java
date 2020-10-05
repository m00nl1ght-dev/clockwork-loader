package dev.m00nl1ght.clockwork.extension.nightconfig;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.plugin.CWLPlugin;
import dev.m00nl1ght.clockwork.core.plugin.CollectClockworkExtensionsEvent;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class CWLNightconfigExtension {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ClockworkCore core;

    private CWLNightconfigExtension(ClockworkCore core) {
        this.core = core;
        this.attachEventListener();
    }

    // ### Internal ###

    private void attachEventListener() {
        final var cwlPluginComponent = core.getComponentType(CWLPlugin.class, ClockworkCore.class).orElseThrow();
        final var cwlPlugin = cwlPluginComponent.get(core);
        if (cwlPlugin == null) throw FormatUtil.illStateExc("Internal core component missing");
        cwlPlugin.getCollectExtensionsEventType().addListener(core, CWLNightconfigExtension.class,
                CWLNightconfigExtension::onCollectExtensionsEvent);
    }

    private void onCollectExtensionsEvent(CollectClockworkExtensionsEvent event) {
        NightconfigPluginReader.registerTo(event);
    }

}