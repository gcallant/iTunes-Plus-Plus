package Utilities;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * @author Ryan Babcock
 */

public class SharedQueue<E> implements Iterable<E> {
    final private LinkedList<E> myList;
    private int maxSize;
    private boolean stop = false;

    public SharedQueue(int size) {
        myList = new LinkedList<>();
        maxSize = size;
    }

    public synchronized E get(int i){
        return myList.get(i);
    }

    public synchronized void enqueue(E job) throws IllegalArgumentException {
        while(isFull()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new NoSuchElementException("Thread Interrupted!");
            }
        }
        myList.addFirst(job);
        notify();
    }

    public synchronized E dequeue() throws NoSuchElementException {
        while(isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new NoSuchElementException("Thread Interrupted!");
            }
        }
        E job = myList.removeLast();
        notify();
        return job;
    }

    public synchronized boolean isEmpty() {
        return myList.isEmpty();
    }

    public synchronized void stop() {
        stop = true;
        notify();
    }

    public synchronized boolean isStopped() {
        return stop;
    }

    public synchronized int size() {
        return myList.size();
    }

    public synchronized boolean isFull() {
        return myList.size() == maxSize;
    }

    @Override
    public Iterator<E> iterator() {
        return new SharedQueueIterator();
    }

    class SharedQueueIterator implements Iterator<E> {

        Iterator<E> _it = myList.iterator();

        public E next(){
            return _it.next();
        }

        public boolean hasNext(){
            return _it.hasNext();
        }
        public void remove(){
            throw new UnsupportedOperationException("Remove method not supported");
        }
    }
}
