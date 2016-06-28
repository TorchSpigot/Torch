package net.minecraft.server;

import javax.annotation.Nullable;

public class ItemBlock extends Item {

    protected final Block a;

    public ItemBlock(Block block) {
        this.a = block;
    }

    public ItemBlock b(String s) {
        super.c(s);
        return this;
    }

    public EnumInteractionResult a(ItemStack itemstack, EntityHuman entityhuman, World world, BlockPosition blockposition, EnumHand enumhand, EnumDirection enumdirection, float f, float f1, float f2) {
        IBlockData iblockdata = world.getType(blockposition);
        Block block = iblockdata.getBlock();

        if (!block.a((IBlockAccess) world, blockposition)) {
            blockposition = blockposition.shift(enumdirection);
        }

        if (itemstack.count != 0 && entityhuman.a(blockposition, enumdirection, itemstack) && world.a(this.a, blockposition, false, enumdirection, entityhuman, itemstack)) { // Paper - Pass entityhuman instead of null
            int i = this.filterData(itemstack.getData());
            IBlockData iblockdata1 = this.a.getPlacedState(world, blockposition, enumdirection, f, f1, f2, i, entityhuman);

            if (world.setTypeAndData(blockposition, iblockdata1, 11)) {
                iblockdata1 = world.getType(blockposition);
                if (iblockdata1.getBlock() == this.a) {
                    a(world, entityhuman, blockposition, itemstack);
                    this.a.postPlace(world, blockposition, iblockdata1, entityhuman, itemstack);
                }

                SoundEffectType soundeffecttype = this.a.w();

                world.a(entityhuman, blockposition, soundeffecttype.e(), SoundCategory.BLOCKS, (soundeffecttype.a() + 1.0F) / 2.0F, soundeffecttype.b() * 0.8F);
                --itemstack.count;
            }

            return EnumInteractionResult.SUCCESS;
        } else {
            return EnumInteractionResult.FAIL;
        }
    }

    public static boolean a(World world, @Nullable EntityHuman entityhuman, BlockPosition blockposition, ItemStack itemstack) {
        MinecraftServer minecraftserver = world.getMinecraftServer();

        if (minecraftserver == null) {
            return false;
        } else {
            if (itemstack.hasTag() && itemstack.getTag().hasKeyOfType("BlockEntityTag", 10)) {
                TileEntity tileentity = world.getTileEntity(blockposition);

                if (tileentity != null) {
                    if (!world.isClientSide && tileentity.isFilteredNBT() && (entityhuman == null || !minecraftserver.getPlayerList().isOp(entityhuman.getProfile()))) {
                        return false;
                    }

                    NBTTagCompound nbttagcompound = tileentity.save(new NBTTagCompound());
                    NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttagcompound.clone();
                    NBTTagCompound nbttagcompound2 = (NBTTagCompound) itemstack.getTag().get("BlockEntityTag");

                    nbttagcompound.a(nbttagcompound2);
                    nbttagcompound.setInt("x", blockposition.getX());
                    nbttagcompound.setInt("y", blockposition.getY());
                    nbttagcompound.setInt("z", blockposition.getZ());
                    if (!nbttagcompound.equals(nbttagcompound1)) {
                        tileentity.a(nbttagcompound);
                        tileentity.update();
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public String f_(ItemStack itemstack) {
        return this.a.a();
    }

    public String getName() {
        return this.a.a();
    }

    public Block d() {
        return this.a;
    }

    public Item c(String s) {
        return this.b(s);
    }
}
