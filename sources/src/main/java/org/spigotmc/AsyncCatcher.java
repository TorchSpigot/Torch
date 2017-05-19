package org.spigotmc;

import static org.torch.server.TorchServer.getServer;

public class AsyncCatcher {
    public static boolean enabled = true;

    public static void catchOp(String reason) {
        if (enabled) {
            if (!getServer().isMainThread()) throw new IllegalStateException( "Asynchronous " + reason + "!" );
        }
    }
}
