package net.skeagle.vrnparkour.parkour;

import net.skeagle.vrnparkour.VRNparkour;
import net.skeagle.vrnparkour.hologram.Hologram;
import net.skeagle.vrnparkour.utils.Utils;
import org.bukkit.Location;

import java.util.UUID;

public class ParkourCheckpoint {
    private final Location location;
    private final Hologram hologram;

    public ParkourCheckpoint(final Location location, final Hologram hologram) {
        this.hologram = hologram;
        this.location = location;
    }

    public Location getLocation() {
        return this.location;
    }

    public Hologram getHologram() {
        return this.hologram;
    }

    public String serialize() {
        return Utils.serializeLocation(getLocation()) + "!!" + getHologram().getId().toString();
    }

    public static ParkourCheckpoint Deserialize(final String s) {
        String[] split = s.split("!!");
        return new ParkourCheckpoint(Utils.deSerializeLocation(split[0]), VRNparkour.getInstance().getHologramManager().getHologram(UUID.fromString(split[1])));
    }
}

