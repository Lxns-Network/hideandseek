package dev.tylerm.khs.event;

import dev.tylerm.khs.game.util.WinType;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GameEndEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final WinType type;

    public GameEndEvent(WinType type) {
        this.type = type;
    }

    public WinType getType() {
        return type;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
