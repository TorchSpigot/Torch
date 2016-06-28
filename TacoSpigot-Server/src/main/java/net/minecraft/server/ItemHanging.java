package net.minecraft.server;

import javax.annotation.Nullable;
// CraftBukkit start
import org.bukkit.entity.Player;
import org.bukkit.event.hanging.HangingPlaceEvent;
// CraftBukkit end

public class ItemHanging extends Item {

    private final Class<? extends EntityHanging> a;

    public ItemHanging(Class<? extends EntityHanging> oclass) {
        this.a = oclass;
        this.a(CreativeModeTab.c);
    }

    public EnumInteractionResult a(ItemStack itemstack, EntityHuman entityhuman, World world, BlockPosition blockposition, EnumHand enumhand, EnumDirection enumdirection, float f, float f1, float f2) {
        BlockPosition blockposition1 = blockposition.shift(enumdirection);

        if (enumdirection != EnumDirection.DOWN && enumdirection != EnumDirection.UP && entityhuman.a(blockposition1, enumdirection, itemstack)) {
            EntityHanging entityhanging = this.a(world, blockposition1, enumdirection);

            if (entityhanging != null && entityhanging.survives()) {
                if (!world.isClientSide) {
                    // CraftBukkit start - fire HangingPlaceEvent
                    Player who = (entityhuman == null) ? null : (Player) entityhuman.getBukkitEntity();
                    org.bukkit.block.Block blockClicked = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
                    org.bukkit.block.BlockFace blockFace = org.bukkit.craftbukkit.block.CraftBlock.notchToBlockFace(enumdirection);

                    HangingPlaceEvent event = new HangingPlaceEvent((org.bukkit.entity.Hanging) entityhanging.getBukkitEntity(), who, blockClicked, blockFace);
                    world.getServer().getPluginManager().callEvent(event);

                    if (event.isCancelled()) {
                        return EnumInteractionResult.FAIL;
                    }
                    // CraftBukkit end
                    entityhanging.o();
                    world.addEntity(entityhanging);
                }

                --itemstack.count;
            }

            return EnumInteractionResult.SUCCESS;
        } else {
            return EnumInteractionResult.FAIL;
        }
    }

    @Nullable
    private EntityHanging a(World world, BlockPosition blockposition, EnumDirection enumdirection) {
        return (EntityHanging) (this.a == EntityPainting.class ? new EntityPainting(world, blockposition, enumdirection) : (this.a == EntityItemFrame.class ? new EntityItemFrame(world, blockposition, enumdirection) : null));
    }
}
