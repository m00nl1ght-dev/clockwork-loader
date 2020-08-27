package dev.m00nl1ght.clockwork.extension.annotations;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.PluginProcessor;
import dev.m00nl1ght.clockwork.core.PluginProcessorContext;

class InternalPluginProcessor implements PluginProcessor {

    public static final String NAME = "extension.annotations.internal";

    private final CWLAnnotationsExtension extension;

    InternalPluginProcessor(CWLAnnotationsExtension extension) {
        this.extension = extension;
    }

    @Override
    public void process(PluginProcessorContext context) {
        final var mainComponent = context.getPlugin().getMainComponent();
        if (mainComponent.getComponentClass() == CWLAnnotationsExtension.class) {
            @SuppressWarnings("unchecked")
            final var extensionComponent = (ComponentType<CWLAnnotationsExtension, ClockworkCore>) mainComponent;
            context.setComponentFactory(extensionComponent, extension::buildChild);
        }
    }

}
