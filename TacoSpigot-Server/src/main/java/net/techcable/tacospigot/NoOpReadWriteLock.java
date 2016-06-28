package net.techcable.tacospigot;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import javax.annotation.Nonnull;

public class NoOpReadWriteLock implements ReadWriteLock {
    private NoOpReadWriteLock() {}
    public static final NoOpReadWriteLock INSTANCE = new NoOpReadWriteLock();

    @Override
    @Nonnull
    public Lock readLock() {
        return NoOpLock.INSTANCE;
    }

    @Override
    @Nonnull
    public Lock writeLock() {
        return NoOpLock.INSTANCE;
    }
}