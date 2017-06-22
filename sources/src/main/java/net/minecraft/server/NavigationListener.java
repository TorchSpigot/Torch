package net.minecraft.server;

import lombok.Getter;

import javax.annotation.Nullable;

import org.torch.server.TorchNavigationListener;

public class NavigationListener implements IWorldAccess, org.torch.api.TorchServant {
    @Getter private final TorchNavigationListener reactor;

    //private final List<NavigationAbstract> a; // Torch - List -> Set

    public NavigationListener() {
        reactor = new TorchNavigationListener(this);
    }

    @Override
    public void a(World world, BlockPosition blockposition, IBlockData iblockdata, IBlockData iblockdata1, int i) {
        reactor.notifyBlockUpdate(world, blockposition, iblockdata, iblockdata1);
    }

    protected boolean a(World world, BlockPosition blockposition, IBlockData iblockdata, IBlockData iblockdata1) {
        return TorchNavigationListener.isBlockChanged(world, blockposition, iblockdata, iblockdata1);
    }

    @Override
    public void a(BlockPosition blockposition) {}

    @Override
    public void a(int i, int j, int k, int l, int i1, int j1) {}

    @Override
    public void a(@Nullable EntityHuman entityhuman, SoundEffect soundeffect, SoundCategory soundcategory, double d0, double d1, double d2, float f, float f1) {}

    @Override
    public void a(int i, boolean flag, double d0, double d1, double d2, double d3, double d4, double d5, int... aint) {}

    @Override
    public void a(int i, boolean flag, boolean flag1, double d0, double d1, double d2, double d3, double d4, double d5, int... aint) {}

    @Override
    public void a(Entity entity) {
        reactor.onEntityAdded(entity);
    }

    @Override
    public void b(Entity entity) {
        reactor.onEntityRemoved(entity);
    }

    @Override
    public void a(SoundEffect soundeffect, BlockPosition blockposition) {}

    @Override
    public void a(int i, BlockPosition blockposition, int j) {}

    @Override
    public void a(EntityHuman entityhuman, int i, BlockPosition blockposition, int j) {}

    @Override
    public void b(int i, BlockPosition blockposition, int j) {}
}
