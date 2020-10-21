package dev.m00nl1ght.clockwork.extension.nightconfig;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.plugin.CWLPlugin;
import dev.m00nl1ght.clockwork.core.plugin.CollectClockworkExtensionsEvent;
import dev.m00nl1ght.clockwork.util.FormatUtil;

public final class CWLNightconfigExtension {

    private final ClockworkCore core;

    private CWLNightconfigExtension(ClockworkCore core) {
        this.core = core;
        this.attachEventListener();
    }

    // ### Internal ###

    private void attachEventListener() {
        final var cwlPluginComponent = core.getComponentType(CWLPlugin.class, ClockworkCore.class).orElseThrow();
        final var extComponent = core.getComponentType(CWLNightconfigExtension.class, ClockworkCore.class).orElseThrow();
        final var cwlPlugin = cwlPluginComponent.get(core);
        if (cwlPlugin == null) throw FormatUtil.illStateExc("Internal core component missing");
        cwlPlugin.getCollectExtensionsEventType()
                .addListener(extComponent, CWLNightconfigExtension::onCollectExtensionsEvent);
    }

    private void onCollectExtensionsEvent(CollectClockworkExtensionsEvent event) {
        NightconfigPluginReader.registerTo(event.getReaderTypeRegistry());
    }

}
