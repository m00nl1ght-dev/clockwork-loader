package dev.m00nl1ght.clockwork.interfaces.impl;

import dev.m00nl1ght.clockwork.core.ComponentTarget;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class InterfaceIterator<T> implements Iterator<T> {

    private final ComponentTarget object;
    private final int[] componentIdxs;
    private int idx = 0;
    private T next = null;

    public InterfaceIterator(ComponentTarget object, int[] componentIdxs) {
        this.object = object;
        this.componentIdxs = componentIdxs;
        this.findNext();
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public T next() {
        if (next == null) throw new NoSuchElementException();
        final var ret = next;
        findNext();
        return ret;
    }

    private void findNext() {
        next = null;
        while (idx < componentIdxs.length) {
            @SuppressWarnings("unchecked")
            final var comp = (T) object.getComponent(componentIdxs[idx++]);
            if (comp != null) {
                next = comp;
                return;
            }
        }
    }

}
