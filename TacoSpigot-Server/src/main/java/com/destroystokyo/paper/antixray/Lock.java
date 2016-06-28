package com.destroystokyo.paper.antixray;

import java.util.concurrent.atomic.AtomicInteger;

public class Lock {
    private final AtomicInteger lockCount = new AtomicInteger();

    public void lock() {
        lockCount.incrementAndGet();
    }

    public synchronized void unlock() {
        lockCount.decrementAndGet();
        notifyAll();
    }

    public synchronized void waitUntilUnlock() {
        try {
            while (lockCount.get() > 0) {
                wait();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}