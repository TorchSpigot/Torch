package net.minecraft.server;

import java.util.Iterator;
import java.util.List;

import org.bukkit.event.entity.EntityInteractEvent; // CraftBukkit

public class BlockPressurePlateBinary extends BlockPressurePlateAbstract {

    public static final BlockStateBoolean POWERED = BlockStateBoolean.of("powered");
    private final BlockPressurePlateBinary.EnumMobType e;

    protected BlockPressurePlateBinary(Material material, BlockPressurePlateBinary.EnumMobType blockpressureplatebinary_enummobtype) {
        super(material);
        this.y(this.blockStateList.getBlockData().set(BlockPressurePlateBinary.POWERED, Boolean.valueOf(false)));
        this.e = blockpressureplatebinary_enummobtype;
    }

    protected int getPower(IBlockData iblockdata) {
        return ((Boolean) iblockdata.get(BlockPressurePlateBinary.POWERED)).booleanValue() ? 15 : 0;
    }

    protected IBlockData a(IBlockData iblockdata, int i) {
        return iblockdata.set(BlockPressurePlateBinary.POWERED, Boolean.valueOf(i > 0));
    }

    protected void b(World world, BlockPosition blockposition) {
        if (this.material == Material.WOOD) {
            world.a((EntityHuman) null, blockposition, SoundEffects.ic, SoundCategory.BLOCKS, 0.3F, 0.8F);
        } else {
            world.a((EntityHuman) null, blockposition, SoundEffects.gO, SoundCategory.BLOCKS, 0.3F, 0.6F);
        }

    }

    protected void c(World world, BlockPosition blockposition) {
        if (this.material == Material.WOOD) {
            world.a((EntityHuman) null, blockposition, SoundEffects.ib, SoundCategory.BLOCKS, 0.3F, 0.7F);
        } else {
            world.a((EntityHuman) null, blockposition, SoundEffects.gN, SoundCategory.BLOCKS, 0.3F, 0.5F);
        }

    }

    protected int e(World world, BlockPosition blockposition) {
        AxisAlignedBB axisalignedbb = BlockPressurePlateBinary.c.a(blockposition);
        List list;

        switch (this.e) {
        case EVERYTHING:
            list = world.getEntities((Entity) null, axisalignedbb);
            break;

        case MOBS:
            list = world.a(EntityLiving.class, axisalignedbb);
            break;

        default:
            return 0;
        }

        if (!list.isEmpty()) {
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                Entity entity = (Entity) iterator.next();

                // CraftBukkit start - Call interact event when turning on a pressure plate
                if (this.getPower(world.getType(blockposition)) == 0) {
                    org.bukkit.World bworld = world.getWorld();
                    org.bukkit.plugin.PluginManager manager = world.getServer().getPluginManager();
                    org.bukkit.event.Cancellable cancellable;

                    if (entity instanceof EntityHuman) {
                        cancellable = org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerInteractEvent((EntityHuman) entity, org.bukkit.event.block.Action.PHYSICAL, blockposition, null, null, null);
                    } else {
                        cancellable = new EntityInteractEvent(entity.getBukkitEntity(), bworld.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()));
                        manager.callEvent((EntityInteractEvent) cancellable);
                    }

                    // We only want to block turning the plate on if all events are cancelled
                    if (cancellable.isCancelled()) {
                        continue;
                    }
                }
                // CraftBukkit end

                if (!entity.isIgnoreBlockTrigger()) {
                    return 15;
                }
            }
        }

        return 0;
    }

    public IBlockData fromLegacyData(int i) {
        return this.getBlockData().set(BlockPressurePlateBinary.POWERED, Boolean.valueOf(i == 1));
    }

    public int toLegacyData(IBlockData iblockdata) {
        return ((Boolean) iblockdata.get(BlockPressurePlateBinary.POWERED)).booleanValue() ? 1 : 0;
    }

    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[] { BlockPressurePlateBinary.POWERED});
    }

    public static enum EnumMobType {

        EVERYTHING, MOBS;

        private EnumMobType() {}
    }
}
