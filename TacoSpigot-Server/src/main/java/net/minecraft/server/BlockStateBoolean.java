package net.minecraft.server;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;

public class BlockStateBoolean extends BlockState<Boolean> {

    private final ImmutableSet<Boolean> a = ImmutableSet.of(Boolean.valueOf(true), Boolean.valueOf(false));

    protected BlockStateBoolean(String s) {
        super(s, Boolean.class);
    }

    public Collection<Boolean> c() {
        return this.a;
    }

    public static BlockStateBoolean of(String s) {
        return new BlockStateBoolean(s);
    }

    public String a(Boolean obool) {
        return obool.toString();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object instanceof BlockStateBoolean && super.equals(object)) {
            BlockStateBoolean blockstateboolean = (BlockStateBoolean) object;

            return this.a.equals(blockstateboolean.a);
        } else {
            return false;
        }
    }

    // TacoSpigot start
    @Override
    public int getValueId(Boolean value) {
        return value ? 1 : 0;
    }

    @Override
    public Boolean getByValueId(int id) {
        switch (id) {
            case 0:
                return false;
            case 1:
                return true;
            default:
                throw new IllegalArgumentException("Invalid id: " + id);
        }
    }
    // TacoSpigot end

    public int hashCode() {
        return 31 * super.hashCode() + this.a.hashCode();
    }
}
