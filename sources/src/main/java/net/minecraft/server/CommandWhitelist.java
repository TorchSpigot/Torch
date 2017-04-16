package net.minecraft.server;

import com.mojang.authlib.GameProfile;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

public class CommandWhitelist extends CommandAbstract {

    public CommandWhitelist() {}

    public String getCommand() {
        return "whitelist";
    }

    public int a() {
        return 3;
    }

    public String getUsage(ICommandListener icommandlistener) {
        return "commands.whitelist.usage";
    }

    public void execute(MinecraftServer minecraftserver, ICommandListener icommandlistener, String[] astring) throws CommandException {
        if (astring.length < 1) {
            throw new ExceptionUsage("commands.whitelist.usage", new Object[0]);
        } else {
            if ("on".equals(astring[0])) {
                minecraftserver.getPlayerList().setHasWhitelist(true);
                a(icommandlistener, (ICommand) this, "commands.whitelist.enabled", new Object[0]);
            } else if ("off".equals(astring[0])) {
                minecraftserver.getPlayerList().setHasWhitelist(false);
                a(icommandlistener, (ICommand) this, "commands.whitelist.disabled", new Object[0]);
            } else if ("list".equals(astring[0])) {
                icommandlistener.sendMessage(new ChatMessage("commands.whitelist.list", new Object[] { Integer.valueOf(minecraftserver.getPlayerList().getWhitelisted().length), Integer.valueOf(minecraftserver.getPlayerList().getSeenPlayers().length)}));
                String[] astring1 = minecraftserver.getPlayerList().getWhitelisted();

                icommandlistener.sendMessage(new ChatComponentText(a((Object[]) astring1)));
            } else {
                GameProfile gameprofile;

                if ("add".equals(astring[0])) {
                    if (astring.length < 2) {
                        throw new ExceptionUsage("commands.whitelist.add.usage", new Object[0]);
                    }

                    // Paper start - Handle offline mode as well
                    /*
                    gameprofile = minecraftserver.getUserCache().getProfile(astring[1]);
                    if (gameprofile == null) {
                        throw new CommandException("commands.whitelist.add.failed", new Object[] { astring[1]});
                    }

                    minecraftserver.getPlayerList().addWhitelist(gameprofile);
                    */
                    this.whitelist(minecraftserver, astring[1], true);
                    // Paper end
                    a(icommandlistener, (ICommand) this, "commands.whitelist.add.success", new Object[] { astring[1]});
                } else if ("remove".equals(astring[0])) {
                    if (astring.length < 2) {
                        throw new ExceptionUsage("commands.whitelist.remove.usage", new Object[0]);
                    }

                    // Paper start - Handle offline mode as well
                    /*
                    gameprofile = minecraftserver.getPlayerList().getWhitelist().a(astring[1]);
                    if (gameprofile == null) {
                        throw new CommandException("commands.whitelist.remove.failed", new Object[] { astring[1]});
                    }

                    minecraftserver.getPlayerList().removeWhitelist(gameprofile);

                    */
                    this.whitelist(minecraftserver, astring[1], false);
                    // Paper end
                    a(icommandlistener, (ICommand) this, "commands.whitelist.remove.success", new Object[] { astring[1]});
                } else if ("reload".equals(astring[0])) {
                    minecraftserver.getPlayerList().reloadWhitelist();
                    a(icommandlistener, (ICommand) this, "commands.whitelist.reloaded", new Object[0]);
                }
            }

        }
    }

    public List<String> tabComplete(MinecraftServer minecraftserver, ICommandListener icommandlistener, String[] astring, @Nullable BlockPosition blockposition) {
        if (astring.length == 1) {
            return a(astring, new String[] { "on", "off", "list", "add", "remove", "reload"});
        } else {
            if (astring.length == 2) {
                if ("remove".equals(astring[0])) {
                    return a(astring, minecraftserver.getPlayerList().getWhitelisted());
                }

                if ("add".equals(astring[0])) {
                    return a(astring, minecraftserver.getUserCache().a());
                }
            }

            return Collections.emptyList();
        }
    }

    // Paper start
    /**
     * Adds or removes a player from the game whitelist
     *
     * @param mcserver running instance of MinecraftServer
     * @param playerName the player we're going to be whitelisting
     * @param add whether we're adding or removing from the whitelist
     */
    private void whitelist(MinecraftServer mcserver, String playerName, boolean add) throws CommandException {
        if (mcserver.getOnlineMode()) {
            // The reason we essentially copy/pasta NMS code here is because the NMS online-only version
            // is capable of providing feedback to the person running the command based on whether or
            // not the player is a real online-mode account
            GameProfile gameprofile = mcserver.getUserCache().getProfile(playerName);
            if (gameprofile == null) {
                if (add) {
                    throw new CommandException("commands.whitelist.add.failed", new Object[] { playerName});
                } else {
                    throw new CommandException("commands.whitelist.remove.failed", new Object[] { playerName});
                }
            }

            if (add) {
                mcserver.getPlayerList().addWhitelist(gameprofile);
            } else {
                mcserver.getPlayerList().removeWhitelist(gameprofile);
            }
        } else {
            // versus our offline version, which will always report success all of the time
            org.bukkit.OfflinePlayer offlinePlayer = org.bukkit.Bukkit.getOfflinePlayer(playerName);
            if (add) {
                offlinePlayer.setWhitelisted(true);
            } else {
                offlinePlayer.setWhitelisted(false);
            }
        }
    }
    // Paper end
}
