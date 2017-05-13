package org.spigotmc;

import java.io.File;
import net.minecraft.server.EntityPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.torch.server.TorchServer;

import com.google.common.collect.ImmutableList;

public class RestartCommand extends Command {

    public RestartCommand(String name) {
        super(name);
        this.description = "Restarts the server";
        this.usageMessage = "/restart";
        this.setPermission("bukkit.command.restart");
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (testPermission(sender)) {
            TorchServer.getServer().processQueue.add(() -> restart());
        }
        return true;
    }

    public static void restart() {
        restart(new File(SpigotConfig.restartScript));
    }

    @SuppressWarnings("deprecation")
    public static void restart(final File script) {
        AsyncCatcher.enabled = false; // Disable async catcher incase it interferes with us
        try {
            if (script.isFile()) {
                System.out.println("Attempting to restart with " + SpigotConfig.restartScript);

                // Disable Watchdog
                WatchdogThread.doStop();
                shutdownServer(); // Paper - Moved to function that will handle sync and async

                // This will be done AFTER the server has completely halted
                Thread shutdownHook = new Thread(() -> {
                    try {
                        String os = System.getProperty("os.name").toLowerCase(java.util.Locale.ENGLISH);
                        if (os.contains("win")) {
                            Runtime.getRuntime().exec("cmd /c start " + script.getPath());
                        } else {
                            Runtime.getRuntime().exec(new String[] { "sh", script.getPath() });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                shutdownHook.setDaemon(true);
                Runtime.getRuntime().addShutdownHook(shutdownHook);
            } else {
                System.out.println("Startup script '" + SpigotConfig.restartScript + "' does not exist! Stopping server.");
                
                // Actually shutdown
                try {
                    TorchServer.getServer().stopServer();
                } catch (Throwable t) {
                    ;
                }
            }
            
            TorchServer.getServer().systemExitNow();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Paper start - sync copied from above with minor changes, async added
    @SuppressWarnings("deprecation")
    private static void shutdownServer() {
        if (TorchServer.getServer().isMainThread()) {
            // Kick all players
            for (EntityPlayer player : ImmutableList.copyOf(TorchServer.getServer().getPlayerList().players)) {
                player.playerConnection.disconnect(SpigotConfig.restartMessage);
            }
            // Give the socket a chance to send the packets
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ;
            }

            closeSocket();

            // Actually shutdown
            try {
                TorchServer.getServer().stopServer();
            } catch (Throwable t) {
                ;
            }
        } else {
            closeSocket();
            TorchServer.getServer().getServerThread().stop();
        }
    }

    // Paper - Split from moved code
    private static void closeSocket() {
        // Close the socket so we can rebind with the new process
        TorchServer.getServer().getServerConnection().b();
        
        // Give time for it to kick in
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ;
        }
    }
    // Paper end
}
