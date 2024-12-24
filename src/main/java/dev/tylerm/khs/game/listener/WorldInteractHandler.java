package dev.tylerm.khs.game.listener;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.game.Board;
import dev.tylerm.khs.game.util.Status;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class WorldInteractHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Board board = Main.getInstance().getBoard();
        if(board.contains(player)) {
            event.setCancelled(true);
        }
        if(event.getBlock().getType().isCompostable()){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickItem(EntityPickupItemEvent event) {
        if (Main.getInstance().getGame().getStatus() != Status.ENDED) {
            return;
        }
        if(event.getEntity() instanceof Player player){
            Board board = Main.getInstance().getBoard();
            if(board.contains(player)) {
                event.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onLaidEgg(EntityDropItemEvent event){
        if (!(event.getEntity() instanceof Player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHangingEntityBreakByEntity(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getRemover();
        Board board = Main.getInstance().getBoard();
        if(board.contains(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreakDoor(EntityBreakDoorEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Board board = Main.getInstance().getBoard();
        if(board.contains(player)) {
            event.setCancelled(true);
        }
    }

}
