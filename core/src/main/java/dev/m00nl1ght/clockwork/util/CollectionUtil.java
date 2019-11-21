package dev.m00nl1ght.clockwork.util;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

public class CollectionUtil {

    public static <T> Iterator<T> compoundIterator(Iterator<T> iteratorA, Iterator<T> iteratorB) {
        Preconditions.notNull(iteratorA, "iteratorA");
        Preconditions.notNull(iteratorB, "iteratorB");
        return new CompoundIterator<>(iteratorA, iteratorB);
    }

    private static class CompoundIterator<T> implements Iterator<T> {

        private final Iterator<T> iteratorA, iteratorB;

        private CompoundIterator(Iterator<T> iteratorA, Iterator<T> iteratorB) {
            this.iteratorA = iteratorA;
            this.iteratorB = iteratorB;
        }

        @Override
        public boolean hasNext() {
            return iteratorA.hasNext() || iteratorB.hasNext();
        }

        @Override
        public T next() {
            return iteratorA.hasNext() ? iteratorA.next() : iteratorB.next();
        }

    }

    public static <T> List<T> compoundList(List<? extends T> listA, List<? extends T> listB) {
        Preconditions.notNull(listA, "listA");
        Preconditions.notNull(listB, "listB");
        return new CompoundList<>(listA, listB);
    }

    private static class CompoundList<T> extends AbstractList<T> {

        private final List<? extends T> listA;
        private final List<? extends T> listB;

        private CompoundList(List<? extends T> listA, List<? extends T> listB) {
            this.listA = listA;
            this.listB = listB;
        }

        @Override
        public int size() {
            return listA.size() + listB.size();
        }

        @Override
        public T get(int index) {
            return index < listA.size() ? listA.get(index) : listB.get(index - listA.size());
        }

    }

}