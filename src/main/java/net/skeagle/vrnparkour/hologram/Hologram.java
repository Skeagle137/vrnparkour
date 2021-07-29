package net.skeagle.vrnparkour.hologram;

import net.skeagle.vrnparkour.VRNparkour;
import net.skeagle.vrnparkour.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import net.skeagle.vrnlib.sql.SQLHelper;

import java.util.UUID;

public class Hologram {
    private final UUID id;
    private final Location loc;
    private String hologram;

    public Hologram(final Location loc, final String hologram, final UUID id) {
        this.id = id;
        this.loc = loc;
        this.hologram = hologram;
    }

    public void setHologram(final String s) {
        this.hologram = s;
        final boolean loaded = this.loc.getChunk().isLoaded();
        this.loc.getChunk().load();
        for (final Entity entity : this.loc.getChunk().getEntities())
            if (entity.getUniqueId().toString().equalsIgnoreCase(this.id.toString()))
                entity.setCustomName(s);
        if (!loaded)
            this.loc.getChunk().unload();
    }

    public String getHologram() {
        return this.hologram;
    }

    public Location getLoc() {
        return this.loc;
    }

    public UUID getId() {
        return this.id;
    }

    public void remove() {
        final boolean loaded = this.loc.getChunk().isLoaded();
        this.loc.getChunk().load();
        for (final Entity entity : this.loc.getChunk().getEntities())
            if (entity.getUniqueId().toString().equalsIgnoreCase(this.id.toString()))
                entity.remove();
        if (!loaded)
            this.loc.getChunk().unload();
    }

    public void save() {
        SQLHelper db = VRNparkour.getInstance().getDB();
        db.execute("DELETE FROM hologram WHERE id = (?)", getId().toString());
        db.execute("INSERT INTO hologram " +
                "(id, hologram, location) " +
                "VALUES (?, ?, ?)", getId().toString(), getHologram(), Utils.serializeLocation(this.getLoc()));
    }
}

