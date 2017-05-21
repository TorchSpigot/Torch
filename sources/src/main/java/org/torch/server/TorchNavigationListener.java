package org.torch.server;

import lombok.Getter;
import net.minecraft.server.AxisAlignedBB;
import net.minecraft.server.BlockPosition;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityInsentient;
import net.minecraft.server.IBlockData;
import net.minecraft.server.IWorldAccess;
import net.minecraft.server.NavigationAbstract;
import net.minecraft.server.PathEntity;
import net.minecraft.server.PathPoint;
import net.minecraft.server.SoundCategory;
import net.minecraft.server.SoundEffect;
import net.minecraft.server.World;

import org.torch.api.TorchReactor;

import com.koloboke.collect.map.hash.HashObjObjMaps;

import java.util.Map;
import javax.annotation.Nullable;

@Getter
public final class TorchNavigationListener implements org.torch.api.IWorldAccess, TorchReactor {
    /** The legacy */
    private final IWorldAccess servant;
    
    private final Map<EntityInsentient, NavigationAbstract> navigators = HashObjObjMaps.newMutableMap();
    
    public TorchNavigationListener(@Nullable IWorldAccess legacy) {
        servant = legacy;
    }
    
    @Override
    public void notifyBlockUpdate(World world, BlockPosition position, IBlockData oldData, IBlockData newData) {
        if (!isBlockChanged(world, position, oldData, newData)) return;
        
        for (NavigationAbstract navigation : navigators.values()) {
            if (navigation.i()) continue; // PAIL: canUpdatePathOnTimeout
            
            PathEntity currentPath = navigation.getPath();
            if (currentPath == null) continue;
            
            if (!currentPath.b() && currentPath.d() != 0) {
                PathPoint finalPathPoint = navigation.getPath().c();
                
                double distance = position.distanceSquared(
                        (finalPathPoint.a + navigation.getEntity().locX) / 2.0D,
                        (finalPathPoint.b + navigation.getEntity().locY) / 2.0D,
                        (finalPathPoint.c + navigation.getEntity().locZ) / 2.0D);
                
                int goal = (currentPath.d() - currentPath.e()) * (currentPath.d() - currentPath.e());

                if (distance < goal) navigation.updatePath();
            }
        }
        
    }
    
    public static boolean isBlockChanged(World world, BlockPosition position, IBlockData oldData, IBlockData newData) {
        // PAIL: c -> getCollisionBoundingBox
        AxisAlignedBB oldBoundingBox = oldData.c(world, position);
        AxisAlignedBB newBoundingBox = newData.c(world, position);
        
        return oldBoundingBox != newBoundingBox && (oldBoundingBox == null || !oldBoundingBox.equals(newBoundingBox));
    }

    @Override
    public void onEntityAdded(Entity entity) {
        if (entity instanceof EntityInsentient) {
            EntityInsentient insentient = (EntityInsentient) entity;
            NavigationAbstract navigation = insentient.getNavigation();
            
            if (navigation != null) navigators.put(insentient, navigation);
        }
    }
    
    @Override
    public void onEntityRemoved(Entity entity) {
        navigators.remove(entity);
    }

    @Override
    public void playSoundNearbyExpect(EntityHuman expect, SoundEffect effect, SoundCategory category, double x, double y, double z, float volume, float pitch) {
        ;
    }

    @Override
    public void playWorldEventNearbyExpect(EntityHuman expect, int type, BlockPosition position, int data) {
        ;
    }

    @Override
    public void playWorldEvent(int type, BlockPosition position, int data) {
        ;
    }

    @Override
    public void sendBlockBreakProgress(int breakerEntityId, BlockPosition position, int progress) {
        ;
    }
}
