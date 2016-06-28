package org.bukkit.craftbukkit.util;

import com.google.common.collect.Sets;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;


public class HashTreeSet<V> implements Set<V> {

     /*private HashSet<V> hash = new HashSet<V>();
    private TreeSet<V> tree = new TreeSet<V>();*/
    private Set<V> hash = Sets.newConcurrentHashSet();
    private ConcurrentSkipListSet<V> tree = new ConcurrentSkipListSet<V>();

    public HashTreeSet() {

    }

    @Override
    public int size() {
        return hash.size();
    }

    @Override
    public boolean isEmpty() {
        return hash.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return hash.contains(o);
    }

    @Override
    public Iterator<V> iterator() {
        return new Iterator<V>() {

            private Iterator<V> it = tree.iterator();
            private V last;

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public V next() {
                return last = it.next();
            }

            @Override
            public void remove() {
                if (last == null) {
                    throw new IllegalStateException();
                }
                it.remove();
                hash.remove(last);
                last = null;
            }
        };
    }

    @Override
    public Object[] toArray() {
        return hash.toArray();
    }

    @Override
    public Object[] toArray(Object[] a) {
        return hash.toArray(a);
    }

    @Override
    public boolean add(V e) {
        hash.add(e);
        return tree.add(e);
    }

    @Override
    public boolean remove(Object o) {
        hash.remove(o);
        return tree.remove(o);
    }

    @Override
    public boolean containsAll(Collection c) {
        return hash.containsAll(c);
    }

    @Override
    public boolean addAll(Collection c) {
        tree.addAll(c);
        return hash.addAll(c);
    }

    @Override
    public boolean retainAll(Collection c) {
        tree.retainAll(c);
        return hash.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection c) {
        tree.removeAll(c);
        return hash.removeAll(c);
    }

    @Override
    public void clear() {
        hash.clear();
        tree.clear();
    }

    public synchronized V first() {
        return tree.first();
    }
	
	static class IterateOnlySortedSet<E> extends AbstractSet<E> implements SortedSet<E> {
     private final ArrayList<E> elements;
     private final Comparator<? super E> comparator;

     public IterateOnlySortedSet(SortedSet<E> source) {
       elements = new ArrayList<>(source);
       comparator = source.comparator();
     }

     @Override
     public Iterator<E> iterator() {
       return elements.iterator();
     }

     @Override
     public int size() {
       return elements.size();
     }

     @Override
     public Comparator<? super E> comparator() {
       return comparator;
     }

     // remaining methods simply throw UnsupportedOperationException

     @Override
     public SortedSet<E> subSet(E fromElement, E toElement) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }

     @Override
     public SortedSet<E> headSet(E toElement) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }

     @Override
     public SortedSet<E> tailSet(E fromElement) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }

     @Override
     public E first() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }

     @Override
     public E last() {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 }

}
