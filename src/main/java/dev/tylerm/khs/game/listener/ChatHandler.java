package dev.tylerm.khs.game.listener;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.game.util.Status;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatHandler implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        //todo add config for chat configuration
        var board = Main.getInstance().getBoard();
        if (board.isSpectator(event.getPlayer())) {
            board.getSpectators().forEach(spectator -> spectator.sendMessage(ChatColor.GRAY + "[SPECTATOR] " + event.getPlayer().getName() + ": " + event.getMessage()));
        } else {
            var status = Main.getInstance().getGame().getStatus();
            if (status == Status.PLAYING) {
                Bukkit.broadcastMessage(
                        ChatColor.BOLD +
                                (board.isHider(event.getPlayer()) ?
                                        ChatColor.GOLD + "[BLOCK] "
                                        : ChatColor.BLUE + "SEEKER ")
                                + ChatColor.WHITE + event.getPlayer().getName() + ": " + ChatColor.GRAY + event.getMessage());
            } else {
                Bukkit.broadcastMessage(
                        event.getPlayer().getName() + ": " + ChatColor.GRAY + event.getMessage()
                );
            }
        }
    }

}
