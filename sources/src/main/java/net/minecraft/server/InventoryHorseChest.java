package net.minecraft.server;

public class InventoryHorseChest extends InventorySubcontainer {

    // CraftBukkit start
    public InventoryHorseChest(String s, int i, EntityHorseAbstract owner) {
        super(s, false, i, (org.bukkit.entity.AbstractHorse) owner.getBukkitEntity());
        // CraftBukkit end
    }
}
