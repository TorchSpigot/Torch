package net.minecraft.server;

import java.util.Random;
import javax.annotation.Nullable;

public class EntitySilverfish extends EntityMonster {

    private EntitySilverfish.PathfinderGoalSilverfishWakeOthers a;

    public EntitySilverfish(World world) {
        super(world);
        this.setSize(0.4F, 0.3F);
    }

    public static void a(DataConverterManager dataconvertermanager) {
        EntityInsentient.a(dataconvertermanager, EntitySilverfish.class);
    }

    @Override
	protected void r() {
        this.a = new EntitySilverfish.PathfinderGoalSilverfishWakeOthers(this);
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(3, this.a);
        this.goalSelector.a(4, new PathfinderGoalMeleeAttack(this, 1.0D, false));
        this.goalSelector.a(5, new EntitySilverfish.PathfinderGoalSilverfishHideInBlock(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true, new Class[0]));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
    }

    @Override
	public double ax() {
        return 0.1D;
    }

    @Override
	public float getHeadHeight() {
        return 0.1F;
    }

    @Override
	protected void initAttributes() {
        super.initAttributes();
        this.getAttributeInstance(GenericAttributes.maxHealth).setValue(8.0D);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.25D);
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(1.0D);
    }

    @Override
	protected boolean playStepSound() {
        return false;
    }

    @Override
	protected SoundEffect G() {
        return SoundEffects.fL;
    }

    @Override
	protected SoundEffect bW() {
        return SoundEffects.fN;
    }

    @Override
	protected SoundEffect bX() {
        return SoundEffects.fM;
    }

    @Override
	protected void a(BlockPosition blockposition, Block block) {
        this.a(SoundEffects.fO, 0.15F, 1.0F);
    }

    @Override
	public boolean damageEntity(DamageSource damagesource, float f) {
        if (this.isInvulnerable(damagesource)) {
            return false;
        } else {
            if ((damagesource instanceof EntityDamageSource || damagesource == DamageSource.MAGIC) && this.a != null) {
                this.a.f();
            }

            return super.damageEntity(damagesource, f);
        }
    }

    @Override
	@Nullable
    protected MinecraftKey J() {
        return LootTables.v;
    }

    @Override
	public void A_() {
        this.aN = this.yaw;
        super.A_();
    }

    @Override
	public void i(float f) {
        this.yaw = f;
        super.i(f);
    }

    @Override
	public float a(BlockPosition blockposition) {
        return this.world.getType(blockposition.down()).getBlock() == Blocks.STONE ? 10.0F : super.a(blockposition);
    }

    @Override
	protected boolean r_() {
        return true;
    }

    @Override
	public boolean cM() {
        if (super.cM()) {
            EntityHuman entityhuman = this.world.b(this, 5.0D);
            return !(entityhuman != null && !entityhuman.affectsSpawning) && entityhuman == null; // Paper - Affects Spawning API
        } else {
            return false;
        }
    }

    @Override
	public EnumMonsterType getMonsterType() {
        return EnumMonsterType.ARTHROPOD;
    }

    static class PathfinderGoalSilverfishHideInBlock extends PathfinderGoalRandomStroll {

        private EnumDirection h;
        private boolean i;

        public PathfinderGoalSilverfishHideInBlock(EntitySilverfish entitysilverfish) {
            super(entitysilverfish, 1.0D, 10);
            this.a(1);
        }

        @Override
		public boolean a() {
            if (this.a.getGoalTarget() != null) {
                return false;
            } else if (!this.a.getNavigation().n()) {
                return false;
            } else {
                Random random = this.a.getRandom();

                if (this.a.world.getGameRules().getBoolean("mobGriefing") && random.nextInt(10) == 0) {
                    this.h = EnumDirection.a(random);
                    BlockPosition blockposition = (new BlockPosition(this.a.locX, this.a.locY + 0.5D, this.a.locZ)).shift(this.h);
                    IBlockData iblockdata = this.a.world.getType(blockposition);

                    if (BlockMonsterEggs.i(iblockdata)) {
                        this.i = true;
                        return true;
                    }
                }

                this.i = false;
                return super.a();
            }
        }

        @Override
		public boolean b() {
            return this.i ? false : super.b();
        }

        @Override
		public void c() {
            if (!this.i) {
                super.c();
            } else {
                World world = this.a.world;
                BlockPosition blockposition = (new BlockPosition(this.a.locX, this.a.locY + 0.5D, this.a.locZ)).shift(this.h);
                IBlockData iblockdata = world.getType(blockposition);

                if (BlockMonsterEggs.i(iblockdata)) {
                    // CraftBukkit start
                    if (org.bukkit.craftbukkit.event.CraftEventFactory.callEntityChangeBlockEvent(this.a, blockposition, Blocks.MONSTER_EGG, Block.getId(BlockMonsterEggs.getById(iblockdata.getBlock().toLegacyData(iblockdata)))).isCancelled()) {
                        return;
                    }
                    // CraftBukkit end
                    world.setTypeAndData(blockposition, Blocks.MONSTER_EGG.getBlockData().set(BlockMonsterEggs.VARIANT, BlockMonsterEggs.EnumMonsterEggVarient.a(iblockdata)), 3);
                    this.a.doSpawnEffect();
                    this.a.die();
                }

            }
        }
    }

    static class PathfinderGoalSilverfishWakeOthers extends PathfinderGoal {

        private final EntitySilverfish silverfish;
        private int b;

        public PathfinderGoalSilverfishWakeOthers(EntitySilverfish entitysilverfish) {
            this.silverfish = entitysilverfish;
        }

        public void f() {
            if (this.b == 0) {
                this.b = 20;
            }

        }

        @Override
		public boolean a() {
            return this.b > 0;
        }

        @Override
		public void e() {
            --this.b;
            if (this.b <= 0) {
                World world = this.silverfish.world;
                Random random = this.silverfish.getRandom();
                BlockPosition blockposition = new BlockPosition(this.silverfish);

                for (int i = 0; i <= 5 && i >= -5; i = (i <= 0 ? 1 : 0) - i) {
                    for (int j = 0; j <= 10 && j >= -10; j = (j <= 0 ? 1 : 0) - j) {
                        for (int k = 0; k <= 10 && k >= -10; k = (k <= 0 ? 1 : 0) - k) {
                            BlockPosition blockposition1 = blockposition.a(j, i, k);
                            IBlockData iblockdata = world.getType(blockposition1);

                            if (iblockdata.getBlock() == Blocks.MONSTER_EGG) {
                                // CraftBukkit start
                                if (org.bukkit.craftbukkit.event.CraftEventFactory.callEntityChangeBlockEvent(this.silverfish, blockposition1, Blocks.AIR, 0).isCancelled()) {
                                    continue;
                                }
                                // CraftBukkit end
                                if (world.getGameRules().getBoolean("mobGriefing")) {
                                    world.setAir(blockposition1, true);
                                } else {
                                    world.setTypeAndData(blockposition1, iblockdata.get(BlockMonsterEggs.VARIANT).d(), 3);
                                }

                                if (random.nextBoolean()) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}
