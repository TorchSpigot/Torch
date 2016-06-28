package net.minecraft.server;

import org.bukkit.event.entity.EntityCombustByEntityEvent; // CraftBukkit

public abstract class EntityMonster extends EntityCreature implements IMonster {

    public EntityMonster(World world) {
        super(world);
        this.b_ = 5;
    }

    public SoundCategory bA() {
        return SoundCategory.HOSTILE;
    }

    public void n() {
        this.bZ();
        float f = this.e(1.0F);

        if (f > 0.5F) {
            this.ticksFarFromPlayer += 2;
        }

        super.n();
    }

    public void m() {
        super.m();
        if (!this.world.isClientSide && this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
            this.die();
        }

    }

    protected SoundEffect aa() {
        return SoundEffects.cG;
    }

    protected SoundEffect ab() {
        return SoundEffects.cF;
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        return this.isInvulnerable(damagesource) ? false : super.damageEntity(damagesource, f);
    }

    protected SoundEffect bS() {
        return SoundEffects.cD;
    }

    protected SoundEffect bT() {
        return SoundEffects.cC;
    }

    protected SoundEffect e(int i) {
        return i > 4 ? SoundEffects.cB : SoundEffects.cE;
    }

    public boolean B(Entity entity) {
        float f = (float) this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue();
        int i = 0;

        if (entity instanceof EntityLiving) {
            f += EnchantmentManager.a(this.getItemInMainHand(), ((EntityLiving) entity).getMonsterType());
            i += EnchantmentManager.a((EntityLiving) this);
        }

        boolean flag = entity.damageEntity(DamageSource.mobAttack(this), f);

        if (flag) {
            if (i > 0 && entity instanceof EntityLiving) {
                ((EntityLiving) entity).a(this, (float) i * 0.5F, (double) MathHelper.sin(this.yaw * 0.017453292F), (double) (-MathHelper.cos(this.yaw * 0.017453292F)));
                this.motX *= 0.6D;
                this.motZ *= 0.6D;
            }

            int j = EnchantmentManager.getFireAspectEnchantmentLevel(this);

            if (j > 0) {
                // CraftBukkit start - Call a combust event when somebody hits with a fire enchanted item
                EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(this.getBukkitEntity(), entity.getBukkitEntity(), j * 4);
                org.bukkit.Bukkit.getPluginManager().callEvent(combustEvent);

                if (!combustEvent.isCancelled()) {
                    entity.setOnFire(combustEvent.getDuration());
                }
                // CraftBukkit end
            }

            if (entity instanceof EntityHuman) {
                EntityHuman entityhuman = (EntityHuman) entity;
                ItemStack itemstack = this.getItemInMainHand();
                ItemStack itemstack1 = entityhuman.ct() ? entityhuman.cw() : null;

                if (itemstack != null && itemstack1 != null && itemstack.getItem() instanceof ItemAxe && itemstack1.getItem() == Items.SHIELD) {
                    float f1 = 0.25F + (float) EnchantmentManager.getDigSpeedEnchantmentLevel(this) * 0.05F;

                    if (this.random.nextFloat() < f1) {
                        entityhuman.db().a(Items.SHIELD, 100);
                        this.world.broadcastEntityEffect(entityhuman, (byte) 30);
                    }
                }
            }

            this.a((EntityLiving) this, entity);
        }

        return flag;
    }

    public float a(BlockPosition blockposition) {
        return 0.5F - this.world.n(blockposition);
    }

    protected boolean s_() {
        BlockPosition blockposition = new BlockPosition(this.locX, this.getBoundingBox().b, this.locZ);

        if (this.world.b(EnumSkyBlock.SKY, blockposition) > this.random.nextInt(32)) {
            return false;
        } else {
            //int i = this.world.getLightLevel(blockposition); // Paper
            boolean passes; // Paper
            if (this.world.V()) {
                int j = this.world.af();

                this.world.c(10);
                passes = !world.isLightLevel(blockposition, this.random.nextInt(9)); // Paper
                this.world.c(j);
            } else { passes = !world.isLightLevel(blockposition, this.random.nextInt(9)); } // Paper

            return passes; // Paper
        }
    }

    public boolean cG() {
        return this.world.getDifficulty() != EnumDifficulty.PEACEFUL && this.s_() && super.cG();
    }

    protected void initAttributes() {
        super.initAttributes();
        this.getAttributeMap().b(GenericAttributes.ATTACK_DAMAGE);
    }

    protected boolean isDropExperience() {
        return true;
    }
}
