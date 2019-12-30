package com.rpc.common.util;

import java.util.*;

import static com.rpc.common.util.Preconditions.checkArgument;
import static com.rpc.common.util.Preconditions.checkNotNull;


public final class Lists {


    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<>();
    }


    @SuppressWarnings("unchecked")
    public static <E> ArrayList<E> newArrayList(E... elements) {
        checkNotNull(elements);
        ArrayList<E> list = new ArrayList<>();
        Collections.addAll(list, elements);
        return list;
    }

    @SuppressWarnings("unchecked")
    public static <E> ArrayList<E> newArrayList(Iterable<? extends E> elements) {
        checkNotNull(elements);
        return elements instanceof Collection
                ? new ArrayList((Collection<E>) elements)
                : newArrayList(elements.iterator());
    }


    public static <E> ArrayList<E> newArrayList(Iterator<? extends E> elements) {
        ArrayList<E> list = newArrayList();
        while (elements.hasNext()) {
            list.add(elements.next());
        }
        return list;
    }


    public static <E> ArrayList<E> newArrayListWithCapacity(int initialArraySize) {
        checkArgument(initialArraySize >= 0);
        return new ArrayList<>(initialArraySize);
    }


    private Lists() {}
}
