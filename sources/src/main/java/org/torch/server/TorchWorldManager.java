package org.torch.server;

import lombok.Getter;
import net.minecraft.server.BlockPosition;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.IBlockData;
import net.minecraft.server.IWorldAccess;
import net.minecraft.server.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.PacketPlayOutWorldEvent;
import net.minecraft.server.SoundCategory;
import net.minecraft.server.SoundEffect;
import net.minecraft.server.World;
import net.minecraft.server.WorldServer;

import org.torch.api.Async;
import org.torch.api.TorchReactor;

import javax.annotation.Nullable;

@Getter
public final class TorchWorldManager implements org.torch.api.IWorldAccess, TorchReactor {
    /** The legacy */
    private final IWorldAccess servant;
    
    /**
     * Reference to the TorchServer instance
     * */
    private final TorchServer server;
    /**
     * The WorldServer instance
     * */
    private final WorldServer world;
    
    public TorchWorldManager(TorchServer server, WorldServer worldserver, @Nullable IWorldAccess legacy) {
        servant = legacy;
        
        this.server = server;
        this.world = worldserver;
    }
    
    /**
     * Called on all IWorldAccess when an entity is created or loaded.
     * On server-side worlds, adds the entity to the entity tracker.
     */
    @Override
    public void onEntityAdded(Entity entity) {
        world.getTracker().track(entity);
        
        if (entity instanceof EntityPlayer) world.worldProvider.a((EntityPlayer) entity); // PAIL: onPlayerAdded
    }
    
    /**
     * Called on all IWorldAccesses when an entity is unloaded or destroyed.
     * On server-side worlds, removes the entity from the entity tracker.
     */
    @Override
    public void onEntityRemoved(Entity entity) {
        world.getTracker().untrackEntity(entity);
        world.getScoreboard().a(entity); // PAIL: removeEntity
        
        if (entity instanceof EntityPlayer) world.worldProvider.b((EntityPlayer) entity); // PAIL: onPlayerRemoved
    }
    
    @Override
    public void notifyBlockUpdate(World world, BlockPosition position, IBlockData oldData, IBlockData newData) {
        this.world.getPlayerChunkMap().flagDirty(position);
    }
    
    @Override @Async
    public void playSoundNearbyExpect(@Nullable EntityHuman expect, SoundEffect effect, SoundCategory category, double x, double y, double z, float volume, float pitch) {
        server.getPlayerList().sendPacketNearby(expect, x, y, z, volume > 1.0F ? (double) (16.0F * volume) : 16.0D, world.dimension,
                new PacketPlayOutNamedSoundEffect(effect, category, x, y, z, volume, pitch));
    }
    
    @Override @Async
    public void playWorldEventNearbyExpect(EntityHuman expect, int type, BlockPosition position, int data) {
        server.getPlayerList().sendPacketNearby(expect, position.getX(), position.getY(), position.getZ(), 64.0D, this.world.dimension,
                new PacketPlayOutWorldEvent(type, position, data, false));
    }
    
    @Override @Async
    public void playWorldEvent(int type, BlockPosition position, int data) {
        server.getPlayerList().sendAll(new PacketPlayOutWorldEvent(type, position, data, true));
    }
    
    @Override @Async
    public void sendBlockBreakProgress(int breakerEntityId, BlockPosition position, int progress) {
        Entity breakerEntity = world.getEntity(breakerEntityId); // TODO: not precisly safe
        
        EntityHuman breakerPlayer = null;
        if (breakerEntity instanceof EntityHuman) breakerPlayer = (EntityHuman) breakerEntity;
        EntityHuman fBreakerPlayer = breakerPlayer;
        
        Regulator.post(() -> {
            for (EntityPlayer eachPlayer : server.getPlayerList().players) {
                if (eachPlayer == null || eachPlayer.world != world || eachPlayer.getId() == breakerEntityId) continue;
                
                // CraftBukkit - only send packet to who can see the breaker
                if (fBreakerPlayer != null && fBreakerPlayer instanceof EntityPlayer && !eachPlayer.getBukkitEntity().canSee(((EntityPlayer) fBreakerPlayer).getBukkitEntity())) {
                    continue;
                }
                
                double offsetX = position.getX() - eachPlayer.locX;
                double offsetY = position.getY() - eachPlayer.locY;
                double offsetZ = position.getZ() - eachPlayer.locZ;
                
                if (offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ < 1024.0D) {
                    eachPlayer.playerConnection.sendPacket(new PacketPlayOutBlockBreakAnimation(breakerEntityId, position, progress));
                }
            }
        });
    }
    
}
