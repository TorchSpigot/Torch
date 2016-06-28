package net.minecraft.server;

import java.util.UUID;
import javax.annotation.Nullable;

public class ItemMonsterEgg extends Item {

    public ItemMonsterEgg() {
        this.a(CreativeModeTab.f);
    }

    public String a(ItemStack itemstack) {
        String s = ("" + LocaleI18n.get(this.getName() + ".name")).trim();
        String s1 = h(itemstack);

        if (s1 != null) {
            s = s + " " + LocaleI18n.get("entity." + s1 + ".name");
        }

        return s;
    }

    public EnumInteractionResult a(ItemStack itemstack, EntityHuman entityhuman, World world, BlockPosition blockposition, EnumHand enumhand, EnumDirection enumdirection, float f, float f1, float f2) {
        if (world.isClientSide) {
            return EnumInteractionResult.SUCCESS;
        } else if (!entityhuman.a(blockposition.shift(enumdirection), enumdirection, itemstack)) {
            return EnumInteractionResult.FAIL;
        } else {
            IBlockData iblockdata = world.getType(blockposition);

            if (iblockdata.getBlock() == Blocks.MOB_SPAWNER) {
                TileEntity tileentity = world.getTileEntity(blockposition);

                if (tileentity instanceof TileEntityMobSpawner) {
                    MobSpawnerAbstract mobspawnerabstract = ((TileEntityMobSpawner) tileentity).getSpawner();

                    mobspawnerabstract.setMobName(h(itemstack));
                    tileentity.update();
                    world.notify(blockposition, iblockdata, iblockdata, 3);
                    if (!entityhuman.abilities.canInstantlyBuild) {
                        --itemstack.count;
                    }

                    return EnumInteractionResult.SUCCESS;
                }
            }

            blockposition = blockposition.shift(enumdirection);
            double d0 = 0.0D;

            if (enumdirection == EnumDirection.UP && iblockdata instanceof BlockFence) {
                d0 = 0.5D;
            }

            Entity entity = a(world, h(itemstack), (double) blockposition.getX() + 0.5D, (double) blockposition.getY() + d0, (double) blockposition.getZ() + 0.5D);

            if (entity != null) {
                if (entity instanceof EntityLiving && itemstack.hasName()) {
                    entity.setCustomName(itemstack.getName());
                }

                a(world, entityhuman, itemstack, entity);
                if (!entityhuman.abilities.canInstantlyBuild) {
                    --itemstack.count;
                }
            }

            return EnumInteractionResult.SUCCESS;
        }
    }

    public static void a(World world, @Nullable EntityHuman entityhuman, ItemStack itemstack, @Nullable Entity entity) {
        MinecraftServer minecraftserver = world.getMinecraftServer();

        if (minecraftserver != null && entity != null) {
            NBTTagCompound nbttagcompound = itemstack.getTag();

            if (nbttagcompound != null && nbttagcompound.hasKeyOfType("EntityTag", 10)) {
                if (!world.isClientSide && entity.bs() && (entityhuman == null || !minecraftserver.getPlayerList().isOp(entityhuman.getProfile()))) {
                    return;
                }

                NBTTagCompound nbttagcompound1 = entity.e(new NBTTagCompound());
                UUID uuid = entity.getUniqueID();

                nbttagcompound1.a(nbttagcompound.getCompound("EntityTag"));
                entity.a(uuid);
                entity.f(nbttagcompound1);
            }

        }
    }

    public InteractionResultWrapper<ItemStack> a(ItemStack itemstack, World world, EntityHuman entityhuman, EnumHand enumhand) {
        if (world.isClientSide) {
            return new InteractionResultWrapper(EnumInteractionResult.PASS, itemstack);
        } else {
            MovingObjectPosition movingobjectposition = this.a(world, entityhuman, true);

            if (movingobjectposition != null && movingobjectposition.type == MovingObjectPosition.EnumMovingObjectType.BLOCK) {
                BlockPosition blockposition = movingobjectposition.a();

                if (!(world.getType(blockposition).getBlock() instanceof BlockFluids)) {
                    return new InteractionResultWrapper(EnumInteractionResult.PASS, itemstack);
                } else if (world.a(entityhuman, blockposition) && entityhuman.a(blockposition, movingobjectposition.direction, itemstack)) {
                    Entity entity = a(world, h(itemstack), (double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.5D, (double) blockposition.getZ() + 0.5D);

                    if (entity == null) {
                        return new InteractionResultWrapper(EnumInteractionResult.PASS, itemstack);
                    } else {
                        if (entity instanceof EntityLiving && itemstack.hasName()) {
                            entity.setCustomName(itemstack.getName());
                        }

                        a(world, entityhuman, itemstack, entity);
                        if (!entityhuman.abilities.canInstantlyBuild) {
                            --itemstack.count;
                        }

                        entityhuman.b(StatisticList.b((Item) this));
                        return new InteractionResultWrapper(EnumInteractionResult.SUCCESS, itemstack);
                    }
                } else {
                    return new InteractionResultWrapper(EnumInteractionResult.FAIL, itemstack);
                }
            } else {
                return new InteractionResultWrapper(EnumInteractionResult.PASS, itemstack);
            }
        }
    }

    @Nullable
    public static Entity a(World world, @Nullable String s, double d0, double d1, double d2) {        // CraftBukkit start - delegate to spawnCreature
        return spawnCreature(world, s, d0, d1, d2, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.SPAWNER_EGG);
    }

    public static Entity spawnCreature(World world, String s, double d0, double d1, double d2, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason spawnReason) {
        if (s != null && EntityTypes.eggInfo.containsKey(s)) {
            Entity entity = null;

            for (int i = 0; i < 1; ++i) {
                entity = EntityTypes.b(s, world);
                if (entity instanceof EntityLiving) {
                    EntityInsentient entityinsentient = (EntityInsentient) entity;

                    entity.setPositionRotation(d0, d1, d2, MathHelper.g(world.random.nextFloat() * 360.0F), 0.0F);
                    entityinsentient.aP = entityinsentient.yaw;
                    entityinsentient.aN = entityinsentient.yaw;
                    entityinsentient.prepare(world.D(new BlockPosition(entityinsentient)), (GroupDataEntity) null);
                    // CraftBukkit start - don't return an entity when CreatureSpawnEvent is canceled
                    if (!world.addEntity(entity, spawnReason)) {
                        entity = null;
                    } else {
                        entityinsentient.D();
                    }
                    // CraftBukkit end
                }
            }

            return entity;
        } else {
            return null;
        }
    }

    @Nullable
    public static String h(ItemStack itemstack) {
        NBTTagCompound nbttagcompound = itemstack.getTag();

        if (nbttagcompound == null) {
            return null;
        } else if (!nbttagcompound.hasKeyOfType("EntityTag", 10)) {
            return null;
        } else {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("EntityTag");

            return !nbttagcompound1.hasKeyOfType("id", 8) ? null : nbttagcompound1.getString("id");
        }
    }
}
