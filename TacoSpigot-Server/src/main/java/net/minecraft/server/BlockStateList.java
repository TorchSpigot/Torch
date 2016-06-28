package net.minecraft.server;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

// TacoSpigot start
import net.techcable.tacospigot.ImmutableArrayMap;
import net.techcable.tacospigot.ImmutableArrayTable;
import net.techcable.tacospigot.SimpleMap;
import net.techcable.tacospigot.SimpleTable;
import net.techcable.tacospigot.TacoSpigotConfig;

import net.techcable.tacospigot.ImmutableArrayMap;
import net.techcable.tacospigot.ImmutableArrayTable;
import net.techcable.tacospigot.TacoSpigotConfig;
// TacoSpigot end

public class BlockStateList {

    private static final Pattern a = Pattern.compile("^[a-z0-9_]+$");
    private static final Function<IBlockState<?>, String> b = new Function() {
        @Nullable
        public String a(@Nullable IBlockState<?> iblockstate) {
            return iblockstate == null ? "<NULL>" : iblockstate.a();
        }

        public Object apply(Object object) {
            return this.a((IBlockState) object);
        }
    };
    private final Block c;
    private final ImmutableSortedMap<String, IBlockState<?>> d;
    private final ImmutableList<IBlockData> e;

    public BlockStateList(Block block, IBlockState<?>... aiblockstate) {
        this.c = block;
        HashMap hashmap = Maps.newHashMap();
        IBlockState[] aiblockstate1 = aiblockstate;
        int i = aiblockstate.length;

        for (int j = 0; j < i; ++j) {
            IBlockState iblockstate = aiblockstate1[j];

            a(block, iblockstate);
            hashmap.put(iblockstate.a(), iblockstate);
        }

        this.d = ImmutableSortedMap.copyOf(hashmap);
        LinkedHashMap linkedhashmap = Maps.newLinkedHashMap();
        ArrayList arraylist = Lists.newArrayList();
        Iterable iterable = IteratorUtils.a(this.e());
        Iterator iterator = iterable.iterator();

        while (iterator.hasNext()) {
            List list = (List) iterator.next();
            Map map = MapGeneratorUtils.b(this.d.values(), list);
            BlockStateList.BlockData blockstatelist_blockdata = new BlockStateList.BlockData(block, ImmutableMap.copyOf(map), null);

            linkedhashmap.put(map, blockstatelist_blockdata);
            arraylist.add(blockstatelist_blockdata);
        }

        iterator = arraylist.iterator();

        while (iterator.hasNext()) {
            BlockStateList.BlockData blockstatelist_blockdata1 = (BlockStateList.BlockData) iterator.next();

            blockstatelist_blockdata1.a((Map) linkedhashmap);
        }

        this.e = ImmutableList.copyOf(arraylist);
    }

    public static <T extends Comparable<T>> String a(Block block, IBlockState<T> iblockstate) {
        String s = iblockstate.a();

        if (!BlockStateList.a.matcher(s).matches()) {
            throw new IllegalArgumentException("Block: " + block.getClass() + " has invalidly named property: " + s);
        } else {
            Iterator<T> iterator = iblockstate.c().iterator(); // TacoSpigot - generic iterator

            String s1;

            do {
                if (!iterator.hasNext()) {
                    return s;
                }

                T comparable = iterator.next(); // TacoSpigot - fix fernflower error

                s1 = iblockstate.a(comparable);
            } while (BlockStateList.a.matcher(s1).matches());

            throw new IllegalArgumentException("Block: " + block.getClass() + " has property: " + s + " with invalidly named value: " + s1);
        }
    }

    public ImmutableList<IBlockData> a() {
        return this.e;
    }

    private List<Iterable<Comparable<?>>> e() {
        ArrayList arraylist = Lists.newArrayList();
        ImmutableCollection immutablecollection = this.d.values();
        Iterator iterator = immutablecollection.iterator();

        while (iterator.hasNext()) {
            IBlockState iblockstate = (IBlockState) iterator.next();

            arraylist.add(iblockstate.c());
        }

        return arraylist;
    }

    public IBlockData getBlockData() {
        return (IBlockData) this.e.get(0);
    }

    public Block getBlock() {
        return this.c;
    }

    public Collection<IBlockState<?>> d() {
        return this.d.values();
    }

    public String toString() {
        return Objects.toStringHelper(this).add("block", Block.REGISTRY.b(this.c)).add("properties", Iterables.transform(this.d.values(), BlockStateList.b)).toString();
    }

    static class BlockData extends BlockDataAbstract {

        private final Block a;
        // TacoSpigot start
        private final ImmutableMap<IBlockState<?>, Comparable<?>> bAsImmutableMap;
        private final SimpleMap<IBlockState<?>, Comparable<?>> b;
        private SimpleTable<IBlockState, Comparable, IBlockData> c;
        // TacoSpigot end

        private BlockData(Block block, ImmutableMap<IBlockState<?>, Comparable<?>> immutablemap) {
            this.a = block;
            // TacoSpigot start
            this.bAsImmutableMap = immutablemap;
            if (TacoSpigotConfig.useArraysForBlockStates) {
                ImmutableArrayMap<IBlockState, Comparable> arrayMap = new ImmutableArrayMap<IBlockState, Comparable>(IBlockState::getId, BlockState::getById, (ImmutableMap) immutablemap);
                b = (key) -> arrayMap.get(key.getId());
            } else {
                b = immutablemap::get;
            }
            // TacoSpigot end
        }

        public Collection<IBlockState<?>> r() {
            return Collections.unmodifiableCollection(this.bAsImmutableMap.keySet()); // TacoSpigot - use bAsImmutableMap
        }

        public <T extends Comparable<T>> T get(IBlockState<T> iblockstate) {
            Comparable comparable = (Comparable) this.b.get(iblockstate);

            if (comparable == null) {
                throw new IllegalArgumentException("Cannot get property " + iblockstate + " as it does not exist in " + this.a.t());
            } else {
                return iblockstate.b().cast(comparable); // TacoSpigot - fix fernflower error
            }
        }

        public <T extends Comparable<T>, V extends T> IBlockData set(IBlockState<T> iblockstate, V v0) {
            Comparable comparable = (Comparable) this.b.get(iblockstate);

            if (comparable == null) {
                throw new IllegalArgumentException("Cannot set property " + iblockstate + " as it does not exist in " + this.a.t());
            } else if (comparable == v0) {
                return this;
            } else {
                IBlockData iblockdata = (IBlockData) this.c.get(iblockstate, v0);

                if (iblockdata == null) {
                    throw new IllegalArgumentException("Cannot set property " + iblockstate + " to " + v0 + " on block " + Block.REGISTRY.b(this.a) + ", it is not an allowed value");
                } else {
                    return iblockdata;
                }
            }
        }

        public ImmutableMap<IBlockState<?>, Comparable<?>> s() {
            return this.bAsImmutableMap; // TacoSpigot
        }

        public Block getBlock() {
            return this.a;
        }

        public boolean equals(Object object) {
            return this == object;
        }

        public int hashCode() {
            return this.b.hashCode();
        }

        public void a(Map<Map<IBlockState<?>, Comparable<?>>, BlockStateList.BlockData> map) {
            if (this.c != null) {
                throw new IllegalStateException();
            } else {
                HashBasedTable hashbasedtable = HashBasedTable.create();
                Iterator iterator = this.bAsImmutableMap.entrySet().iterator(); // TacoSpigot - use bAsImmutableMap

                while (iterator.hasNext()) {
                    Entry entry = (Entry) iterator.next();
                    IBlockState iblockstate = (IBlockState) entry.getKey();
                    Iterator iterator1 = iblockstate.c().iterator();

                    while (iterator1.hasNext()) {
                        Comparable comparable = (Comparable) iterator1.next();

                        if (true) { // TacoSpigot - include everything in the table
                            hashbasedtable.put(iblockstate, comparable, map.get(this.b(iblockstate, comparable)));
                        }
                    }
                }

                // TacoSpigot start
              if (TacoSpigotConfig.useArraysForBlockStates) {
                  // I had some 'fun' getting this to work >:(
                  ImmutableArrayTable<IBlockState, Comparable, IBlockData> arrayTable = new ImmutableArrayTable<IBlockState, Comparable, IBlockData> (
                          IBlockState::getId,
                          BlockState::getById,
                          IBlockState::getValueId,
                          IBlockState::getByValueId,
                          hashbasedtable
                  );
                  this.c = (row, column) -> arrayTable.get(row.getId(), row.getValueId(column));
              } else {
                  ImmutableTable<IBlockState, Comparable, IBlockData> immutableTable = ImmutableTable.copyOf(hashbasedtable);
                  this.c = immutableTable::get;
              }
              // TacoSpigot end
            }
        }

        private Map<IBlockState<?>, Comparable<?>> b(IBlockState<?> iblockstate, Comparable<?> comparable) {
            HashMap hashmap = Maps.newHashMap(this.bAsImmutableMap); // TacoSpigot - use 'bAsImmutableMap'


            hashmap.put(iblockstate, comparable);
            return hashmap;
        }

        public Material getMaterial() {
            return this.a.q(this);
        }

        public boolean b() {
            return this.a.l(this);
        }

        public int c() {
            return this.a.m(this);
        }

        public int d() {
            return this.a.o(this);
        }

        public boolean f() {
            return this.a.p(this);
        }

        public MaterialMapColor g() {
            return this.a.r(this);
        }

        public IBlockData a(EnumBlockRotation enumblockrotation) {
            return this.a.a((IBlockData) this, enumblockrotation);
        }

        public IBlockData a(EnumBlockMirror enumblockmirror) {
            return this.a.a((IBlockData) this, enumblockmirror);
        }

        public boolean h() {
            return this.a.c((IBlockData) this);
        }

        public EnumRenderType i() {
            return this.a.a((IBlockData) this);
        }

        public boolean k() {
            return this.a.s(this);
        }

        public boolean l() {
            return this.a.isOccluding(this);
        }

        public boolean m() {
            return this.a.isPowerSource(this);
        }

        public int a(IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
            return this.a.b((IBlockData) this, iblockaccess, blockposition, enumdirection);
        }

        public boolean n() {
            return this.a.isComplexRedstone(this);
        }

        public int a(World world, BlockPosition blockposition) {
            return this.a.d(this, world, blockposition);
        }

        public float b(World world, BlockPosition blockposition) {
            return this.a.b(this, world, blockposition);
        }

        public float a(EntityHuman entityhuman, World world, BlockPosition blockposition) {
            return this.a.getDamage(this, entityhuman, world, blockposition);
        }

        public int b(IBlockAccess iblockaccess, BlockPosition blockposition, EnumDirection enumdirection) {
            return this.a.c(this, iblockaccess, blockposition, enumdirection);
        }

        public EnumPistonReaction o() {
            return this.a.h(this);
        }

        public IBlockData b(IBlockAccess iblockaccess, BlockPosition blockposition) {
            return this.a.updateState(this, iblockaccess, blockposition);
        }

        public boolean p() {
            return this.a.b((IBlockData) this);
        }

        @Nullable
        public AxisAlignedBB d(World world, BlockPosition blockposition) {
            return this.a.a((IBlockData) this, world, blockposition);
        }

        public void a(World world, BlockPosition blockposition, AxisAlignedBB axisalignedbb, List<AxisAlignedBB> list, @Nullable Entity entity) {
            this.a.a((IBlockData) this, world, blockposition, axisalignedbb, list, entity);
        }

        public AxisAlignedBB c(IBlockAccess iblockaccess, BlockPosition blockposition) {
            return this.a.a((IBlockData) this, iblockaccess, blockposition);
        }

        public MovingObjectPosition a(World world, BlockPosition blockposition, Vec3D vec3d, Vec3D vec3d1) {
            return this.a.a(this, world, blockposition, vec3d, vec3d1);
        }

        public boolean q() {
            return this.a.k(this);
        }

        public boolean a(World world, BlockPosition blockposition, int i, int j) {
            return this.a.a(this, world, blockposition, i, j);
        }

        public void doPhysics(World world, BlockPosition blockposition, Block block) {
            this.a.a((IBlockData) this, world, blockposition, block);
        }

        BlockData(Block block, ImmutableMap immutablemap, Object object) {
            this(block, immutablemap);
        }
    }
}
