package dev.tylerm.khs.game.listener;

import com.cryptomorin.xseries.XSound;
import dev.tylerm.khs.Main;
import dev.tylerm.khs.event.PlayerKillEvent;
import dev.tylerm.khs.game.Board;
import dev.tylerm.khs.game.Game;
import dev.tylerm.khs.game.PlayerLoader;
import dev.tylerm.khs.game.util.Status;
import dev.tylerm.khs.item.CustomItems;
import dev.tylerm.khs.task.SpecialArrowTaunt;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static dev.tylerm.khs.configuration.Config.*;
import static dev.tylerm.khs.configuration.Localization.message;

public class DamageHandler implements Listener {
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (Main.getInstance().getGame().getStatus() != Status.PLAYING) {
            return;
        }
        var directEntity = event.getEntity();
        var hitEntity = event.getHitEntity();
        var hitBlock = event.getHitBlock();

        if(hitBlock != null && directEntity.hasMetadata(CustomItems.OWL_BOW_METADATA_KEY)){
            new SpecialArrowTaunt(hitBlock.getLocation().add(0,1,0), 32).runTaskTimer(Main.getInstance(), 0L, 5L);
            directEntity.remove();
            return;
        }

        if(hitEntity instanceof Player player){
            if(Main.getInstance().getBoard().isHider(player)){
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                    player.setArrowsInBody(0);
                }, 10L);
            }
            if (directEntity instanceof WindCharge) {
                if (player != directEntity.getShooter() && player.getHealth() > 6.5) {
                    player.setHealth(player.getHealth() - 6.5);
                    //todo: test 风弹伤害
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        Board board = Main.getInstance().getBoard();
        Game game = Main.getInstance().getGame();
        // If you are not a player, get out of here
        if (!(event.getEntity() instanceof Player)) return;
        // Define variables
        Player player = (Player) event.getEntity();
        Player attacker = null;
        // If map is not setup we won't be able to process on it :o
        if (!game.isCurrentMapValid()) {
            return;
        }
        // If there is an attacker, find them
        if (event instanceof EntityDamageByEntityEvent) {
            if (((EntityDamageByEntityEvent) event).getDamager() instanceof Player)
                attacker = (Player) ((EntityDamageByEntityEvent) event).getDamager();
            else if (((EntityDamageByEntityEvent) event).getDamager() instanceof Projectile)
                if (((Projectile) ((EntityDamageByEntityEvent) event).getDamager()).getShooter() instanceof Player)
                    attacker = (Player) ((Projectile) ((EntityDamageByEntityEvent) event).getDamager()).getShooter();
        }
        // Makes sure that if there was an attacking player, that the event is allowed for the game
        if (attacker != null) {
            if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                var directEntity = event.getDamageSource().getDirectEntity();
                if (directEntity instanceof Arrow arrow) {
                    if (arrow.hasMetadata(CustomItems.OWL_BOW_METADATA_KEY)) {
                        new SpecialArrowTaunt(player.getLocation(), 32)
                                .runTaskTimer(Main.getInstance(), 0L, 5L);
                        event.setDamage(0);
                        return;
                    }
                }
            }
            // Cancel if one player is in the game but other isn't
            if ((board.contains(player) && !board.contains(attacker)) || (!board.contains(player) && board.contains(attacker))) {
                event.setCancelled(true);
                return;
                // Ignore event if neither player are in the game
            } else if (!board.contains(player) && !board.contains(attacker)) {
                return;
                // Ignore event if players are on the same team, or one of them is a spectator
            } else if (board.onSameTeam(player, attacker)) {
                event.setCancelled(true);
                return;
            } else if (board.isSpectator(player) || board.isSpectator(attacker)) {
                event.setCancelled(true);
                return;
                // Ignore the event if pvp is disabled, and a hider is trying to attack a seeker
            } else if (!pvpEnabled && board.isHider(attacker) && board.isSeeker(player)) {
                event.setCancelled(true);
                return;
            }
            // If there was no attacker, if the damaged is not a player, ignore them.
        } else if (!board.contains(player)) {
            return;
            // If there is no attacker, it most of been by natural causes. If pvp is disabled, and config doesn't allow natural causes, cancel event.
        } else if (!pvpEnabled && !allowNaturalCauses && board.contains(player)) {
            event.setCancelled(true);
            return;
        }
        // Spectators and cannot take damage
        if (board.isSpectator(player)) {
            event.setCancelled(true);
            if (player.getLocation().getBlockY() < -64) {
                game.getCurrentMap().getGameSpawn().teleport(player);
            }
            return;
        }
        // Players cannot take damage while game is not in session
        if (board.contains(player) && game.getStatus() != Status.PLAYING) {
            event.setCancelled(true);
            return;
        }
        if (blindnessWhenHit > 0 && pvpEnabled
                && board.isSeeker(player) &&
                attacker != null && board.isHider(attacker)) {
            // icybear: give seeker blindness effect when hit
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blindnessWhenHit, 1));
        }
        // Check if player dies (pvp mode)
        if (pvpEnabled && player.getHealth() - event.getFinalDamage() >= 0.5) return;
        // Handle death event
        event.setCancelled(true);

        // icybear: post death event
        {
            var killer = event.getDamageSource().getCausingEntity();
            Player k = null;
            if (killer instanceof Player a) {
                k = a;
            }
            var killEvent = new PlayerKillEvent(player, k);
            Bukkit.getServer().getPluginManager().callEvent(killEvent);
            if (killEvent.isCancelled()) return;
        }
        // Play death effect
        XSound.ENTITY_PLAYER_DEATH.play(player, 1, 1);
        // Reveal player if they are disguised
        Main.getInstance().getDisguiser().reveal(player);
        // Teleport player to seeker spawn
        if (delayedRespawn && !respawnAsSpectator) {
            game.getCurrentMap().getGameSeekerLobby().teleport(player);
            player.sendMessage(messagePrefix + message("RESPAWN_NOTICE").addAmount(delayedRespawnDelay));
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
                if (game.getStatus() == Status.PLAYING) {
                    game.getCurrentMap().getGameSpawn().teleport(player);
                    player.setHealth(20d);
                }
            }, delayedRespawnDelay * 20L);
        } else {
            game.getCurrentMap().getGameSpawn().teleport(player);
            player.setHealth(20d);
        }
        // Add leaderboard stats
        board.addDeath(player.getUniqueId());
        if (attacker != null) board.addKill(attacker.getUniqueId());
        // Broadcast player death message
        if (board.isSeeker(player)) {
            game.broadcastMessage(message("GAME_PLAYER_DEATH").addPlayer(player).toString());
        } else if (board.isHider(player)) {
            // icybear: update the timer
            game.extendTime();
            if (attacker == null) {
                game.broadcastMessage(message("GAME_PLAYER_FOUND")
                        .addPlayer(player).addAmount(extendPerKill).toString());
            } else {
                game.broadcastMessage(message("GAME_PLAYER_FOUND_BY")
                        .addPlayer(player).addPlayer(attacker).addAmount(extendPerKill).toString());
            }
            if (respawnAsSpectator) {
                board.addSpectator(player);
                PlayerLoader.loadDeadHiderSpectator(player, game.getCurrentMap());
            } else {
                board.addSeeker(player);
                PlayerLoader.resetPlayer(player, board);
            }
        }
        board.reloadBoardTeams();
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Main.getInstance().getDisguiser().reveal(event.getEntity());
    }

}
