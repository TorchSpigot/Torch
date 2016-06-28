package net.techcable.tacospigot.event.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import static com.google.common.base.Preconditions.*;

/**
 * Fired when an item (possibly empty) is sent to a player
 * <p>Modifying the event's item only changes what the player sees, not what is actually on the server.</p>
 */
public class ItemSendEvent extends Event {
    private final Player player;
    private ItemStack stack;

    public ItemSendEvent(@Nonnull Player player, @Nullable ItemStack stack) {
        this.player = checkNotNull(player, "Null player");
        this.stack = stack;
    }

    /**
     * Get the stack that is being sent, or null if an empty item is being sent
     *
     * @return the stack that is being sent, or null if none
     */
    @Nullable
    public ItemStack getStack() {
        return stack;
    }

    /**
     * Set the stack that should be sent, or null to send an empty item
     *
     * @param stack the stack that should be being sent, or null if nothing should be sent
     */
    public void setStack(@Nullable ItemStack stack) {
        this.stack = stack;
    }

    /**
     * Get the player the item is being sent to
     *
     * @return the player
     */
    @Nonnull
    public Player getPlayer() {
        return player;
    }

    // Event boilerplate
    private static final HandlerList handlerList = new HandlerList();

    public HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }
}