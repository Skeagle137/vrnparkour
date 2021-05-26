package net.skeagle.vrnparkour.parkour.faildetection;

import net.skeagle.vrnparkour.parkour.Parkour;
import org.bukkit.entity.Player;

public class HeightFailDetection implements FailDetection {
    @Override
    public boolean check(final Parkour parkour, final Player player) {
        return (int)player.getLocation().getY() > parkour.getFailHeight();
    }

    @Override
    public String key() {
        return "height";
    }
}

