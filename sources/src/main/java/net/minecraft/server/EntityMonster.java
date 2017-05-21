package net.minecraft.server;

import org.bukkit.event.entity.EntityCombustByEntityEvent; // CraftBukkit

public abstract class EntityMonster extends EntityCreature implements IMonster {

    public EntityMonster(World world) {
        super(world);
        this.b_ = 5;
    }

    @Override
	public SoundCategory bC() {
        return SoundCategory.HOSTILE;
    }

    @Override
	public void n() {
        this.cd();
        float f = this.e(1.0F);

        if (f > 0.5F) {
            this.ticksFarFromPlayer += 2;
        }

        super.n();
    }

    @Override
	public void A_() {
        super.A_();
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
            this.die();
        }

    }

    @Override
	protected SoundEffect aa() {
        return SoundEffects.cR;
    }

    @Override
	protected SoundEffect ab() {
        return SoundEffects.cQ;
    }

    @Override
	public boolean damageEntity(DamageSource damagesource, float f) {
        return this.isInvulnerable(damagesource) ? false : super.damageEntity(damagesource, f);
    }

    @Override
	protected SoundEffect bW() {
        return SoundEffects.cO;
    }

    @Override
	protected SoundEffect bX() {
        return SoundEffects.cN;
    }

    @Override
	protected SoundEffect e(int i) {
        return i > 4 ? SoundEffects.cM : SoundEffects.cP;
    }

    @Override
	public boolean B(Entity entity) {
        float f = (float) this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue();
        int i = 0;

        if (entity instanceof EntityLiving) {
            f += EnchantmentManager.a(this.getItemInMainHand(), ((EntityLiving) entity).getMonsterType());
            i += EnchantmentManager.b(this);
        }

        boolean flag = entity.damageEntity(DamageSource.mobAttack(this), f);

        if (flag) {
            if (i > 0 && entity instanceof EntityLiving) {
                ((EntityLiving) entity).a(this, i * 0.5F, MathHelper.sin(this.yaw * 0.017453292F), (-MathHelper.cos(this.yaw * 0.017453292F)));
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
                ItemStack itemstack1 = entityhuman.isHandRaised() ? entityhuman.cB() : ItemStack.a;

                if (!itemstack.isEmpty() && !itemstack1.isEmpty() && itemstack.getItem() instanceof ItemAxe && itemstack1.getItem() == Items.SHIELD) {
                    float f1 = 0.25F + EnchantmentManager.getDigSpeedEnchantmentLevel(this) * 0.05F;

                    if (this.random.nextFloat() < f1) {
                        entityhuman.di().a(Items.SHIELD, 100);
                        this.world.broadcastEntityEffect(entityhuman, (byte) 30);
                    }
                }
            }

            this.a(this, entity);
        }

        return flag;
    }

    @Override
	public float a(BlockPosition blockposition) {
        return 0.5F - this.world.n(blockposition);
    }

    protected boolean r_() {
        BlockPosition blockposition = new BlockPosition(this.locX, this.getBoundingBox().b, this.locZ);

        if (this.world.getBrightness(EnumSkyBlock.SKY, blockposition) > this.random.nextInt(32)) {
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

    @Override
	public boolean cM() {
        return this.world.getDifficulty() != EnumDifficulty.PEACEFUL && this.r_() && super.cM();
    }

    @Override
	protected void initAttributes() {
        super.initAttributes();
        this.getAttributeMap().b(GenericAttributes.ATTACK_DAMAGE);
    }

    @Override
	protected boolean isDropExperience() {
        return true;
    }
}
