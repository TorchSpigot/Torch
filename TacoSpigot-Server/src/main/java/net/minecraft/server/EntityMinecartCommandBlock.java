package net.minecraft.server;

import javax.annotation.Nullable;

public class EntityMinecartCommandBlock extends EntityMinecartAbstract {

    private static final DataWatcherObject<String> a = DataWatcher.a(EntityMinecartCommandBlock.class, DataWatcherRegistry.d);
    private static final DataWatcherObject<IChatBaseComponent> b = DataWatcher.a(EntityMinecartCommandBlock.class, DataWatcherRegistry.e);
    private final CommandBlockListenerAbstract c = new CommandBlockListenerAbstract() {
        {
            this.sender = (org.bukkit.craftbukkit.entity.CraftMinecartCommand) EntityMinecartCommandBlock.this.getBukkitEntity(); // CraftBukkit - Set the sender
        }
        public void i() {
            EntityMinecartCommandBlock.this.getDataWatcher().set(EntityMinecartCommandBlock.a, this.getCommand());
            EntityMinecartCommandBlock.this.getDataWatcher().set(EntityMinecartCommandBlock.b, this.l());
        }

        public BlockPosition getChunkCoordinates() {
            return new BlockPosition(EntityMinecartCommandBlock.this.locX, EntityMinecartCommandBlock.this.locY + 0.5D, EntityMinecartCommandBlock.this.locZ);
        }

        public Vec3D d() {
            return new Vec3D(EntityMinecartCommandBlock.this.locX, EntityMinecartCommandBlock.this.locY, EntityMinecartCommandBlock.this.locZ);
        }

        public World getWorld() {
            return EntityMinecartCommandBlock.this.world;
        }

        public Entity f() {
            return EntityMinecartCommandBlock.this;
        }

        public MinecraftServer h() {
            return EntityMinecartCommandBlock.this.world.getMinecraftServer();
        }
    };
    private int d = 0;

    public EntityMinecartCommandBlock(World world) {
        super(world);
    }

    public EntityMinecartCommandBlock(World world, double d0, double d1, double d2) {
        super(world, d0, d1, d2);
    }

    protected void i() {
        super.i();
        this.getDataWatcher().register(EntityMinecartCommandBlock.a, "");
        this.getDataWatcher().register(EntityMinecartCommandBlock.b, new ChatComponentText(""));
    }

    protected void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.c.b(nbttagcompound);
        this.getDataWatcher().set(EntityMinecartCommandBlock.a, this.getCommandBlock().getCommand());
        this.getDataWatcher().set(EntityMinecartCommandBlock.b, this.getCommandBlock().l());
    }

    protected void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        this.c.a(nbttagcompound);
    }

    public EntityMinecartAbstract.EnumMinecartType v() {
        return EntityMinecartAbstract.EnumMinecartType.COMMAND_BLOCK;
    }

    public IBlockData x() {
        return Blocks.COMMAND_BLOCK.getBlockData();
    }

    public CommandBlockListenerAbstract getCommandBlock() {
        return this.c;
    }

    public void a(int i, int j, int k, boolean flag) {
        if (flag && this.ticksLived - this.d >= 4) {
            this.getCommandBlock().a(this.world);
            this.d = this.ticksLived;
        }

    }

    public boolean a(EntityHuman entityhuman, @Nullable ItemStack itemstack, EnumHand enumhand) {
        this.c.a(entityhuman);
        return false;
    }

    public void a(DataWatcherObject<?> datawatcherobject) {
        super.a(datawatcherobject);
        if (EntityMinecartCommandBlock.b.equals(datawatcherobject)) {
            try {
                this.c.b((IChatBaseComponent) this.getDataWatcher().get(EntityMinecartCommandBlock.b));
            } catch (Throwable throwable) {
                ;
            }
        } else if (EntityMinecartCommandBlock.a.equals(datawatcherobject)) {
            this.c.setCommand((String) this.getDataWatcher().get(EntityMinecartCommandBlock.a));
        }

    }

    public boolean bs() {
        return true;
    }
}
