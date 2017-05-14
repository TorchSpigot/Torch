package net.minecraft.server;

import javax.annotation.Nullable;

import org.torch.api.Async;
import org.torch.server.TorchWorldManager;

import lombok.Getter;

public class WorldManager implements IWorldAccess, org.torch.api.TorchServant {
    @Getter private final TorchWorldManager reactor;

    private final MinecraftServer a;
    private final WorldServer world;

    public WorldManager(MinecraftServer minecraftserver, WorldServer worldserver) {
        reactor = new TorchWorldManager(minecraftserver.getReactor(), worldserver, this);
        
        this.a = minecraftserver;
        this.world = reactor.getWorld();
    }

    @Override
    public void a(int i, boolean flag, double d0, double d1, double d2, double d3, double d4, double d5, int... aint) {} // PAIL: spawnParticle

    @Override
    public void a(int i, boolean flag, boolean flag1, double d0, double d1, double d2, double d3, double d4, double d5, int... aint) {} // PAIL: spawnParticle

    @Override
    public void a(Entity entity) {
        reactor.onEntityAdded(entity);
    }

    @Override
    public void b(Entity entity) {
        reactor.onEntityRemoved(entity);
    }

    @Override @Async
    public void a(@Nullable EntityHuman entityhuman, SoundEffect soundeffect, SoundCategory soundcategory, double d0, double d1, double d2, float f, float f1) {
        reactor.playSoundNearbyExpect(entityhuman, soundeffect, soundcategory, d0, d1, d2, f, f1);
    }

    @Override
    public void a(int i, int j, int k, int l, int i1, int j1) {} // PAIL: markBlockRangeForRenderUpdate (client-side only)

    @Override
    public void a(World world, BlockPosition blockposition, IBlockData iblockdata, IBlockData iblockdata1, int i) {
        reactor.notifyBlockUpdate(blockposition);
    }

    @Override
    public void a(BlockPosition blockposition) {} // PAIL: notifyLightSet

    @Override
    public void a(SoundEffect soundeffect, BlockPosition blockposition) {} // PAIL: playRecord

    @Override @Async
    public void a(EntityHuman entityhuman, int i, BlockPosition blockposition, int j) {
        // CraftBukkit - this.world.dimension
        reactor.playWorldEventNearbyExpect(entityhuman, i, blockposition, j);
    }

    @Override @Async
    public void a(int i, BlockPosition blockposition, int j) {
        reactor.playWorldEvent(i, blockposition, j);
    }

    @Override
    public void b(int i, BlockPosition blockposition, int j) {
        reactor.sendBlockBreakProgress(i, blockposition, j);
    }
}
