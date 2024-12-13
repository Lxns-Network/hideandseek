package dev.tylerm.khs.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class StartTimerUpdateEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final int timeLeft;

    public StartTimerUpdateEvent(int timeLeft) {
        this.timeLeft = timeLeft;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
