package net.minecraft.server;

public class ItemFireball extends Item {

    public ItemFireball() {
        this.a(CreativeModeTab.f);
    }

    public EnumInteractionResult a(ItemStack itemstack, EntityHuman entityhuman, World world, BlockPosition blockposition, EnumHand enumhand, EnumDirection enumdirection, float f, float f1, float f2) {
        if (world.isClientSide) {
            return EnumInteractionResult.SUCCESS;
        } else {
            blockposition = blockposition.shift(enumdirection);
            if (!entityhuman.a(blockposition, enumdirection, itemstack)) {
                return EnumInteractionResult.FAIL;
            } else {
                if (world.getType(blockposition).getMaterial() == Material.AIR) {
                    // CraftBukkit start - fire BlockIgniteEvent
                    if (org.bukkit.craftbukkit.event.CraftEventFactory.callBlockIgniteEvent(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), org.bukkit.event.block.BlockIgniteEvent.IgniteCause.FIREBALL, entityhuman).isCancelled()) {
                        if (!entityhuman.abilities.canInstantlyBuild) {
                            --itemstack.count;
                        }
                        return EnumInteractionResult.PASS;
                    }
                    // CraftBukkit end
                    world.a((EntityHuman) null, blockposition, SoundEffects.bm, SoundCategory.BLOCKS, 1.0F, (ItemFireball.j.nextFloat() - ItemFireball.j.nextFloat()) * 0.2F + 1.0F);
                    world.setTypeUpdate(blockposition, Blocks.FIRE.getBlockData());
                }

                if (!entityhuman.abilities.canInstantlyBuild) {
                    --itemstack.count;
                }

                return EnumInteractionResult.SUCCESS;
            }
        }
    }
}
