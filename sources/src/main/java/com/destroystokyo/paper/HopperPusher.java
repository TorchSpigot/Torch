package com.destroystokyo.paper;

import net.minecraft.server.AxisAlignedBB;
import net.minecraft.server.BlockPosition;
import net.minecraft.server.MCUtil;
import net.minecraft.server.TileEntityHopper;
import net.minecraft.server.World;

public interface HopperPusher {

    default TileEntityHopper findHopper() {
        BlockPosition pos = new BlockPosition(getX(), getY(), getZ());
        int startX = pos.getX() - 1;
        int endX = pos.getX() + 1;
        int startY = Math.max(0, pos.getY() - 1);
        int endY = Math.min(255, pos.getY() + 1);
        int startZ = pos.getZ() - 1;
        int endZ = pos.getZ() + 1;
        BlockPosition.PooledBlockPosition adjacentPos = BlockPosition.PooledBlockPosition.aquire();
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    adjacentPos.setValues(x, y, z);
                    TileEntityHopper hopper = MCUtil.getHopper(getWorld(), adjacentPos);
                    if (hopper == null) continue; // Avoid playing with the bounding boxes, if at all possible
                    AxisAlignedBB hopperBoundingBox = hopper.getHopperLookupBoundingBox();
                    /*
                     * Check if the entity's bounding box intersects with the hopper's lookup box.
                     * This operation doesn't work both ways!
                     * Make sure you check if the entity's box intersects the hopper's box, not vice versa!
                     */
                    if (this.getBoundingBox().intersects(hopperBoundingBox)) {
                        return hopper;
                    }
                }
            }
        }
        adjacentPos.free();
        return null;
    }

    boolean acceptItem(TileEntityHopper hopper);

    default boolean tryPutInHopper() {
        if (!getWorld().paperConfig.isHopperPushBased) return false;
        TileEntityHopper hopper = findHopper();
        return hopper != null && hopper.canAcceptItems() && acceptItem(hopper);
    }

    AxisAlignedBB getBoundingBox();

    World getWorld();

    double getX();

    double getY();

    double getZ();
}
