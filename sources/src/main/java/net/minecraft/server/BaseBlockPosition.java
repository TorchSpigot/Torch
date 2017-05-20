package net.minecraft.server;

import com.google.common.base.Objects;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.concurrent.Immutable;

@Immutable
public class BaseBlockPosition implements Comparable<BaseBlockPosition> {

    public static final BaseBlockPosition ZERO = new BaseBlockPosition(0, 0, 0);
    // Paper start - Make mutable and protected for MutableBlockPos and PooledBlockPos
    // Torch start
    /* protected int a;
    protected int b;
    protected int c; */
    protected AtomicInteger x;
    protected AtomicInteger y;
    protected AtomicInteger z;
    // Torch end

    public final boolean isValidLocation() {
        return x.get() >= -30000000 && z.get() >= -30000000 && x.get() < 30000000 && z.get() < 30000000 && y.get() >= 0 && y.get() < 256;
    }
    public boolean isInvalidYLocation() {
        return y.get() < 0 || y.get() >= 256;
    }
    // Paper end

    public BaseBlockPosition(int x, int y, int z) {
        this.x = new AtomicInteger(x);
        this.y = new AtomicInteger(y);
        this.z = new AtomicInteger(z);
    }

    public BaseBlockPosition(double x, double y, double z) {
        this(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof BaseBlockPosition)) {
            return false;
        } else {
            BaseBlockPosition baseblockposition = (BaseBlockPosition) object;

            return this.getX() != baseblockposition.getX() ? false : (this.getY() != baseblockposition.getY() ? false : this.getZ() == baseblockposition.getZ());
        }
    }

    @Override
    public int hashCode() {
        return (this.getY() + this.getZ() * 31) * 31 + this.getX();
    }

    public int l(BaseBlockPosition baseblockposition) {
        return this.getY() == baseblockposition.getY() ? (this.getZ() == baseblockposition.getZ() ? this.getX() - baseblockposition.getX() : this.getZ() - baseblockposition.getZ()) : this.getY() - baseblockposition.getY();
    }

    // Paper start - Only allow a single implementation
    public final int getX() {
        return x.get();
    }

    public final int getY() {
        return y.get();
    }

    public final int getZ() {
        return z.get();
    }
    // Paper end

    public BaseBlockPosition d(BaseBlockPosition baseblockposition) {
        return new BaseBlockPosition(this.getY() * baseblockposition.getZ() - this.getZ() * baseblockposition.getY(), this.getZ() * baseblockposition.getX() - this.getX() * baseblockposition.getZ(), this.getX() * baseblockposition.getY() - this.getY() * baseblockposition.getX());
    }

    public double h(int i, int j, int k) {
        double d0 = this.getX() - i;
        double d1 = this.getY() - j;
        double d2 = this.getZ() - k;

        return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    public double distanceSquared(double d0, double d1, double d2) {
        double d3 = this.getX() - d0;
        double d4 = this.getY() - d1;
        double d5 = this.getZ() - d2;

        return d3 * d3 + d4 * d4 + d5 * d5;
    }

    public double g(double d0, double d1, double d2) {
        double d3 = this.getX() + 0.5D - d0;
        double d4 = this.getY() + 0.5D - d1;
        double d5 = this.getZ() + 0.5D - d2;

        return d3 * d3 + d4 * d4 + d5 * d5;
    }

    public double n(BaseBlockPosition baseblockposition) {
        return this.distanceSquared(baseblockposition.getX(), baseblockposition.getY(), baseblockposition.getZ());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("x", this.getX()).add("y", this.getY()).add("z", this.getZ()).toString();
    }

    @Override
    public int compareTo(BaseBlockPosition object) { // Paper - decompile fix
        return this.l(object);
    }
}
