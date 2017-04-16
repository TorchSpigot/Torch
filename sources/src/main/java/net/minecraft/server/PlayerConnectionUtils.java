package net.minecraft.server;

import co.aikar.timings.MinecraftTimings; // Paper
import co.aikar.timings.Timing; // Paper

public class PlayerConnectionUtils {

    // Paper start, fix decompile and add timings
    public static <T extends PacketListener> void ensureMainThread(final Packet<T> packet, final T listener, IAsyncTaskHandler iasynctaskhandler) throws CancelledPacketHandleException {
        if (!iasynctaskhandler.isMainThread()) {
            Timing timing = MinecraftTimings.getPacketTiming(packet);
            iasynctaskhandler.postToMainThread(() -> {
                try (Timing ignored = timing.startTiming()) {
                    packet.a(listener);
                }
            });
            throw CancelledPacketHandleException.INSTANCE;
        }
    }
    // Paper end
}
