package net.minecraft.server;

import javax.annotation.Nullable;

public class TileEntitySign extends TileEntity {

    public final IChatBaseComponent[] lines = new IChatBaseComponent[] { new ChatComponentText(""), new ChatComponentText(""), new ChatComponentText(""), new ChatComponentText("")};
    public int f = -1;
    public boolean isEditable = true;
    private EntityHuman h;
    private final CommandObjectiveExecutor i = new CommandObjectiveExecutor();

    public TileEntitySign() {}

    @Override
    public NBTTagCompound save(NBTTagCompound nbttagcompound) {
        super.save(nbttagcompound);

        for (int i = 0; i < 4; ++i) {
            String s = IChatBaseComponent.ChatSerializer.a(this.lines[i]);

            nbttagcompound.setString("Text" + (i + 1), s);
        }

        // CraftBukkit start
        if (Boolean.getBoolean("convertLegacySigns")) {
            nbttagcompound.setBoolean("Bukkit.isConverted", true);
        }
        // CraftBukkit end

        this.i.b(nbttagcompound);
        return nbttagcompound;
    }

    @Override
    protected void b(World world) {
        this.a(world);
    }

    @Override
    public void a(NBTTagCompound nbttagcompound) {
        this.isEditable = false;
        super.a(nbttagcompound);
        ICommandListener icommandlistener = new ICommandListener() {
            @Override
            public String getName() {
                return "Sign";
            }

            @Override
            public IChatBaseComponent getScoreboardDisplayName() {
                return new ChatComponentText(this.getName());
            }

            @Override
            public void sendMessage(IChatBaseComponent ichatbasecomponent) {}

            @Override
            public boolean a(int i, String s) {
                return true;
            }

            @Override
            public BlockPosition getChunkCoordinates() {
                return TileEntitySign.this.position;
            }

            @Override
            public Vec3D d() {
                return new Vec3D(TileEntitySign.this.position.getX() + 0.5D, TileEntitySign.this.position.getY() + 0.5D, TileEntitySign.this.position.getZ() + 0.5D);
            }

            @Override
            public World getWorld() {
                return TileEntitySign.this.world;
            }

            @Override
            public Entity f() {
                return null;
            }

            @Override
            public boolean getSendCommandFeedback() {
                return false;
            }

            @Override
            public void a(CommandObjectiveExecutor.EnumCommandResult commandobjectiveexecutor_enumcommandresult, int i) {}

            @Override
            public MinecraftServer B_() {
                return TileEntitySign.this.world.getMinecraftServer();
            }
        };

        // CraftBukkit start - Add an option to convert signs correctly
        // This is done with a flag instead of all the time because
        // we have no way to tell whether a sign is from 1.7.10 or 1.8

        boolean oldSign = Boolean.getBoolean("convertLegacySigns") && !nbttagcompound.getBoolean("Bukkit.isConverted");

        for (int i = 0; i < 4; ++i) {
            String s = nbttagcompound.getString("Text" + (i + 1));
            if (s != null && s.length() > 2048) {
                s = "\"\"";
            }

            try {
                //IChatBaseComponent ichatbasecomponent = IChatBaseComponent.ChatSerializer.a(s); // Paper - move down - the old format might throw a json error

                if (oldSign && !isLoadingStructure) { // Paper - saved structures will be in the new format, but will not have isConverted
                    lines[i] = org.bukkit.craftbukkit.util.CraftChatMessage.fromString(s)[0];
                    continue;
                }
                // CraftBukkit end
                IChatBaseComponent ichatbasecomponent = IChatBaseComponent.ChatSerializer.a(s); // Paper - after old sign

                try {
                    this.lines[i] = ChatComponentUtils.filterForDisplay(icommandlistener, ichatbasecomponent, (Entity) null);
                } catch (CommandException commandexception) {
                    this.lines[i] = ichatbasecomponent;
                }
            } catch (com.google.gson.JsonParseException jsonparseexception) {
                this.lines[i] = new ChatComponentText(s);
            }
        }

        this.i.a(nbttagcompound);
    }

    @Override
    @Nullable
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return new PacketPlayOutTileEntityData(this.position, 9, this.d());
    }

    @Override
    public NBTTagCompound d() {
        return this.save(new NBTTagCompound());
    }

    @Override
    public boolean isFilteredNBT() {
        return true;
    }

    public boolean a() {
        return this.isEditable;
    }

    public void a(EntityHuman entityhuman) {
        this.h = entityhuman;
    }

    public EntityHuman e() {
        return this.h;
    }

    public boolean b(final EntityHuman entityhuman) {
        ICommandListener icommandlistener = new ICommandListener() {
            @Override
            public String getName() {
                return entityhuman.getName();
            }

            @Override
            public IChatBaseComponent getScoreboardDisplayName() {
                return entityhuman.getScoreboardDisplayName();
            }

            @Override
            public void sendMessage(IChatBaseComponent ichatbasecomponent) {}

            @Override
            public boolean a(int i, String s) {
                return i <= 2;
            }

            @Override
            public BlockPosition getChunkCoordinates() {
                return TileEntitySign.this.position;
            }

            @Override
            public Vec3D d() {
                return new Vec3D(TileEntitySign.this.position.getX() + 0.5D, TileEntitySign.this.position.getY() + 0.5D, TileEntitySign.this.position.getZ() + 0.5D);
            }

            @Override
            public World getWorld() {
                return entityhuman.getWorld();
            }

            @Override
            public Entity f() {
                return entityhuman;
            }

            @Override
            public boolean getSendCommandFeedback() {
                return false;
            }

            @Override
            public void a(CommandObjectiveExecutor.EnumCommandResult commandobjectiveexecutor_enumcommandresult, int i) {
                if (TileEntitySign.this.world != null) {
                    TileEntitySign.this.i.a(TileEntitySign.this.world.getMinecraftServer(), this, commandobjectiveexecutor_enumcommandresult, i);
                }

            }

            @Override
            public MinecraftServer B_() {
                return entityhuman.B_();
            }
        };
        IChatBaseComponent[] aichatbasecomponent = this.lines;
        int i = aichatbasecomponent.length;

        for (int j = 0; j < i; ++j) {
            IChatBaseComponent ichatbasecomponent = aichatbasecomponent[j];
            ChatModifier chatmodifier = ichatbasecomponent == null ? null : ichatbasecomponent.getChatModifier();

            if (chatmodifier != null && chatmodifier.h() != null) {
                ChatClickable chatclickable = chatmodifier.h();

                if (chatclickable.a() == ChatClickable.EnumClickAction.RUN_COMMAND) {
                    // CraftBukkit start
                    // entityhuman.B_().getCommandHandler().a(icommandlistener, chatclickable.b());
                    CommandBlockListenerAbstract.executeSafely(icommandlistener, new org.bukkit.craftbukkit.command.ProxiedNativeCommandSender(
                            icommandlistener,
                            new org.bukkit.craftbukkit.command.CraftBlockCommandSender(icommandlistener),
                            entityhuman.getBukkitEntity()
                    ), chatclickable.b());
                    // CraftBukkit end
                }
            }
        }

        return true;
    }

    public CommandObjectiveExecutor f() {
        return this.i;
    }
}
