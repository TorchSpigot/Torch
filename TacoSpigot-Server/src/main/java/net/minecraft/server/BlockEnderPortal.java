package net.minecraft.server;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;

import org.bukkit.event.entity.EntityPortalEnterEvent; // CraftBukkit

public class BlockEnderPortal extends BlockTileEntity {

    protected static final AxisAlignedBB a = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D);

    protected BlockEnderPortal(Material material) {
        super(material);
        this.a(1.0F);
    }

    public TileEntity a(World world, int i) {
        return new TileEntityEnderPortal();
    }

    public AxisAlignedBB a(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return BlockEnderPortal.a;
    }

    public void a(IBlockData iblockdata, World world, BlockPosition blockposition, AxisAlignedBB axisalignedbb, List<AxisAlignedBB> list, @Nullable Entity entity) {}

    public boolean b(IBlockData iblockdata) {
        return false;
    }

    public boolean c(IBlockData iblockdata) {
        return false;
    }

    public int a(Random random) {
        return 0;
    }

    public void a(World world, BlockPosition blockposition, IBlockData iblockdata, Entity entity) {
        if (!entity.isPassenger() && !entity.isVehicle() && entity.aV() && !world.isClientSide && entity.getBoundingBox().b(iblockdata.c(world, blockposition).a(blockposition))) {
            // CraftBukkit start - Entity in portal
            EntityPortalEnterEvent event = new EntityPortalEnterEvent(entity.getBukkitEntity(), new org.bukkit.Location(world.getWorld(), blockposition.getX(), blockposition.getY(), blockposition.getZ()));
            world.getServer().getPluginManager().callEvent(event);
            // CraftBukkit end
            entity.c(1);
        }

    }

    @Nullable
    public ItemStack a(World world, BlockPosition blockposition, IBlockData iblockdata) {
        return null;
    }

    public MaterialMapColor r(IBlockData iblockdata) {
        return MaterialMapColor.E;
    }
}
