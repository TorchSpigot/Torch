package net.minecraft.server;

import java.util.Iterator;
import java.util.List;

import org.bukkit.event.hanging.HangingPlaceEvent; // CraftBukkit

public class ItemLeash extends Item {

    public ItemLeash() {
        this.a(CreativeModeTab.i);
    }

    public EnumInteractionResult a(ItemStack itemstack, EntityHuman entityhuman, World world, BlockPosition blockposition, EnumHand enumhand, EnumDirection enumdirection, float f, float f1, float f2) {
        Block block = world.getType(blockposition).getBlock();

        if (!(block instanceof BlockFence)) {
            return EnumInteractionResult.PASS;
        } else {
            if (!world.isClientSide) {
                a(entityhuman, world, blockposition);
            }

            return EnumInteractionResult.SUCCESS;
        }
    }

    public static boolean a(EntityHuman entityhuman, World world, BlockPosition blockposition) {
        EntityLeash entityleash = EntityLeash.b(world, blockposition);
        boolean flag = false;
        double d0 = 7.0D;
        int i = blockposition.getX();
        int j = blockposition.getY();
        int k = blockposition.getZ();
        List list = world.a(EntityInsentient.class, new AxisAlignedBB((double) i - d0, (double) j - d0, (double) k - d0, (double) i + d0, (double) j + d0, (double) k + d0));
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            EntityInsentient entityinsentient = (EntityInsentient) iterator.next();

            if (entityinsentient.isLeashed() && entityinsentient.getLeashHolder() == entityhuman) {
                if (entityleash == null) {
                    entityleash = EntityLeash.a(world, blockposition);

                    // CraftBukkit start - fire HangingPlaceEvent
                    HangingPlaceEvent event = new HangingPlaceEvent((org.bukkit.entity.Hanging) entityleash.getBukkitEntity(), entityhuman != null ? (org.bukkit.entity.Player) entityhuman.getBukkitEntity() : null, world.getWorld().getBlockAt(i, j, k), org.bukkit.block.BlockFace.SELF);
                    world.getServer().getPluginManager().callEvent(event);

                    if (event.isCancelled()) {
                        entityleash.die();
                        return false;
                    }
                    // CraftBukkit end
                }

                // CraftBukkit start
                if (org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerLeashEntityEvent(entityinsentient, entityleash, entityhuman).isCancelled()) {
                    continue;
                }
                // CraftBukkit end

                entityinsentient.setLeashHolder(entityleash, true);
                flag = true;
            }
        }

        return flag;
    }
}
