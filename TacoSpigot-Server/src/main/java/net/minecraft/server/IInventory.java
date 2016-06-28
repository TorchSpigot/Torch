package net.minecraft.server;

import javax.annotation.Nullable;
import org.bukkit.craftbukkit.entity.CraftHumanEntity; // CraftBukkit

public interface IInventory extends INamableTileEntity {

    int getSize();

    @Nullable
    ItemStack getItem(int i);

    @Nullable
    ItemStack splitStack(int i, int j);

    @Nullable
    ItemStack splitWithoutUpdate(int i);

    void setItem(int i, @Nullable ItemStack itemstack);

    int getMaxStackSize();

    void update();

    boolean a(EntityHuman entityhuman);

    void startOpen(EntityHuman entityhuman);

    void closeContainer(EntityHuman entityhuman);

    boolean b(int i, ItemStack itemstack);

    int getProperty(int i);

    void setProperty(int i, int j);

    int g();

    void l();

    // CraftBukkit start
    ItemStack[] getContents();

    void onOpen(CraftHumanEntity who);

    void onClose(CraftHumanEntity who);

    java.util.List<org.bukkit.entity.HumanEntity> getViewers();

    org.bukkit.inventory.InventoryHolder getOwner();

    void setMaxStackSize(int size);

    org.bukkit.Location getLocation();

    int MAX_STACK = 64;
    // CraftBukkit end
}
