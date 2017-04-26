package net.minecraft.server;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.text.DecimalFormat;
import java.util.Random;
import javax.annotation.Nullable;

// CraftBukkit start
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.event.world.StructureGrowEvent;
// CraftBukkit end

public final class ItemStack {

    public static final ItemStack a = new ItemStack((Item) null);
    public static final DecimalFormat b = new DecimalFormat("#.##");
    private int count;
    private int d;
    private Item item;
    private NBTTagCompound tag;
    private boolean g;
    private int damage;
    private EntityItemFrame i;
    private Block j;
    private boolean k;
    private Block l;
    private boolean m;

    public ItemStack(Block block) {
        this(block, 1);
    }

    public ItemStack(Block block, int i) {
        this(block, i, 0);
    }

    public ItemStack(Block block, int i, int j) {
        this(Item.getItemOf(block), i, j);
    }

    public ItemStack(Item item) {
        this(item, 1);
    }

    public ItemStack(Item item, int i) {
        this(item, i, 0);
    }

    // CraftBukkit start
    public ItemStack(Item item, int i, int j) {
        this(item, i, j, true);
    }

    public ItemStack(Item item, int i, int j, boolean convert) {
        // CraftBukkit end
        this.item = item;
        this.damage = j;
        this.count = i;
        // CraftBukkit start - Pass to setData to do filtering
        if (MinecraftServer.getServer() != null) {
            this.setData(j);
        }
        if (convert) {
            this.convertStack();
        }
        // CraftBukkit end
        if (this.damage < 0) {
            // this.damage = 0; // CraftBukkit - remove this.
        }

        this.F();
    }

    // Called to run this stack through the data converter to handle older storage methods and serialized items
    public void convertStack() {
        if (MinecraftServer.getServer() != null) {
            NBTTagCompound savedStack = new NBTTagCompound();
            this.save(savedStack);
            MinecraftServer.getServer().getDataConverterManager().a(DataConverterTypes.ITEM_INSTANCE, savedStack); // PAIL: convert
            this.load(savedStack);
        }
    }

    private void F() {
        if (this.g && this == ItemStack.a) throw new AssertionError("TRAP"); // CraftBukkit
        this.g = this.isEmpty();
    }

    // CraftBukkit - break into own method
    public void load(NBTTagCompound nbttagcompound) {
    	// Paper - fix NumberFormatException caused by attempting to read an EMPTY ItemStack
    	this.item = nbttagcompound.hasKeyOfType("id", 8) ? Item.b(nbttagcompound.getString("id")) : Item.getItemOf(Blocks.AIR);
        this.count = nbttagcompound.getByte("Count");
        // CraftBukkit start - Route through setData for filtering
        // this.damage = Math.max(0, nbttagcompound.getShort("Damage"));
        this.setData(nbttagcompound.getShort("Damage"));
        // CraftBukkit end

        if (nbttagcompound.hasKeyOfType("tag", 10)) {
            // CraftBukkit start - make defensive copy as this data may be coming from the save thread
            this.tag = (NBTTagCompound) nbttagcompound.getCompound("tag").clone();
            if (this.item != null) {
                this.item.a(this.tag);
                // CraftBukkit end
            }
        }
    }

    public ItemStack(NBTTagCompound nbttagcompound) {
        this.load(nbttagcompound);
        // CraftBukkit end
        this.F();
    }

    // Paper start - optimize isEmpty
    private static Item airItem;
    public boolean isEmpty() {
        if (airItem == null) {
            airItem = Item.REGISTRY.get(new MinecraftKey("air"));
        }
        return this == ItemStack.a || this.item == null || this.item == airItem || (this.count <= 0 || (this.damage < -32768 || this.damage > '\uffff'));
    }
    // Paper end

    public static void a(DataConverterManager dataconvertermanager) {
        dataconvertermanager.a(DataConverterTypes.ITEM_INSTANCE, (new DataInspectorBlockEntity()));
        dataconvertermanager.a(DataConverterTypes.ITEM_INSTANCE, (new DataInspectorEntity()));
    }

    public ItemStack cloneAndSubtract(int i) {
        int j = Math.min(i, this.count);
        ItemStack itemstack = this.cloneItemStack();

        itemstack.setCount(j);
        this.subtract(j);
        return itemstack;
    }

    public Item getItem() {
        return this.g ? Item.getItemOf(Blocks.AIR) : this.item;
    }

    public EnumInteractionResult placeItem(EntityHuman entityhuman, World world, BlockPosition blockposition, EnumHand enumhand, EnumDirection enumdirection, float f, float f1, float f2) {
        // CraftBukkit start - handle all block place event logic here
        int oldData = this.getData();
        int oldCount = this.getCount();

        if (!(this.getItem() instanceof ItemBucket)) { // if not bucket
            world.captureBlockStates = true;
            // special case bonemeal
            if (this.getItem() instanceof ItemDye && this.getData() == 15) {
                Block block = world.getType(blockposition).getBlock();
                if (block == Blocks.SAPLING || block instanceof BlockMushroom) {
                    world.captureTreeGeneration = true;
                }
            }
        }
        EnumInteractionResult enuminteractionresult = this.getItem().a(entityhuman, world, blockposition, enumhand, enumdirection, f, f1, f2);
        int newData = this.getData();
        int newCount = this.getCount();
        this.setCount(oldCount);
        this.setData(oldData);
        world.captureBlockStates = false;
        if (enuminteractionresult == EnumInteractionResult.SUCCESS && world.captureTreeGeneration && world.capturedBlockStates.size() > 0) {
            world.captureTreeGeneration = false;
            Location location = new Location(world.getWorld(), blockposition.getX(), blockposition.getY(), blockposition.getZ());
            TreeType treeType = BlockSapling.treeType;
            BlockSapling.treeType = null;
            List<BlockState> blocks = (List<BlockState>) world.capturedBlockStates.clone();
            world.capturedBlockStates.clear();
            StructureGrowEvent event = null;
            if (treeType != null) {
                boolean isBonemeal = getItem() == Items.DYE && oldData == 15;
                event = new StructureGrowEvent(location, treeType, isBonemeal, (Player) entityhuman.getBukkitEntity(), blocks);
                org.bukkit.Bukkit.getPluginManager().callEvent(event);
            }
            if (event == null || !event.isCancelled()) {
                // Change the stack to its new contents if it hasn't been tampered with.
                if (this.getCount() == oldCount && this.getData() == oldData) {
                    this.setData(newData);
                    this.setCount(newCount);
                }
                for (BlockState blockstate : blocks) {
                    blockstate.update(true);
                }
            }

            return enuminteractionresult;
        }
        world.captureTreeGeneration = false;

        if (enuminteractionresult == EnumInteractionResult.SUCCESS) {
            org.bukkit.event.block.BlockPlaceEvent placeEvent = null;
            List<BlockState> blocks = (List<BlockState>) world.capturedBlockStates.clone();
            world.capturedBlockStates.clear();
            if (blocks.size() > 1) {
                placeEvent = org.bukkit.craftbukkit.event.CraftEventFactory.callBlockMultiPlaceEvent(world, entityhuman, enumhand, blocks, blockposition.getX(), blockposition.getY(), blockposition.getZ());
            } else if (blocks.size() == 1) {
                placeEvent = org.bukkit.craftbukkit.event.CraftEventFactory.callBlockPlaceEvent(world, entityhuman, enumhand, blocks.get(0), blockposition.getX(), blockposition.getY(), blockposition.getZ());
            }

            if (placeEvent != null && (placeEvent.isCancelled() || !placeEvent.canBuild())) {
                enuminteractionresult = EnumInteractionResult.FAIL; // cancel placement
                // PAIL: Remove this when MC-99075 fixed
                placeEvent.getPlayer().updateInventory();
                // revert back all captured blocks
                for (BlockState blockstate : blocks) {
                    blockstate.update(true, false);
                }
            } else {
                // Change the stack to its new contents if it hasn't been tampered with.
                if (this.getCount() == oldCount && this.getData() == oldData) {
                    this.setData(newData);
                    this.setCount(newCount);
                }

                for (Map.Entry<BlockPosition, TileEntity> e : world.capturedTileEntities.entrySet()) {
                    world.setTileEntity(e.getKey(), e.getValue());
                }

                for (BlockState blockstate : blocks) {
                    int x = blockstate.getX();
                    int y = blockstate.getY();
                    int z = blockstate.getZ();
                    int updateFlag = ((CraftBlockState) blockstate).getFlag();
                    org.bukkit.Material mat = blockstate.getType();
                    Block oldBlock = CraftMagicNumbers.getBlock(mat);
                    BlockPosition newblockposition = new BlockPosition(x, y, z);
                    IBlockData block = world.getType(newblockposition);

                    if (!(block.getBlock() instanceof BlockTileEntity)) { // Containers get placed automatically
                        block.getBlock().onPlace(world, newblockposition, block);
                    }

                    world.notifyAndUpdatePhysics(newblockposition, null, oldBlock.getBlockData(), block, updateFlag); // send null chunk as chunk.k() returns false by this point
                }

                // Special case juke boxes as they update their tile entity. Copied from ItemRecord.
                // PAIL: checkme on updates.
                if (this.item instanceof ItemRecord) {
                    ((BlockJukeBox) Blocks.JUKEBOX).a(world, blockposition, world.getType(blockposition), this);
                    world.a((EntityHuman) null, 1010, blockposition, Item.getId(this.item));
                    this.subtract(1);
                    entityhuman.b(StatisticList.Z);
                }

                if (this.item == Items.SKULL) { // Special case skulls to allow wither spawns to be cancelled
                    BlockPosition bp = blockposition;
                    if (!world.getType(blockposition).getBlock().a(world, blockposition)) {
                        if (!world.getType(blockposition).getMaterial().isBuildable()) {
                            bp = null;
                        } else {
                            bp = bp.shift(enumdirection);
                        }
                    }
                    if (bp != null) {
                        TileEntity te = world.getTileEntity(bp);
                        if (te instanceof TileEntitySkull) {
                            Blocks.SKULL.a(world, bp, (TileEntitySkull) te);
                        }
                    }
                }

                // SPIGOT-1288 - play sound stripped from ItemBlock
                if (this.item instanceof ItemBlock) {
                    SoundEffectType soundeffecttype = ((ItemBlock) this.item).getBlock().getStepSound();
                    world.a(entityhuman, blockposition, soundeffecttype.e(), SoundCategory.BLOCKS, (soundeffecttype.a() + 1.0F) / 2.0F, soundeffecttype.b() * 0.8F);
                }

                entityhuman.b(StatisticList.b(this.item));
            }
        }
        world.capturedTileEntities.clear();
        world.capturedBlockStates.clear();
        // CraftBukkit end

        return enuminteractionresult;
    }

    public float a(IBlockData iblockdata) {
        return this.getItem().getDestroySpeed(this, iblockdata);
    }

    public InteractionResultWrapper<ItemStack> a(World world, EntityHuman entityhuman, EnumHand enumhand) {
        return this.getItem().a(world, entityhuman, enumhand);
    }

    public ItemStack a(World world, EntityLiving entityliving) {
        return this.getItem().a(this, world, entityliving);
    }

    public NBTTagCompound save(NBTTagCompound nbttagcompound) {
        MinecraftKey minecraftkey = Item.REGISTRY.b(this.item);

        nbttagcompound.setString("id", minecraftkey == null ? "minecraft:air" : minecraftkey.toString());
        nbttagcompound.setByte("Count", (byte) this.count);
        nbttagcompound.setShort("Damage", (short) this.damage);
        if (this.tag != null) {
            nbttagcompound.set("tag", this.tag.clone()); // CraftBukkit - make defensive copy, data is going to another thread
        }

        return nbttagcompound;
    }

    public int getMaxStackSize() {
        return this.getItem().getMaxStackSize();
    }

    public boolean isStackable() {
        return this.getMaxStackSize() > 1 && (!this.f() || !this.h());
    }

    public boolean f() {
        return this.g ? false : (this.item.getMaxDurability() <= 0 ? false : !this.hasTag() || !this.getTag().getBoolean("Unbreakable"));
    }

    public boolean usesData() {
        return this.getItem().l();
    }

    public boolean h() {
        return this.f() && this.damage > 0;
    }

    public int i() {
        return this.damage;
    }

    public int getData() {
        return this.damage;
    }

    public void setData(int i) {
        // CraftBukkit start - Filter out data for items that shouldn't have it
        // The crafting system uses this value for a special purpose so we have to allow it
        if (i == 32767) {
            this.damage = i;
            return;
        }

        // Is this a block?
        if (CraftMagicNumbers.getBlock(CraftMagicNumbers.getId(this.getItem())) != Blocks.AIR) {
            // If vanilla doesn't use data on it don't allow any
            if (!(this.usesData() || this.getItem().usesDurability())) {
                i = 0;
            }
        }

        // Filter invalid plant data
        if (CraftMagicNumbers.getBlock(CraftMagicNumbers.getId(this.getItem())) == Blocks.DOUBLE_PLANT && (i > 5 || i < 0)) {
            i = 0;
        }
        // CraftBukkit end
        this.damage = i;
        if (this.damage < 0) {
            // this.damage = 0; // CraftBukkit - remove this.
        }
    }

    public int k() {
        return this.getItem().getMaxDurability();
    }

    public boolean isDamaged(int i, Random random) {
        return isDamaged(i, random, null);
    }

    public boolean isDamaged(int i, Random random, EntityLiving entityliving) {
        // Spigot end
        if (!this.f()) {
            return false;
        } else {
            if (i > 0) {
                int j = EnchantmentManager.getEnchantmentLevel(Enchantments.DURABILITY, this);
                int k = 0;

                for (int l = 0; j > 0 && l < i; ++l) {
                    if (EnchantmentDurability.a(this, j, random)) {
                        ++k;
                    }
                }

                i -= k;
                // Spigot start
                if (entityliving instanceof EntityPlayer) {
                    org.bukkit.craftbukkit.inventory.CraftItemStack item = org.bukkit.craftbukkit.inventory.CraftItemStack.asCraftMirror(this);
                    org.bukkit.event.player.PlayerItemDamageEvent event = new org.bukkit.event.player.PlayerItemDamageEvent((org.bukkit.entity.Player) entityliving.getBukkitEntity(), item, i);
                    org.bukkit.Bukkit.getServer().getPluginManager().callEvent(event);
                    if (i != event.getDamage() || event.isCancelled()) {
                        event.getPlayer().updateInventory();
                    }
                    if (event.isCancelled()) return false;
                    i = event.getDamage();
                }
                // Spigot end
                if (i <= 0) {
                    return false;
                }
            }

            this.damage += i;
            return this.damage > this.k();
        }
    }

    public void damage(int i, EntityLiving entityliving) {
        if (!(entityliving instanceof EntityHuman) || !((EntityHuman) entityliving).abilities.canInstantlyBuild) {
            if (this.f()) {
                if (this.isDamaged(i, entityliving.getRandom(), entityliving)) { // Spigot
                    entityliving.b(this);
                    // CraftBukkit start - Check for item breaking
                    if (this.count == 1 && entityliving instanceof EntityHuman) {
                        org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerItemBreakEvent((EntityHuman) entityliving, this);
                    }
                    // CraftBukkit end
                    this.subtract(1);
                    if (entityliving instanceof EntityHuman) {
                        EntityHuman entityhuman = (EntityHuman) entityliving;

                        entityhuman.b(StatisticList.c(this.item));
                    }

                    this.damage = 0;
                }

            }
        }
    }

    public void a(EntityLiving entityliving, EntityHuman entityhuman) {
        boolean flag = this.item.a(this, entityliving, entityhuman);

        if (flag) {
            entityhuman.b(StatisticList.b(this.item));
        }

    }

    public void a(World world, IBlockData iblockdata, BlockPosition blockposition, EntityHuman entityhuman) {
        boolean flag = this.getItem().a(this, world, iblockdata, blockposition, entityhuman);

        if (flag) {
            entityhuman.b(StatisticList.b(this.item));
        }

    }

    public boolean b(IBlockData iblockdata) {
        return this.getItem().canDestroySpecialBlock(iblockdata);
    }

    public boolean a(EntityHuman entityhuman, EntityLiving entityliving, EnumHand enumhand) {
        return this.getItem().a(this, entityhuman, entityliving, enumhand);
    }

    public ItemStack cloneItemStack() {
        ItemStack itemstack = new ItemStack(this.item, this.count, this.damage, false); // CraftBukkit

        if (this.tag != null) {
            itemstack.tag = this.tag.g();
        }

        return itemstack;
    }

    public static boolean equals(ItemStack itemstack, ItemStack itemstack1) {
        return itemstack.isEmpty() && itemstack1.isEmpty() ? true : (!itemstack.isEmpty() && !itemstack1.isEmpty() ? (itemstack.tag == null && itemstack1.tag != null ? false : itemstack.tag == null || itemstack.tag.equals(itemstack1.tag)) : false);
    }

    // Spigot Start
    public static boolean fastMatches(ItemStack itemstack, ItemStack itemstack1) {
        if (itemstack.isEmpty() && itemstack1.isEmpty()) {
            return true;
        }
        if (!itemstack.isEmpty() && !itemstack1.isEmpty()) {
            return itemstack.count == itemstack1.count && itemstack.item == itemstack1.item && itemstack.damage == itemstack1.damage;
        }
        return false;
    }
    // Spigot End

    public static boolean matches(ItemStack itemstack, ItemStack itemstack1) {
        return itemstack.isEmpty() && itemstack1.isEmpty() ? true : (!itemstack.isEmpty() && !itemstack1.isEmpty() ? itemstack.d(itemstack1) : false);
    }

    private boolean d(ItemStack itemstack) {
        return this.count != itemstack.count ? false : (this.getItem() != itemstack.getItem() ? false : (this.damage != itemstack.damage ? false : (this.tag == null && itemstack.tag != null ? false : this.tag == null || this.tag.equals(itemstack.tag))));
    }

    public static boolean c(ItemStack itemstack, ItemStack itemstack1) {
        return itemstack == itemstack1 ? true : (!itemstack.isEmpty() && !itemstack1.isEmpty() ? itemstack.doMaterialsMatch(itemstack1) : false);
    }

    public static boolean d(ItemStack itemstack, ItemStack itemstack1) {
        return itemstack == itemstack1 ? true : (!itemstack.isEmpty() && !itemstack1.isEmpty() ? itemstack.b(itemstack1) : false);
    }

    public boolean doMaterialsMatch(ItemStack itemstack) {
        return !itemstack.isEmpty() && this.item == itemstack.item && this.damage == itemstack.damage;
    }

    public boolean b(ItemStack itemstack) {
        return !this.f() ? this.doMaterialsMatch(itemstack) : !itemstack.isEmpty() && this.item == itemstack.item;
    }

    public String a() {
        return this.getItem().a(this);
    }

    @Override
	public String toString() {
        return this.count + "x" + this.getItem().getName() + "@" + this.damage;
    }

    public void a(World world, Entity entity, int i, boolean flag) {
        if (this.d > 0) {
            --this.d;
        }

        if (this.item != null) {
            this.item.a(this, world, entity, i, flag);
        }

    }

    public void a(World world, EntityHuman entityhuman, int i) {
        entityhuman.a(StatisticList.a(this.item), i);
        this.getItem().b(this, world, entityhuman);
    }

    public int m() {
        return this.getItem().e(this);
    }

    public EnumAnimation n() {
        return this.getItem().f(this);
    }

    public void a(World world, EntityLiving entityliving, int i) {
        this.getItem().a(this, world, entityliving, i);
    }

    public boolean hasTag() {
        return !this.g && this.tag != null;
    }

    @Nullable
    public NBTTagCompound getTag() {
        return this.tag;
    }

    public NBTTagCompound c(String s) {
        if (this.tag != null && this.tag.hasKeyOfType(s, 10)) {
            return this.tag.getCompound(s);
        } else {
            NBTTagCompound nbttagcompound = new NBTTagCompound();

            this.a(s, nbttagcompound);
            return nbttagcompound;
        }
    }

    @Nullable
    public NBTTagCompound d(String s) {
        return this.tag != null && this.tag.hasKeyOfType(s, 10) ? this.tag.getCompound(s) : null;
    }

    public void e(String s) {
        if (this.tag != null && this.tag.hasKeyOfType(s, 10)) {
            this.tag.remove(s);
        }

    }

    @Nullable
    public NBTTagList getEnchantments() {
        return this.tag == null ? null : this.tag.getList("ench", 10);
    }

    public void setTag(@Nullable NBTTagCompound nbttagcompound) {
        this.tag = nbttagcompound;
    }

    public String getName() {
        NBTTagCompound nbttagcompound = this.d("display");

        if (nbttagcompound != null) {
            if (nbttagcompound.hasKeyOfType("Name", 8)) {
                return nbttagcompound.getString("Name");
            }

            if (nbttagcompound.hasKeyOfType("LocName", 8)) {
                return LocaleI18n.get(nbttagcompound.getString("LocName"));
            }
        }

        return this.getItem().b(this);
    }

    public ItemStack f(String s) {
        this.c("display").setString("LocName", s);
        return this;
    }

    public ItemStack g(String s) {
        this.c("display").setString("Name", s);
        return this;
    }

    public void s() {
        NBTTagCompound nbttagcompound = this.d("display");

        if (nbttagcompound != null) {
            nbttagcompound.remove("Name");
            if (nbttagcompound.isEmpty()) {
                this.e("display");
            }
        }

        if (this.tag != null && this.tag.isEmpty()) {
            this.tag = null;
        }

    }

    public boolean hasName() {
        NBTTagCompound nbttagcompound = this.d("display");

        return nbttagcompound != null && nbttagcompound.hasKeyOfType("Name", 8);
    }

    public EnumItemRarity v() {
        return this.getItem().g(this);
    }

    public boolean canEnchant() {
        return !this.getItem().g_(this) ? false : !this.hasEnchantments();
    }

    public void addEnchantment(Enchantment enchantment, int i) {
        if (this.tag == null) {
            this.setTag(new NBTTagCompound());
        }

        if (!this.tag.hasKeyOfType("ench", 9)) {
            this.tag.set("ench", new NBTTagList());
        }

        NBTTagList nbttaglist = this.tag.getList("ench", 10);
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        nbttagcompound.setShort("id", (short) Enchantment.getId(enchantment));
        nbttagcompound.setShort("lvl", ((byte) i));
        nbttaglist.add(nbttagcompound);
    }

    public boolean hasEnchantments() {
        return this.tag != null && this.tag.hasKeyOfType("ench", 9) ? !this.tag.getList("ench", 10).isEmpty() : false;
    }

    public void a(String s, NBTBase nbtbase) {
        if (this.tag == null) {
            this.setTag(new NBTTagCompound());
        }

        this.tag.set(s, nbtbase);
    }

    public boolean y() {
        return this.getItem().t();
    }

    public boolean z() {
        return this.i != null;
    }

    public void a(EntityItemFrame entityitemframe) {
        this.i = entityitemframe;
    }

    @Nullable
    public EntityItemFrame A() {
        return this.g ? null : this.i;
    }

    public int getRepairCost() {
        return this.hasTag() && this.tag.hasKeyOfType("RepairCost", 3) ? this.tag.getInt("RepairCost") : 0;
    }

    public void setRepairCost(int i) {
        if (!this.hasTag()) {
            this.tag = new NBTTagCompound();
        }

        this.tag.setInt("RepairCost", i);
    }

    public Multimap<String, AttributeModifier> a(EnumItemSlot enumitemslot) {
        Object object;

        if (this.hasTag() && this.tag.hasKeyOfType("AttributeModifiers", 9)) {
            object = HashMultimap.create();
            NBTTagList nbttaglist = this.tag.getList("AttributeModifiers", 10);

            for (int i = 0; i < nbttaglist.size(); ++i) {
                NBTTagCompound nbttagcompound = nbttaglist.get(i);
                AttributeModifier attributemodifier = GenericAttributes.a(nbttagcompound);

                if (attributemodifier != null && (!nbttagcompound.hasKeyOfType("Slot", 8) || nbttagcompound.getString("Slot").equals(enumitemslot.d())) && attributemodifier.a().getLeastSignificantBits() != 0L && attributemodifier.a().getMostSignificantBits() != 0L) {
                    ((Multimap) object).put(nbttagcompound.getString("AttributeName"), attributemodifier);
                }
            }
        } else {
            object = this.getItem().a(enumitemslot);
        }

        return (Multimap) object;
    }

    public void a(String s, AttributeModifier attributemodifier, @Nullable EnumItemSlot enumitemslot) {
        if (this.tag == null) {
            this.tag = new NBTTagCompound();
        }

        if (!this.tag.hasKeyOfType("AttributeModifiers", 9)) {
            this.tag.set("AttributeModifiers", new NBTTagList());
        }

        NBTTagList nbttaglist = this.tag.getList("AttributeModifiers", 10);
        NBTTagCompound nbttagcompound = GenericAttributes.a(attributemodifier);

        nbttagcompound.setString("AttributeName", s);
        if (enumitemslot != null) {
            nbttagcompound.setString("Slot", enumitemslot.d());
        }

        nbttaglist.add(nbttagcompound);
    }

    @Deprecated
    public void setItem(Item item) {
        this.item = item;
        this.setData(this.getData()); // CraftBukkit - Set data again to ensure it is filtered properly
    }

    public IChatBaseComponent C() {
        ChatComponentText chatcomponenttext = new ChatComponentText(this.getName());

        if (this.hasName()) {
            chatcomponenttext.getChatModifier().setItalic(Boolean.valueOf(true));
        }

        IChatBaseComponent ichatbasecomponent = (new ChatComponentText("[")).addSibling(chatcomponenttext).a("]");

        if (!this.g) {
            NBTTagCompound nbttagcompound = this.save(new NBTTagCompound());

            ichatbasecomponent.getChatModifier().setChatHoverable(new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_ITEM, new ChatComponentText(nbttagcompound.toString())));
            ichatbasecomponent.getChatModifier().setColor(this.v().e);
        }

        return ichatbasecomponent;
    }

    public boolean a(Block block) {
        if (block == this.j) {
            return this.k;
        } else {
            this.j = block;
            if (this.hasTag() && this.tag.hasKeyOfType("CanDestroy", 9)) {
                NBTTagList nbttaglist = this.tag.getList("CanDestroy", 8);

                for (int i = 0; i < nbttaglist.size(); ++i) {
                    Block block1 = Block.getByName(nbttaglist.getString(i));

                    if (block1 == block) {
                        this.k = true;
                        return true;
                    }
                }
            }

            this.k = false;
            return false;
        }
    }

    public boolean b(Block block) {
        if (block == this.l) {
            return this.m;
        } else {
            this.l = block;
            if (this.hasTag() && this.tag.hasKeyOfType("CanPlaceOn", 9)) {
                NBTTagList nbttaglist = this.tag.getList("CanPlaceOn", 8);

                for (int i = 0; i < nbttaglist.size(); ++i) {
                    Block block1 = Block.getByName(nbttaglist.getString(i));

                    if (block1 == block) {
                        this.m = true;
                        return true;
                    }
                }
            }

            this.m = false;
            return false;
        }
    }

    public void d(int i) {
        this.d = i;
    }

    public int getCount() {
        return this.g ? 0 : this.count;
    }

    public void setCount(int i) {
        this.count = i;
        this.F();
    }

    public void add(int i) {
        this.setCount(this.count + i);
    }

    public void subtract(int i) {
        this.add(-i);
    }
}
