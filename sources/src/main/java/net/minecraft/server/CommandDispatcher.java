package net.minecraft.server;

import java.util.Iterator;

import org.torch.server.TorchServer;

public class CommandDispatcher extends CommandHandler implements ICommandDispatcher {

    private final MinecraftServer a;

    public CommandDispatcher(MinecraftServer minecraftserver) {
        this.a = minecraftserver;
        this.a((new CommandTime()));
        this.a((new CommandGamemode()));
        this.a((new CommandDifficulty()));
        this.a((new CommandGamemodeDefault()));
        this.a((new CommandKill()));
        this.a((new CommandToggleDownfall()));
        this.a((new CommandWeather()));
        this.a((new CommandXp()));
        this.a((new CommandTp()));
        this.a((new CommandTeleport()));
        this.a((new CommandGive()));
        this.a((new CommandReplaceItem()));
        this.a((new CommandStats()));
        this.a((new CommandEffect()));
        this.a((new CommandEnchant()));
        this.a((new CommandParticle()));
        this.a((new CommandMe()));
        this.a((new CommandSeed()));
        this.a((new CommandHelp()));
        this.a((new CommandDebug()));
        this.a((new CommandTell()));
        this.a((new CommandSay()));
        this.a((new CommandSpawnpoint()));
        this.a((new CommandSetWorldSpawn()));
        this.a((new CommandGamerule()));
        this.a((new CommandClear()));
        this.a((new CommandTestFor()));
        this.a((new CommandSpreadPlayers()));
        this.a((new CommandPlaySound()));
        this.a((new CommandScoreboard()));
        this.a((new CommandExecute()));
        this.a((new CommandTrigger()));
        this.a((new CommandAchievement()));
        this.a((new CommandSummon()));
        this.a((new CommandSetBlock()));
        this.a((new CommandFill()));
        this.a((new CommandClone()));
        this.a((new CommandTestForBlocks()));
        this.a((new CommandBlockData()));
        this.a((new CommandTestForBlock()));
        this.a((new CommandTellRaw()));
        this.a((new CommandWorldBorder()));
        this.a((new CommandTitle()));
        this.a((new CommandEntityData()));
        this.a((new CommandStopSound()));
        this.a((new CommandLocate()));
        if (minecraftserver.aa()) {
            this.a((new CommandOp()));
            this.a((new CommandDeop()));
            this.a((new CommandStop()));
            this.a((new CommandSaveAll()));
            this.a((new CommandSaveOff()));
            this.a((new CommandSaveOn()));
            this.a((new CommandBanIp()));
            this.a((new CommandPardonIP()));
            this.a((new CommandBan()));
            this.a((new CommandBanList()));
            this.a((new CommandPardon()));
            this.a((new CommandKick()));
            this.a((new CommandList()));
            this.a((new CommandWhitelist()));
            this.a((new CommandIdleTimeout()));
        } else {
            this.a((new CommandPublish()));
        }

        CommandAbstract.a(this);
    }

    @Override
    public void a(ICommandListener icommandlistener, ICommand icommand, int i, String s, Object... aobject) {
        boolean flag = true;
        MinecraftServer minecraftserver = this.a;

        if (!icommandlistener.getSendCommandFeedback()) {
            flag = false;
        }

        ChatMessage chatmessage = new ChatMessage("chat.type.admin", new Object[] { icommandlistener.getName(), new ChatMessage(s, aobject)});

        chatmessage.getChatModifier().setColor(EnumChatFormat.GRAY);
        chatmessage.getChatModifier().setItalic(Boolean.valueOf(true));
        if (flag) {
            Iterator iterator = minecraftserver.getPlayerList().v().iterator();

            while (iterator.hasNext()) {
                EntityHuman entityhuman = (EntityHuman) iterator.next();

                if (entityhuman != icommandlistener && minecraftserver.getPlayerList().isOp(entityhuman.getProfile()) && icommand.canUse(this.a, icommandlistener)) {
                    boolean flag1 = icommandlistener instanceof MinecraftServer && this.a.s();
                    boolean flag2 = icommandlistener instanceof RemoteControlCommandListener && this.a.r();

                    if (flag1 || flag2 || !(icommandlistener instanceof RemoteControlCommandListener) && !(icommandlistener instanceof MinecraftServer)) {
                        entityhuman.sendMessage(chatmessage);
                    }
                }
            }
        }

        if (icommandlistener != minecraftserver && minecraftserver.worldServer[0].getGameRules().getBoolean("logAdminCommands") && !org.spigotmc.SpigotConfig.silentCommandBlocks) { // Spigot
            TorchServer.sendMessage(chatmessage);
        }

        boolean flag3 = minecraftserver.worldServer[0].getGameRules().getBoolean("sendCommandFeedback");

        if (icommandlistener instanceof CommandBlockListenerAbstract) {
            flag3 = ((CommandBlockListenerAbstract) icommandlistener).n();
        }

        if ((i & 1) != 1 && flag3 || icommandlistener instanceof MinecraftServer) {
            icommandlistener.sendMessage(new ChatMessage(s, aobject));
        }

    }

    @Override
    protected MinecraftServer a() {
        return this.a;
    }
}
