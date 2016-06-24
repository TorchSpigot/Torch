package org.bukkit.block;

import com.destroystokyo.paper.loottable.LootableInventory; // Paper
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Represents a chest.
 */
public interface Chest extends BlockState, InventoryHolder, LootableInventory { // Paper

    /**
     * Returns the chest's inventory. If this is a double chest, it returns
     * just the portion of the inventory linked to this half of the chest.
     *
     * @return The inventory.
     */
    Inventory getBlockInventory();
}
