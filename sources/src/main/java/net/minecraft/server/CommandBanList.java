package net.minecraft.server;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

public class CommandBanList extends CommandAbstract {

    public CommandBanList() {}

    @Override
	public String getCommand() {
        return "banlist";
    }

    @Override
	public int a() {
        return 3;
    }

    @Override
	public boolean canUse(MinecraftServer minecraftserver, ICommandListener icommandlistener) {
        return (minecraftserver.getPlayerList().getIPBans().isEnabled() || minecraftserver.getPlayerList().getProfileBans().isEnabled()) && super.canUse(minecraftserver, icommandlistener);
    }

    @Override
	public String getUsage(ICommandListener icommandlistener) {
        return "commands.banlist.usage";
    }

    @Override
	public void execute(MinecraftServer minecraftserver, ICommandListener icommandlistener, String[] astring) throws CommandException {
        if (astring.length >= 1 && "ips".equalsIgnoreCase(astring[0])) {
        	astring = minecraftserver.getPlayerList().getIPBans().getEntries(); // Torch
        	
            icommandlistener.sendMessage(new ChatMessage("commands.banlist.ips", new Object[] { Integer.valueOf(astring.length)}));
            icommandlistener.sendMessage(new ChatComponentText(a(astring)));
        } else {
        	astring = minecraftserver.getPlayerList().getProfileBans().getEntries(); // Torch
        	
            icommandlistener.sendMessage(new ChatMessage("commands.banlist.players", new Object[] { Integer.valueOf(astring.length)}));
            icommandlistener.sendMessage(new ChatComponentText(a(astring)));
        }

    }

    @Override
	public List<String> tabComplete(MinecraftServer minecraftserver, ICommandListener icommandlistener, String[] astring, @Nullable BlockPosition blockposition) {
        return astring.length == 1 ? a(astring, new String[] { "players", "ips"}) : Collections.emptyList();
    }
}
