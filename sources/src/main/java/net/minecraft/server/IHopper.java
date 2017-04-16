package net.minecraft.server;

public interface IHopper extends IInventory {

    World getWorld();

    double E(); default double getX() { return E(); } // Paper - OBFHELPER

    double F(); default double getY() { return F(); } // Paper - OBFHELPER

    double G(); default double getZ() { return G(); } // Paper - OBFHELPER
}
