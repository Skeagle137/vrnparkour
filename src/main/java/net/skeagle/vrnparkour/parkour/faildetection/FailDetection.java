package net.skeagle.vrnparkour.parkour.faildetection;

import net.skeagle.vrnparkour.parkour.Parkour;
import org.bukkit.entity.Player;

public interface FailDetection {
    boolean check(final Parkour p0, final Player p1);

    String key();
}
