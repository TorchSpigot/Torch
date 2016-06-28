package net.techcable.tacospigot.function;

@FunctionalInterface
public interface ObjIntBiFunction<T, R> {
    public R apply(T t, int i);
}
