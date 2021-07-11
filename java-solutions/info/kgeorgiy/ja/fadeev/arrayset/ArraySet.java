package info.kgeorgiy.ja.fadeev.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements SortedSet<E> {
    private final List<E> list;
    private final Comparator<? super E> comparator;
    private final boolean isComparatorNull;

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Collection<? extends E> collection) {
        this(collection, null);
    }

    public ArraySet(Comparator<? super E> comparator) {
        this(Collections.emptyList(), comparator);
    }

    @SuppressWarnings("unchecked")
    public ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator) {
        SortedSet<E> sortedSet = new TreeSet<>(comparator);
        sortedSet.addAll(collection);
        list = List.copyOf(sortedSet);
        if (comparator == null) {
            isComparatorNull = true;
            //TreeMap getComparator()
            this.comparator = (((e1, e2) -> {
                Comparable<? super E> e = (Comparable<? super E>) e1;
                return e.compareTo(e2);
            }));
        } else {
            isComparatorNull = false;
            this.comparator = comparator;
        }
    }

    private ArraySet(List<E> sortedArray, Comparator<? super E> comparator, boolean isComparatorNull) {
        this.isComparatorNull = isComparatorNull;
        this.list = sortedArray;
        this.comparator = comparator;
    }

    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        try {
            return findIndex((E) o) >= 0;
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public Comparator<? super E> comparator() {
        return isComparatorNull ? null : comparator;
    }

    @Override
    public SortedSet<E> subSet(E from, E to) {
        return subSet(from, to, -1, -1);
    }

    // :NOTE: rewrite conditions
    private ArraySet<E> subSet(E from, E to, int iFrom, int iTo) {
        if (iFrom == -1 && iTo == -1 && comparator.compare(from, to) > 0) {
            throw new IllegalArgumentException();
        }
        int fromIndex = iFrom == -1 ? findRealIndex(from) : iFrom;
        int toIndex = iTo == -1 ? findRealIndex(to) : iTo;
        return new ArraySet<>(list.subList(fromIndex, toIndex), comparator, isComparatorNull);
    }

    @Override
    public SortedSet<E> headSet(E to) {
        if (isEmpty())
            return new ArraySet<>(comparator);
        return subSet(first(), to, 0, -1);
    }

    @Override
    public SortedSet<E> tailSet(E from) {
        if (isEmpty())
            return new ArraySet<>(comparator);
        return subSet(from, last(), -1, size());
    }

    @Override
    public E first() {
        if (isEmpty())
            throw new NoSuchElementException();
        return list.get(0);
    }

    @Override
    public E last() {
        if (isEmpty())
            throw new NoSuchElementException();
        return list.get(list.size() - 1);
    }

    private int findIndex(final E e) {
        return Collections.binarySearch(list, e, comparator);
    }

    private int findRealIndex(final E e) {
        int i = findIndex(e);
        if (i < 0)
            i = -i - 1;
        return i;
    }
}