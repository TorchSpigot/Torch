package com.destroystokyo.paper;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldServer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.File;

public class PaperCommand extends Command {

    public PaperCommand(String name) {
        super(name);
        this.description = "Paper related commands";
        this.usageMessage = "/paper [reload | version]";
        this.setPermission("bukkit.command.paper");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + usageMessage);
            return false;
        }

        if (args[0].equals("reload")) {
            Command.broadcastCommandMessage(sender, ChatColor.RED + "Please note that this command is not supported and may cause issues.");
            Command.broadcastCommandMessage(sender, ChatColor.RED + "If you encounter any issues please use the /stop command to restart your server.");

            MinecraftServer console = MinecraftServer.getServer();
            com.destroystokyo.paper.PaperConfig.init((File) console.options.valueOf("paper-settings"));
            for (WorldServer world : console.worlds) {
                world.paperConfig.init();
            }
            console.server.reloadCount++;

            Command.broadcastCommandMessage(sender, ChatColor.GREEN + "Paper config reload complete.");
        }

        if (args[0].equals("version")) {
            org.bukkit.Bukkit.getServer().getCommandMap().getCommand("version").execute(sender, commandLabel, new String[0]);
        }

        return true;
    }
}
