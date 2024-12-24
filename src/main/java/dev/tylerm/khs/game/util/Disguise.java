package dev.tylerm.khs.game.util;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.messages.ActionBar;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import dev.tylerm.khs.util.Helper;
import dev.tylerm.khs.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

@SuppressWarnings("deprecation")
public class Disguise {
    final Player hider;
    final Material material;
    FallingBlock block;
    LivingEntity hitBox;
    Location blockLocation;
    boolean solid, solidify, solidifying;
    static Team hidden;

    static {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        hidden = board.getTeam("KHS_Collision");
        if (hidden == null) {
            hidden = board.registerNewTeam("KHS_Collision");
        }
        hidden.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        hidden.setCanSeeFriendlyInvisibles(false);
    }

    public Disguise(Player player, Material material) {
        this.hider = player;
        this.material = material;
        this.solid = false;
        respawnFallingBlock();
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 0, false, false));
        hidden.addEntry(player.getName());
    }

    public void remove() {
        if (block != null)
            block.remove();
        if (hitBox != null) {
            hidden.removeEntry(hitBox.getUniqueId().toString());
            hitBox.remove();
        }
        if (solid)
            sendBlockUpdate(blockLocation, Material.AIR);
        hider.removePotionEffect(PotionEffectType.INVISIBILITY);
        hidden.removeEntry(hider.getName());
    }

    public int getEntityID() {
        if (block == null) return -1;
        return block.getEntityId();
    }

    public int getHitBoxID() {
        if (hitBox == null) return -1;
        return hitBox.getEntityId();
    }

    public Material getMaterial() {
        return material;
    }

    public Player getPlayer() {
        return hider;
    }

    public boolean isSolid() {
        return solid;
    }

    public LivingEntity getHitBox() {
        return hitBox;
    }

    public FallingBlock getBlock() {
        return block;
    }

    public void update() {

        if (block == null || block.isDead()) {
            if (block != null) block.remove();
            respawnFallingBlock();
        }

        if (solidify) {
            if (!solid) {
                solid = true;
                blockLocation = hider.getLocation().getBlock().getLocation();
                respawnHitbox();
                onSolidifyChanged(true);
            }
            sendBlockUpdate(blockLocation, material);
        } else if (solid) {
            solid = false;
            hidden.removeEntry(hitBox.getUniqueId().toString());
            hitBox.remove();
            hitBox = null;
            sendBlockUpdate(blockLocation, Material.AIR);
            onSolidifyChanged(false);
        }
        toggleEntityVisibility(block, !solid);
        teleportEntity(hitBox, true);
        teleportEntity(block, solid);
    }

    private void onSolidifyChanged(boolean solid) {
        if (solid) {
            var loc = Helper.obfuscateLocation(getPlayer().getLocation());
            var vec = new Vector3d(loc.getX(), -66.0, loc.getZ());
            var tp = new WrapperPlayServerEntityTeleport(
                    getPlayer().getEntityId(),
                    new com.github.retrooper.packetevents.protocol.world.Location(
                            vec, loc.getYaw(), loc.getPitch()
                    ),
                    true
            );
            // seekers may see a "phantom" when hiders come back to their block.
            // so obfuscating is a little bit necessary
            for (Player seeker : Main.getInstance().getBoard().getSeekers()) {
                PacketEvents.getAPI().getPlayerManager().sendPacket(seeker, tp);
            }
        } else {
            var loc = getPlayer().getLocation();
            var tp = new WrapperPlayServerEntityTeleport(
                    getPlayer().getEntityId(),
                    new com.github.retrooper.packetevents.protocol.world.Location(
                            new Vector3d(loc.getX(), loc.getY(),loc.getZ()),
                            loc.getYaw(), loc.getPitch()
                    ),
                    getPlayer().isOnGround()
            );
            for (Player seeker : Main.getInstance().getBoard().getSeekers()) {
                PacketEvents.getAPI().getPlayerManager().sendPacket(seeker, tp);
            }
        }
    }

    public void setSolidify(boolean value) {
        this.solidify = value;
    }

    private void sendBlockUpdate(Location location, Material material) {
        var data = material.createBlockData();
        Bukkit.getOnlinePlayers().forEach(receiver -> {
            if (receiver.getName().equals(hider.getName())) return;
            receiver.sendBlockChange(location, data);
        });
    }

    private void teleportEntity(Entity entity, boolean center) {
        if (entity == null) return;
        WrapperPlayServerEntityTeleport packet;
        double x, y, z;
        if (center) {
            x = Math.round(hider.getLocation().getX() + .5) - .5;
            y = Math.round(hider.getLocation().getY());
            z = Math.round(hider.getLocation().getZ() + .5) - .5;
        } else {
            x = hider.getLocation().getX();
            y = hider.getLocation().getY();
            z = hider.getLocation().getZ();
        }
        var pos = entity.getLocation();
        packet = new WrapperPlayServerEntityTeleport(entity.getEntityId(),new Vector3d(x,y,z),pos.getYaw(),pos.getPitch(),entity.isOnGround());
        Bukkit.getOnlinePlayers().forEach(p -> PacketEvents.getAPI().getPlayerManager().sendPacket(p, packet));
    }

    private void toggleEntityVisibility(Entity entity, boolean show) {
        if (entity == null) return;
        Bukkit.getOnlinePlayers().forEach(receiver -> {
            if (receiver == hider) return;
            if (show) {
                if (entity instanceof Player p) {
                    receiver.showPlayer(Main.getInstance(), p);
                } else receiver.showEntity(Main.getInstance(), entity);
            } else if (entity instanceof Player p) {
                receiver.hidePlayer(Main.getInstance(), p);
            } else receiver.hideEntity(Main.getInstance(), entity);
            //Main.getInstance().getEntityHider().hideEntity(receiver, entity);
        });
    }

    private void respawnFallingBlock() {
        block = hider.getLocation().getWorld().spawnFallingBlock(hider.getLocation().add(0, 1000, 0), material, (byte) 0);
        block.setGravity(false);
        block.setDropItem(false);
        block.setInvulnerable(true);
    }

    private void respawnHitbox() {
        hitBox = (PolarBear) hider.getLocation().getWorld().spawnEntity(hider.getLocation().add(0, 1000, 0), EntityType.POLAR_BEAR);
        hitBox.setGravity(false);
        hitBox.setAI(false);
        hitBox.setInvulnerable(true);
        hitBox.setCanPickupItems(false);
        hitBox.setCollidable(false);
        hitBox.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 0, false, false));
        hidden.addEntry(hitBox.getUniqueId().toString());
    }

    public void startSolidifying() {
        if (solidifying) return;
        if (solid) return;
        solidifying = true;
        final Location lastLocation = hider.getLocation();
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> solidifyUpdate(lastLocation, 3), 10);
    }

    private void solidifyUpdate(Location lastLocation, int time) {
        Location currentLocation = hider.getLocation();
        if (lastLocation.getWorld() != currentLocation.getWorld()) {
            solidifying = false;
            return;
        }
        if (lastLocation.distance(currentLocation) > .1) {
            solidifying = false;
            return;
        }
        var pos = currentLocation.getBlock();
        if (!pos.getType().isAir()
                && pos.getType() != Material.WATER
                && (currentLocation.getY() - pos.getY() > 0)) {
            solidifying = false;
            return;
        }
        if (time == 0) {
            ActionBar.clearActionBar(hider);
            setSolidify(true);
            solidifying = false;
        } else {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < time; i++) {
                s.append("▪");
            }
            ActionBar.sendActionBar(hider, s.toString());
            XSound.BLOCK_NOTE_BLOCK_PLING.play(hider, 1, 1);
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> solidifyUpdate(lastLocation, time - 1), 20);
        }
    }

}