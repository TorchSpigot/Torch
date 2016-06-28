package net.minecraft.server;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

public class CommandGamemode extends CommandAbstract {

    public CommandGamemode() {}

    public String getCommand() {
        return "gamemode";
    }

    public int a() {
        return 2;
    }

    public String getUsage(ICommandListener icommandlistener) {
        return "commands.gamemode.usage";
    }

    public void execute(MinecraftServer minecraftserver, ICommandListener icommandlistener, String[] astring) throws CommandException {
        if (astring.length <= 0) {
            throw new ExceptionUsage("commands.gamemode.usage", new Object[0]);
        } else {
            WorldSettings.EnumGamemode worldsettings_enumgamemode = this.c(icommandlistener, astring[0]);
            EntityPlayer entityplayer = astring.length >= 2 ? a(minecraftserver, icommandlistener, astring[1]) : a(icommandlistener);

            entityplayer.a(worldsettings_enumgamemode);
            // CraftBukkit start - handle event cancelling the change
            if (entityplayer.playerInteractManager.getGameMode() != worldsettings_enumgamemode) {
                icommandlistener.sendMessage(new ChatComponentText("Failed to set the gamemode of '" + entityplayer.getName() + "'"));
                return;
            }
            // CraftBukkit end
            ChatMessage chatmessage = new ChatMessage("gameMode." + worldsettings_enumgamemode.b(), new Object[0]);

            if (icommandlistener.getWorld().getGameRules().getBoolean("sendCommandFeedback")) {
                entityplayer.sendMessage(new ChatMessage("gameMode.changed", new Object[] { chatmessage}));
            }

            if (entityplayer != icommandlistener) {
                a(icommandlistener, this, 1, "commands.gamemode.success.other", new Object[] { entityplayer.getName(), chatmessage});
            } else {
                a(icommandlistener, this, 1, "commands.gamemode.success.self", new Object[] { chatmessage});
            }

        }
    }

    protected WorldSettings.EnumGamemode c(ICommandListener icommandlistener, String s) throws ExceptionInvalidNumber {
        WorldSettings.EnumGamemode worldsettings_enumgamemode = WorldSettings.EnumGamemode.a(s, WorldSettings.EnumGamemode.NOT_SET);

        return worldsettings_enumgamemode == WorldSettings.EnumGamemode.NOT_SET ? WorldSettings.a(a(s, 0, WorldSettings.EnumGamemode.values().length - 2)) : worldsettings_enumgamemode;
    }

    public List<String> tabComplete(MinecraftServer minecraftserver, ICommandListener icommandlistener, String[] astring, @Nullable BlockPosition blockposition) {
        return astring.length == 1 ? a(astring, new String[] { "survival", "creative", "adventure", "spectator"}) : (astring.length == 2 ? a(astring, minecraftserver.getPlayers()) : Collections.<String>emptyList()); // CraftBukkit - decompile error
    }

    public boolean isListStart(String[] astring, int i) {
        return i == 1;
    }

    // CraftBukkit start - fix decompiler error
    @Override
    public int compareTo(ICommand o) {
        return a((ICommand) o);
    }
    // CraftBukkit end
}
