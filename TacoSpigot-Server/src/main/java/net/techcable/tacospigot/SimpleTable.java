package net.techcable.tacospigot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@FunctionalInterface
public interface SimpleTable<R, C, V> {
    @Nullable
    public V get(@Nonnull R row, @Nonnull C column);
}