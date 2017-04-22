package net.minecraft.server;

import org.apache.logging.log4j.Logger;
import org.torch.server.TorchServer;

public class DedicatedPlayerList extends PlayerList {
    private static final Logger f = TorchServer.logger;

    public DedicatedPlayerList(DedicatedServer dedicatedserver) {
        super(dedicatedserver);
    }

    private void w() {
    	super.getReactor().saveIPBanList();
    }

    private void x() {
    	super.getReactor().savePlayerBanList();
    }

    private void y() {
    	super.getReactor().loadIPBanList();
    }

    private void z() {
    	super.getReactor().loadPlayerBanList();
    }

    private void A() {
    	super.getReactor().loadOpsList();
    }

    private void B() {
    	super.getReactor().saveOpsList();
    }

    private void C() {
    	super.getReactor().readWhiteList();
    }

    private void D() {
    	super.getReactor().saveWhiteList();
    }
}
