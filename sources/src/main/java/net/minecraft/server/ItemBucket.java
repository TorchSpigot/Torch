package net.minecraft.server;

import javax.annotation.Nullable;
// CraftBukkit start
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
// CraftBukkit end

public class ItemBucket extends Item {

    private final Block a;

    public ItemBucket(Block block) {
        this.maxStackSize = 1;
        this.a = block;
        this.a(CreativeModeTab.f);
    }

    public InteractionResultWrapper<ItemStack> a(World world, EntityHuman entityhuman, EnumHand enumhand) {
        boolean flag = this.a == Blocks.AIR;
        ItemStack itemstack = entityhuman.b(enumhand);
        MovingObjectPosition movingobjectposition = this.a(world, entityhuman, flag);

        if (movingobjectposition == null) {
            return new InteractionResultWrapper(EnumInteractionResult.PASS, itemstack);
        } else if (movingobjectposition.type != MovingObjectPosition.EnumMovingObjectType.BLOCK) {
            return new InteractionResultWrapper(EnumInteractionResult.PASS, itemstack);
        } else {
            BlockPosition blockposition = movingobjectposition.a();

            if (!world.a(entityhuman, blockposition)) {
                return new InteractionResultWrapper(EnumInteractionResult.FAIL, itemstack);
            } else if (flag) {
                if (!entityhuman.a(blockposition.shift(movingobjectposition.direction), movingobjectposition.direction, itemstack)) {
                    return new InteractionResultWrapper(EnumInteractionResult.FAIL, itemstack);
                } else {
                    IBlockData iblockdata = world.getType(blockposition);
                    Material material = iblockdata.getMaterial();

                    if (material == Material.WATER && ((Integer) iblockdata.get(BlockFluids.LEVEL)).intValue() == 0) {
                        // CraftBukkit start
                        PlayerBucketFillEvent event = CraftEventFactory.callPlayerBucketFillEvent(entityhuman, blockposition.getX(), blockposition.getY(), blockposition.getZ(), null, itemstack, Items.WATER_BUCKET);
 
                        if (event.isCancelled()) {
                            return new InteractionResultWrapper(EnumInteractionResult.FAIL, itemstack);
                        }
                        // CraftBukkit end
                        world.setTypeAndData(blockposition, Blocks.AIR.getBlockData(), 11);
                        entityhuman.b(StatisticList.b((Item) this));
                        entityhuman.a(SoundEffects.P, 1.0F, 1.0F);
                        return new InteractionResultWrapper(EnumInteractionResult.SUCCESS, this.a(itemstack, entityhuman, Items.WATER_BUCKET, event.getItemStack())); // CraftBukkit
                    } else if (material == Material.LAVA && ((Integer) iblockdata.get(BlockFluids.LEVEL)).intValue() == 0) {
                        // CraftBukkit start
                        PlayerBucketFillEvent event = CraftEventFactory.callPlayerBucketFillEvent(entityhuman, blockposition.getX(), blockposition.getY(), blockposition.getZ(), null, itemstack, Items.LAVA_BUCKET);

                        if (event.isCancelled()) {
                            return new InteractionResultWrapper(EnumInteractionResult.FAIL, itemstack);
                        }
                        // CraftBukkit end
                        entityhuman.a(SoundEffects.Q, 1.0F, 1.0F);
                        world.setTypeAndData(blockposition, Blocks.AIR.getBlockData(), 11);
                        entityhuman.b(StatisticList.b((Item) this));
                        return new InteractionResultWrapper(EnumInteractionResult.SUCCESS, this.a(itemstack, entityhuman, Items.LAVA_BUCKET, event.getItemStack())); // CraftBukkit
                    } else {
                        return new InteractionResultWrapper(EnumInteractionResult.FAIL, itemstack);
                    }
                }
            } else {
                boolean flag1 = world.getType(blockposition).getBlock().a((IBlockAccess) world, blockposition);
                BlockPosition blockposition1 = flag1 && movingobjectposition.direction == EnumDirection.UP ? blockposition : blockposition.shift(movingobjectposition.direction);

                if (!entityhuman.a(blockposition1, movingobjectposition.direction, itemstack)) {
                    return new InteractionResultWrapper(EnumInteractionResult.FAIL, itemstack);
                } else if (this.a(entityhuman, world, blockposition1, movingobjectposition.direction, blockposition, itemstack)) { // CraftBukkit
                    entityhuman.b(StatisticList.b((Item) this));
                    return !entityhuman.abilities.canInstantlyBuild ? new InteractionResultWrapper(EnumInteractionResult.SUCCESS, new ItemStack(Items.BUCKET)) : new InteractionResultWrapper(EnumInteractionResult.SUCCESS, itemstack);
                } else {
                    return new InteractionResultWrapper(EnumInteractionResult.FAIL, itemstack);
                }
            }
        }
    }

    // CraftBukkit - added ob.ItemStack result - TODO: Is this... the right way to handle this?
    private ItemStack a(ItemStack itemstack, EntityHuman entityhuman, Item item, org.bukkit.inventory.ItemStack result) {
        if (entityhuman.abilities.canInstantlyBuild) {
            return itemstack;
        } else {
            itemstack.subtract(1);
            if (itemstack.isEmpty()) {
                // CraftBukkit start
                return CraftItemStack.asNMSCopy(result);
            } else {
                if (!entityhuman.inventory.pickup(CraftItemStack.asNMSCopy(result))) {
                    entityhuman.drop(CraftItemStack.asNMSCopy(result), false);
                    // CraftBukkit end
                }

                return itemstack;
            }
        }
    }

    // CraftBukkit start
    public boolean a(@Nullable EntityHuman entityhuman, World world, BlockPosition blockposition) {
        return a(entityhuman, world, blockposition, null, blockposition, null);
    }

    public boolean a(EntityHuman entityhuman, World world, BlockPosition blockposition, EnumDirection enumdirection, BlockPosition clicked, ItemStack itemstack) {
        // CraftBukkit end
        if (this.a == Blocks.AIR) {
            return false;
        } else {
            IBlockData iblockdata = world.getType(blockposition);
            Material material = iblockdata.getMaterial();
            boolean flag = !material.isBuildable();
            boolean flag1 = iblockdata.getBlock().a((IBlockAccess) world, blockposition);

            if (!world.isEmpty(blockposition) && !flag && !flag1) {
                return false;
            } else {
                // CraftBukkit start
                if (entityhuman != null) {
                    PlayerBucketEmptyEvent event = CraftEventFactory.callPlayerBucketEmptyEvent(entityhuman, clicked.getX(), clicked.getY(), clicked.getZ(), enumdirection, itemstack);
                    if (event.isCancelled()) {
                        // TODO: inventory not updated
                        return false;
                    }
                }
                // CraftBukkit end
                if (world.worldProvider.l() && this.a == Blocks.FLOWING_WATER) {
                    int i = blockposition.getX();
                    int j = blockposition.getY();
                    int k = blockposition.getZ();

                    world.a(entityhuman, blockposition, SoundEffects.bH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

                    for (int l = 0; l < 8; ++l) {
                        world.addParticle(EnumParticle.SMOKE_LARGE, (double) i + Math.random(), (double) j + Math.random(), (double) k + Math.random(), 0.0D, 0.0D, 0.0D, new int[0]);
                    }
                } else {
                    if (!world.isClientSide && (flag || flag1) && !material.isLiquid()) {
                        world.setAir(blockposition, true);
                    }

                    SoundEffect soundeffect = this.a == Blocks.FLOWING_LAVA ? SoundEffects.O : SoundEffects.N;

                    world.a(entityhuman, blockposition, soundeffect, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    world.setTypeAndData(blockposition, this.a.getBlockData(), 11);
                }

                return true;
            }
        }
    }
}
