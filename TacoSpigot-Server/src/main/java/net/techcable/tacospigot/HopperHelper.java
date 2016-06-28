package net.techcable.tacospigot;

import net.minecraft.server.Block;
import net.minecraft.server.BlockChest;
import net.minecraft.server.BlockPosition;
import net.minecraft.server.Blocks;
import net.minecraft.server.Chunk;
import net.minecraft.server.IHopper;
import net.minecraft.server.IInventory;
import net.minecraft.server.TileEntity;
import net.minecraft.server.TileEntityHopper;
import net.minecraft.server.World;

import org.bukkit.event.inventory.InventoryMoveItemEvent;

public class HopperHelper {

    public static TileEntityHopper getHopper(World world, BlockPosition pos) {
        Chunk chunk = world.getChunkIfLoaded(pos);
        if (chunk != null && chunk.getBlockData(pos).getBlock() == Blocks.HOPPER) {
            TileEntity tileEntity = chunk.a(pos, Chunk.EnumTileEntityState.IMMEDIATE);
            if (tileEntity instanceof TileEntityHopper) {
                return (TileEntityHopper) tileEntity;
            }
        }
        return null;
    }

    public static IInventory getInventory(World world, BlockPosition position) {
        Block block = world.getType(position).getBlock();
        if (block instanceof BlockChest) {
            return ((BlockChest) block).c(world, position);
        }
        if (block.isTileEntity()) {
            TileEntity tile = world.getTileEntity(position);
            if (tile instanceof IInventory) return (IInventory) tile;
        }
        return null;
    }

    public static boolean isFireInventoryMoveItemEvent(IHopper hopper) {
        return hopper.getWorld().tacoSpigotConfig.isHopperFireIMIE && InventoryMoveItemEvent.getHandlerList().getRegisteredListeners().length > 0;
    }
}
