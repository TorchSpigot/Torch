package org.spigotmc;

import org.torch.server.TorchServer;

public class AsyncCatcher {
    public static boolean enabled = true;

    public static void catchOp(String reason) {
        if (enabled) {
        	if (!TorchServer.getServer().isMainThread()) throw new IllegalStateException( "Asynchronous " + reason + "!" );
        }
    }
}
