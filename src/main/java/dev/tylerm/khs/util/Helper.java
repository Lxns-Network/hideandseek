package dev.tylerm.khs.util;

import org.bukkit.Location;

import java.util.concurrent.ThreadLocalRandom;

public class Helper {
    public static Location obfuscateLocation(Location location) {
        var rand = ThreadLocalRandom.current();
        return location.clone().add(
                rand.nextInt(0, 16) - 8,
                rand.nextInt(0, 16) - 8,
                rand.nextInt(0, 16) - 8
        );
    }
}
