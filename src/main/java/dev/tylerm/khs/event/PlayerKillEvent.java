package dev.tylerm.khs.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerKillEvent extends Event implements Cancellable {
    public static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;
    private final Player killer;
    private final Player victim;

    public PlayerKillEvent(Player killer,@Nullable Player victim) {
        this.killer = killer;
        this.victim = victim;
    }

    @Nullable
    public Player getKiller() {
        return killer;
    }

    public Player getVictim() {
        return victim;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
