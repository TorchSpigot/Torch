package net.minecraft.server;

import org.bukkit.event.player.PlayerFishEvent; // CraftBukkit

public class ItemFishingRod extends Item {

    public ItemFishingRod() {
        this.setMaxDurability(64);
        this.d(1);
        this.a(CreativeModeTab.i);
        this.a(new MinecraftKey("cast"), new IDynamicTexture() {
        });
    }

    public InteractionResultWrapper<ItemStack> a(ItemStack itemstack, World world, EntityHuman entityhuman, EnumHand enumhand) {
        if (entityhuman.hookedFish != null) {
            int i = entityhuman.hookedFish.j();

            itemstack.damage(i, entityhuman);
            entityhuman.a(enumhand);
        } else {
            // CraftBukkit start
            EntityFishingHook hook = new EntityFishingHook(world, entityhuman);
            PlayerFishEvent playerFishEvent = new PlayerFishEvent((org.bukkit.entity.Player) entityhuman.getBukkitEntity(), null, (org.bukkit.entity.Fish) hook.getBukkitEntity(), PlayerFishEvent.State.FISHING);
            world.getServer().getPluginManager().callEvent(playerFishEvent);

            if (playerFishEvent.isCancelled()) {
                entityhuman.hookedFish = null;
                return new InteractionResultWrapper(EnumInteractionResult.PASS, itemstack);
            }
            // CraftBukkit end
            world.a((EntityHuman) null, entityhuman.locX, entityhuman.locY, entityhuman.locZ, SoundEffects.H, SoundCategory.NEUTRAL, 0.5F, 0.4F / (ItemFishingRod.j.nextFloat() * 0.4F + 0.8F));
            if (!world.isClientSide) {
                world.addEntity(hook); // CraftBukkit - moved creation up
            }

            entityhuman.a(enumhand);
            entityhuman.b(StatisticList.b((Item) this));
        }

        return new InteractionResultWrapper(EnumInteractionResult.SUCCESS, itemstack);
    }

    public boolean g_(ItemStack itemstack) {
        return super.g_(itemstack);
    }

    public int c() {
        return 1;
    }
}
