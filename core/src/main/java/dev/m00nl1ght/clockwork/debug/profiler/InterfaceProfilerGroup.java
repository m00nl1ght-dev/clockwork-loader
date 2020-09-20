package dev.m00nl1ght.clockwork.debug.profiler;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.interfaces.InterfaceType;

import java.util.List;
import java.util.function.Consumer;

public class InterfaceProfilerGroup<I, T extends ComponentTarget> extends ProfilerGroup implements Consumer<I> {

    protected final TargetType<T> targetType;
    protected final InterfaceType<I, ? super T> interfaceType;
    protected ProfilerEntry[] compEntries;

    private Consumer<? super I> consumer;
    private int idx = -1;

    public InterfaceProfilerGroup(InterfaceType<I, ? super T> interfaceType, TargetType<T> targetType) {
        this(interfaceType, targetType, 100);
    }

    public InterfaceProfilerGroup(InterfaceType<I, ? super T> interfaceType, TargetType<T> targetType, int bufferSize) {
        super(interfaceType.getInterfaceClass().getSimpleName() + "@" + targetType.toString());
        this.targetType = targetType;
        this.interfaceType = interfaceType;
        final var components = interfaceType.getEffectiveComponents(targetType);
        this.compEntries = new ProfilerEntry[components.size()];
        for (int i = 0; i < components.size(); i++) {
            compEntries[i] = new SimpleCyclicProfilerEntry(components.get(i).toString(), bufferSize);
        }
    }

    @Override
    public List<ProfilerEntry> getEntries() {
        return List.of(compEntries);
    }

    public ProfilerEntry get(int idx) {
        return compEntries[idx];
    }

    public TargetType<T> getTargetType() {
        return targetType;
    }

    public InterfaceType<I, ? super T> getInterfaceType() {
        return interfaceType;
    }

    public void begin(Consumer<? super I> consumer) {
        this.consumer = consumer;
        this.idx = 0;
    }

    @Override
    public void accept(I object) {
        final long t = System.nanoTime();
        consumer.accept(object);
        compEntries[idx].put(System.nanoTime() - t);
        idx++;
    }

}
