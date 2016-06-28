package net.minecraft.server;

import com.google.common.base.Objects;

// TacoSpigot start
import java.util.Arrays;
// TacoSpigot end

public abstract class BlockState<T extends Comparable<T>> implements IBlockState<T> {

    private final Class<T> a;
    private final String b;
    // TacoSpigot start
    private static int nextId = 0;
    private final int id;

    @Override
    public int getId() {
        return id;
    }

    private static BlockState[] byId = new BlockState[0];

    public static BlockState getById(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("Negative id: " + id);
        } else if (id < byId.length) {
            return byId[id];
        } else {
            return null;
        }
    }
    // TacoSpigot end

    protected BlockState(String s, Class<T> oclass) {
        this.a = oclass;
        this.b = s;
        // TacoSpigot start
        id = nextId++;
        if (id >= byId.length) byId = Arrays.copyOf(byId, id + 1);
        byId[id] = this;
        // TacoSpigot end
    }

    public String a() {
        return this.b;
    }

    public Class<T> b() {
        return this.a;
    }

    public String toString() {
        return Objects.toStringHelper(this).add("name", this.b).add("clazz", this.a).add("values", this.c()).toString();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof BlockState)) {
            return false;
        } else {
            BlockState blockstate = (BlockState) object;

            return this.a.equals(blockstate.a) && this.b.equals(blockstate.b);
        }
    }

    public int hashCode() {
        return 31 * this.a.hashCode() + this.b.hashCode();
    }
}
