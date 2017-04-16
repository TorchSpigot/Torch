package net.minecraft.server;

import java.io.File;
import org.apache.logging.log4j.Logger;
import org.torch.server.TorchServer;

public class EULA {

    private static final Logger a = TorchServer.logger;
    private final File b;
    private final boolean c;

    public EULA(File file) {
        this.b = file;
        this.c = this.a(file);
    }

    private boolean a(File file) {
        return true;
    }

    public boolean a() {
        return this.c;
    }

    public void b() {}
}
