package net.techcable.tacospigot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@FunctionalInterface
public interface SimpleMap<K, V> {
    @Nullable
    public V get(@Nonnull K key);
}
