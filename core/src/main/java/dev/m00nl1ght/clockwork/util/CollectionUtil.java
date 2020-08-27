package dev.m00nl1ght.clockwork.util;

import java.util.*;

public class CollectionUtil {

    public static <T> Iterator<T> compoundIterator(Iterator<T> iteratorA, Iterator<T> iteratorB) {
        Arguments.notNull(iteratorA, "iteratorA");
        Arguments.notNull(iteratorB, "iteratorB");
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
        Arguments.notNull(listA, "listA");
        Arguments.notNull(listB, "listB");
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

    public static <T> List<T> joinedCopy(Collection<? extends T> collectionA, Collection<? extends T> collectionB) {
        final var joined = new ArrayList<T>(collectionA.size() + collectionB.size());
        joined.addAll(collectionA);
        joined.addAll(collectionB);
        return Collections.unmodifiableList(joined);
    }

}
