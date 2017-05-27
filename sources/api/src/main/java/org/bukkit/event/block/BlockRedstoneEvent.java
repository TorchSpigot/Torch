package org.bukkit.event.block;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Called when a redstone current changes
 */
public class BlockRedstoneEvent extends BlockEvent {
    private static final HandlerList handlers = new HandlerList();
    // Torch start
    protected int oldCurrent;
    protected int newCurrent;
    
    private static BlockRedstoneEvent instance;
    
    public static BlockRedstoneEvent requestMutable(final Block block, final int oldCurrent, final int newCurrent) {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Async request mutable event!");
        
        if (instance == null) {
            instance = new BlockRedstoneEvent(block, oldCurrent, newCurrent);
            return instance;
        }
        
        instance.block = block;
        instance.oldCurrent = oldCurrent;
        instance.newCurrent = newCurrent;
        
        return instance;
    }
    // Torch end

    public BlockRedstoneEvent(final Block block, final int oldCurrent, final int newCurrent) {
        super(block);
        this.oldCurrent = oldCurrent;
        this.newCurrent = newCurrent;
    }

    /**
     * Gets the old current of this block
     *
     * @return The previous current
     */
    public int getOldCurrent() {
        return oldCurrent;
    }

    /**
     * Gets the new current of this block
     *
     * @return The new current
     */
    public int getNewCurrent() {
        return newCurrent;
    }

    /**
     * Sets the new current of this block
     *
     * @param newCurrent The new current to set
     */
    public void setNewCurrent(int newCurrent) {
        this.newCurrent = newCurrent;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
