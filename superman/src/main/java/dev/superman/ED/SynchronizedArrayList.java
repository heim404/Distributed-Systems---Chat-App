package dev.superman.ED;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SynchronizedArrayList<T> {
    private List<T> list;

    public SynchronizedArrayList() {
        list = new ArrayList<>();
    }

    public synchronized boolean add(T element) {
        return list.add(element);
    }

    public synchronized T remove(int index) {
        return list.remove(index);
    }

    public synchronized List<T> get() {
        return list;
    }

    public Iterator<T> iterator() {
        return list.iterator();
    }

    public synchronized void clear() {
        list.clear();
    }

    public synchronized boolean isEmpty() {
        return list.isEmpty();
    }

    public synchronized int size() {
        return list.size();
    }
}
