package net.minecraft.server;

// CraftBukkit start
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftShapedRecipe;
// CraftBukkit end

public class ShapedRecipes implements IRecipe {

    private final int width;
    private final int height;
    private final ItemStack[] items;
    public ItemStack result; // Spigot
    private boolean e;

    public ShapedRecipes(int i, int j, ItemStack[] aitemstack, ItemStack itemstack) {
        this.width = i;
        this.height = j;
        this.items = aitemstack;

        for (int k = 0; k < this.items.length; ++k) {
            if (this.items[k] == null) {
                this.items[k] = ItemStack.a;
            }
        }

        this.result = itemstack;
    }

    // CraftBukkit start
    public org.bukkit.inventory.ShapedRecipe toBukkitRecipe() {
        CraftItemStack result = CraftItemStack.asCraftMirror(this.result);
        CraftShapedRecipe recipe = new CraftShapedRecipe(result, this);
        switch (this.height) {
        case 1:
            switch (this.width) {
            case 1:
                recipe.shape("a");
                break;
            case 2:
                recipe.shape("ab");
                break;
            case 3:
                recipe.shape("abc");
                break;
            }
            break;
        case 2:
            switch (this.width) {
            case 1:
                recipe.shape("a","b");
                break;
            case 2:
                recipe.shape("ab","cd");
                break;
            case 3:
                recipe.shape("abc","def");
                break;
            }
            break;
        case 3:
            switch (this.width) {
            case 1:
                recipe.shape("a","b","c");
                break;
            case 2:
                recipe.shape("ab","cd","ef");
                break;
            case 3:
                recipe.shape("abc","def","ghi");
                break;
            }
            break;
        }
        char c = 'a';
        for (ItemStack stack : this.items) {
            if (stack != null) {
                recipe.setIngredient(c, org.bukkit.craftbukkit.util.CraftMagicNumbers.getMaterial(stack.getItem()), stack.getData());
            }
            c++;
        }
        return recipe;
    }
    // CraftBukkit end

    public ItemStack b() {
        return this.result;
    }

    public NonNullList<ItemStack> b(InventoryCrafting inventorycrafting) {
        NonNullList nonnulllist = NonNullList.a(inventorycrafting.getSize(), ItemStack.a);

        for (int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack itemstack = inventorycrafting.getItem(i);

            if (itemstack.getItem().s()) {
                nonnulllist.set(i, new ItemStack(itemstack.getItem().r()));
            }
        }

        return nonnulllist;
    }

    public boolean a(InventoryCrafting inventorycrafting, World world) {
        for (int i = 0; i <= 3 - this.width; ++i) {
            for (int j = 0; j <= 3 - this.height; ++j) {
                if (this.a(inventorycrafting, i, j, true)) {
                    return true;
                }

                if (this.a(inventorycrafting, i, j, false)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean a(InventoryCrafting inventorycrafting, int i, int j, boolean flag) {
        for (int k = 0; k < 3; ++k) {
            for (int l = 0; l < 3; ++l) {
                int i1 = k - i;
                int j1 = l - j;
                ItemStack itemstack = ItemStack.a;

                if (i1 >= 0 && j1 >= 0 && i1 < this.width && j1 < this.height) {
                    if (flag) {
                        itemstack = this.items[this.width - i1 - 1 + j1 * this.width];
                    } else {
                        itemstack = this.items[i1 + j1 * this.width];
                    }
                }

                ItemStack itemstack1 = inventorycrafting.c(k, l);

                if (!itemstack1.isEmpty() || !itemstack.isEmpty()) {
                    if (itemstack1.isEmpty() != itemstack.isEmpty()) {
                        return false;
                    }

                    if (itemstack.getItem() != itemstack1.getItem()) {
                        return false;
                    }

                    if (itemstack.getData() != 32767 && itemstack.getData() != itemstack1.getData()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public ItemStack craftItem(InventoryCrafting inventorycrafting) {
        ItemStack itemstack = this.b().cloneItemStack();

        if (this.e) {
            for (int i = 0; i < inventorycrafting.getSize(); ++i) {
                ItemStack itemstack1 = inventorycrafting.getItem(i);

                if (!itemstack1.isEmpty() && itemstack1.hasTag()) {
                    itemstack.setTag(itemstack1.getTag().g());
                }
            }
        }

        return itemstack;
    }

    public int a() {
        return this.width * this.height;
    }

    // Spigot start
    public java.util.List<ItemStack> getIngredients()
    {
        return java.util.Arrays.asList( items );
    }
    // Spigot end
}
