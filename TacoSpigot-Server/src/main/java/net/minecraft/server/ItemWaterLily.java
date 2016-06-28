package net.minecraft.server;

public class ItemWaterLily extends ItemWithAuxData {

    public ItemWaterLily(Block block) {
        super(block, false);
    }

    public InteractionResultWrapper<ItemStack> a(ItemStack itemstack, World world, EntityHuman entityhuman, EnumHand enumhand) {
        MovingObjectPosition movingobjectposition = this.a(world, entityhuman, true);

        if (movingobjectposition == null) {
            return new InteractionResultWrapper(EnumInteractionResult.PASS, itemstack);
        } else {
            if (movingobjectposition.type == MovingObjectPosition.EnumMovingObjectType.BLOCK) {
                BlockPosition blockposition = movingobjectposition.a();

                if (!world.a(entityhuman, blockposition) || !entityhuman.a(blockposition.shift(movingobjectposition.direction), movingobjectposition.direction, itemstack)) {
                    return new InteractionResultWrapper(EnumInteractionResult.FAIL, itemstack);
                }

                BlockPosition blockposition1 = blockposition.up();
                IBlockData iblockdata = world.getType(blockposition);

                if (iblockdata.getMaterial() == Material.WATER && ((Integer) iblockdata.get(BlockFluids.LEVEL)).intValue() == 0 && world.isEmpty(blockposition1)) {
                    // CraftBukkit start - special case for handling block placement with water lilies
                    org.bukkit.block.BlockState blockstate = org.bukkit.craftbukkit.block.CraftBlockState.getBlockState(world, blockposition1.getX(), blockposition1.getY(), blockposition1.getZ());
                    world.setTypeAndData(blockposition1, Blocks.WATERLILY.getBlockData(), 11);
                    org.bukkit.event.block.BlockPlaceEvent placeEvent = org.bukkit.craftbukkit.event.CraftEventFactory.callBlockPlaceEvent(world, entityhuman, enumhand, blockstate, blockposition.getX(), blockposition.getY(), blockposition.getZ());
                    if (placeEvent != null && (placeEvent.isCancelled() || !placeEvent.canBuild())) {
                        blockstate.update(true, false);
                        return new InteractionResultWrapper(EnumInteractionResult.PASS, itemstack);
                    }
                    // CraftBukkit end
                    if (!entityhuman.abilities.canInstantlyBuild) {
                        --itemstack.count;
                    }

                    entityhuman.b(StatisticList.b((Item) this));
                    world.a(entityhuman, blockposition, SoundEffects.gw, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    return new InteractionResultWrapper(EnumInteractionResult.SUCCESS, itemstack);
                }
            }

            return new InteractionResultWrapper(EnumInteractionResult.FAIL, itemstack);
        }
    }
}
