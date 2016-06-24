package org.spigotmc;

// TacoSpigot start
import java.util.List;
import java.util.Set;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.ForwardingSet;
// TacoSpigot end

import net.minecraft.server.v1_9_R2.MinecraftServer;

public class AsyncCatcher
{

    public static final boolean enabled = true; // TacoSpigot - final (never disable)

    @SuppressWarnings("deprecation")
	public static void catchOp(String reason)
    {
        if ( enabled && Thread.currentThread() != MinecraftServer.getServer().primaryThread )
        {
            MinecraftServer.LOGGER.warn(reason + " called async on " + Thread.currentThread().getName()); // TacoSpigot - log
            throw new IllegalStateException( "Asynchronous " + reason + " on thread " + Thread.currentThread().getName() + "!" ); // TacoSpigot - give thread
        }
    }

    // TacoSpigot start - safety wrappers
    public static <E> List<E> catchAsyncUsage(List<E> list, String msg) {
        return new ForwardingList<E>() {
            @Override
            protected List<E> delegate() {
                AsyncCatcher.catchOp(msg);
                return list;
            }
        };
    }

    public static <E> Set<E> catchAsyncUsage(Set<E> set, String msg) {
        return new ForwardingSet<E>() {
            @Override
            protected Set<E> delegate() {
                AsyncCatcher.catchOp(msg);
                return set;
            }
        };
    }
    // TacoSpigot end
}
