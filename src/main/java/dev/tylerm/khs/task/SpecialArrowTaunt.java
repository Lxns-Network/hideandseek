package dev.tylerm.khs.task;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.game.util.Status;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class SpecialArrowTaunt extends BukkitRunnable {
    private final Location location;
    private final int count;
    private int counter = 0;

    public SpecialArrowTaunt(Location location, int count) {
        this.location = location;
        this.count = count;
    }

    @Override
    public void run() {
        if(Main.getInstance().getGame().getStatus() != Status.PLAYING || counter++ > count){
            cancel();
            return;
        }
        location.getWorld().spawnParticle(
                Particle.NOTE,
                location,
                4
        );
        location.getWorld().playSound(
                location,
                Sound.BLOCK_NOTE_BLOCK_GUITAR,
                2f,2f
        );
    }
}
