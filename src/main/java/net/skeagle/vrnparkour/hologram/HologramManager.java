package net.skeagle.vrnparkour.hologram;

import net.skeagle.vrnlib.misc.Task;
import net.skeagle.vrnparkour.VRNparkour;
import net.skeagle.vrnparkour.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.util.EulerAngle;
import net.skeagle.vrnlib.sql.SQLHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HologramManager {
    private final List<Hologram> holograms;

    public HologramManager() {
        this.holograms = new ArrayList<>();
        this.load();
    }

    private void load() {
        SQLHelper db = VRNparkour.getInstance().getDB();
        SQLHelper.Results res = db.queryResults("SELECT * FROM hologram");
        res.forEach(pk -> {
            UUID id = UUID.fromString(res.getString(1));
            String hologram = res.getString(2);
            Location location = Utils.deSerializeLocation(res.get(3));
            holograms.add(new Hologram(location, hologram, id));
        });
    }

    public void save() {
        holograms.forEach(Hologram::save);
    }

    public Hologram createHologram(final Location loc, final String customName) {
        final ArmorStand armorStand = (ArmorStand) loc.clone().getWorld().spawnEntity(loc.clone().add(0.5, 0.38, 0.5), EntityType.ARMOR_STAND);
        armorStand.setAI(false);
        armorStand.setSmall(true);
        armorStand.setGravity(false);
        armorStand.setVisible(false);
        armorStand.setBasePlate(false);
        armorStand.setCustomName(customName);
        armorStand.setCustomNameVisible(true);
        armorStand.setInvulnerable(true);
        armorStand.setRightLegPose(new EulerAngle(0, 180, 0));
        armorStand.setLeftLegPose(new EulerAngle(0, 180, 0));
        final Hologram hologram = new Hologram(loc, customName, armorStand.getUniqueId());
        this.holograms.add(hologram);
        hologram.save();
        return hologram;
    }

    public Hologram getHologram(final UUID uuid) {
        return this.getHolograms().stream().filter(holo -> holo.getId().equals(uuid)).findFirst().orElse(null);
    }

    public void removeHologram(final UUID uuid) {
        this.getHologram(uuid).remove();
        SQLHelper db = VRNparkour.getInstance().getDB();
        db.execute("DELETE FROM hologram WHERE id = (?)", uuid.toString());
        this.holograms.remove(this.getHologram(uuid));
    }

    public List<Hologram> getHolograms() {
        return this.holograms;
    }
}

